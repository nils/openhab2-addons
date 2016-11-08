/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.collections.CollectionUtils;
import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;
import org.openhab.binding.pioneeravr.internal.util.Pair;
import org.openhab.binding.pioneeravr.protocol.AvrCommand;
import org.openhab.binding.pioneeravr.protocol.AvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;
import org.openhab.binding.pioneeravr.protocol.event.AvrConnectionListener;
import org.openhab.binding.pioneeravr.protocol.event.AvrNotificationEvent;
import org.openhab.binding.pioneeravr.protocol.event.AvrNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A class that wraps the communication to Pioneer AVR devices by using Input/Ouptut streams.
 *
 * see {@link http ://www.pioneerelectronics.com/StaticFiles/PUSA/Files/Home%20Custom %20Install/VSX-1120-K-RS232.PDF}
 * for the protocol specs
 *
 * @author Antoine Besnard
 * @author Rainer Ostendorf
 * @author based on the Onkyo binding by Pauli Anttila and others
 */
public abstract class StreamAvrConnection implements AvrConnection {

    private final Logger logger = LoggerFactory.getLogger(StreamAvrConnection.class);

    /**
     * Timeout when the response for a request is not received after this duration.
     */
    private static final Integer RESPONSE_TIMEOUT = 500;

    /**
     * The number of milliseconds between a notification is received and its processing.
     */
    private static final Integer NOTIFICATION_EXECUTOR_DELAY = 250;

    private List<AvrNotificationListener> updateListeners;
    private List<AvrConnectionListener> connectionListeners;

    private ScheduledExecutorService executorService;
    private Future<?> inputStreamReaderFuture;
    private DataOutputStream outputStream;

    private Object responseLock;
    private AvrCommand sentCommand;
    private AvrResponse receivedResponse;

    private Map<ResponseType, Pair<AvrNotificationEvent, ScheduledFuture<?>>> notificationsByType;

    private int burstMessageDelay;
    private boolean isBurstModeEnabled;

    public StreamAvrConnection(ScheduledExecutorService executorService) {
        this.executorService = executorService;
        this.updateListeners = new CopyOnWriteArrayList<>();
        this.connectionListeners = new CopyOnWriteArrayList<>();
        this.responseLock = new Object();
        this.notificationsByType = new HashMap<>();
        this.burstMessageDelay = 0;
    }

    @Override
    public void addNotificationListener(AvrNotificationListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void addConnectionListener(AvrConnectionListener listener) {
        connectionListeners.add(listener);
    }

    /**
     * Notify the listeners with the given notification event. The event may be dropped to limit the notification
     * rate.
     *
     * The volume level notifications may be also discarded if needed.
     *
     * @param event
     */
    protected void notifyListeners(AvrNotificationEvent event) {
        ResponseType notificationType = event.getNotification().getResponseType();

        // Limit the rate of the notifications since it can be pretty heavy (for example, when the volume is
        // modified without the SetVolume command, it can be up to 200 notifications by seconds)
        // => limit to 1 notification of each type each 250 ms (keep only the last notification of each type)
        synchronized (notificationsByType) {
            Pair<AvrNotificationEvent, ScheduledFuture<?>> notificationContext = notificationsByType
                    .remove(event.getNotification().getResponseType());
            // If pending a notification is already found for the notification type, remove it and cancel the
            // notification process.
            if (notificationContext != null) {
                // If the notification process cannot be cancelled, it is already executed => it is an old process that
                // is just being cleaned. If it has been cancelled, it is a pending notification that will be replaced.
                if (notificationContext.getValue().cancel(false)) {
                    logger.debug("Cancelled the listeners notification for the notification {}",
                            notificationContext.getKey().getNotification());
                }
            }
            // Then add the notification and schedule the notification process. This process may be cancelled later if a
            // new notification of the same type is received before its execution.
            logger.debug("Scheduling the listeners notification for the notification {}", event.getNotification());
            notificationsByType.put(notificationType, Pair.of(event, scheduleEventNotificationProcess(event)));
        }
    }

    /**
     * Schedule the notification process of the given event in {@link #NOTIFICATION_EXECUTOR_DELAY} milliseconds.
     *
     * @param event the event to notify listeners with.
     */
    protected ScheduledFuture<?> scheduleEventNotificationProcess(AvrNotificationEvent event) {
        return executorService.schedule(() -> {
            // Notify all the listeners with this event.
            logger.debug("Notify notification listeners for notification event: {}", event.getNotification());
            for (AvrNotificationListener pioneerAvrEventListener : updateListeners) {
                pioneerAvrEventListener.statusUpdateReceived(event);
            }
        }, NOTIFICATION_EXECUTOR_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean connect() {
        boolean isConnected = isConnected();
        if (!isConnected) {
            try {
                openConnection();

                // Start the inputStream reader.
                inputStreamReaderFuture = executorService.submit(new IpControlInputStreamReader(getInputStream()));

                // Get Output stream
                outputStream = new DataOutputStream(getOutputStream());

                isConnected = isConnected();

                if (isConnected) {
                    notifyConnection();
                }
            } catch (IOException ioException) {
                logger.debug("Can't connect to {}. Cause: {}", getConnectionName(), ioException.getMessage());
            }
        }
        return isConnected;
    }

    /**
     * Open the connection to the AVR.
     *
     * @throws IOException
     */
    protected abstract void openConnection() throws IOException;

    /**
     * Return the inputStream to read responses.
     *
     * @return
     * @throws IOException
     */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * Return the outputStream to send commands.
     *
     * @return
     * @throws IOException
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    @Override
    public void close() {
        if (inputStreamReaderFuture != null) {
            // This method block until the reader is really stopped.
            inputStreamReaderFuture.cancel(true);
            inputStreamReaderFuture = null;
            logger.debug("Stream reader stopped for AVR@{}", getConnectionName());
        }
    }

    @Override
    public AvrResponse sendCommand(AvrCommand ipControlCommand) throws TimeoutException {
        return sendCommand(ipControlCommand, true);
    }

    /**
     * Send the given command and wait for the response if a response is expected and waitForResponse is true. Else
     * return a response of type {@link ResponseType#NONE}.
     *
     * If a Timeout occurs, a {@link TimeoutException} is thrown.
     *
     * This implementation is synchronized to serialize requests and avoid response collisions.
     *
     * @see AvrConnection#sendCommand(AvrCommand)
     **/
    protected synchronized AvrResponse sendCommand(AvrCommand ipControlCommand, boolean waitForResponse)
            throws TimeoutException {
        // By default, return null (the request has not been sent)
        AvrResponse response = null;
        if (connect()) {
            String command = ipControlCommand.getCommand();
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Sending {} bytes: {}", command.length(),
                            DatatypeConverter.printHexBinary(command.getBytes()));
                }

                // Critical section to avoid to miss the response
                synchronized (responseLock) {
                    outputStream.writeBytes(command);
                    outputStream.flush();
                    logger.debug("Command sent to AVR @{}: {}", getConnectionName(), command);

                    if (ipControlCommand.isResponseExpected() && waitForResponse) {
                        // If a response is expected, wait until the response is received.
                        sentCommand = ipControlCommand;
                        try {
                            responseLock.wait(RESPONSE_TIMEOUT);
                            // If receivedResponse is null, it is a timeout
                            if (receivedResponse == null) {
                                throw new TimeoutException("No response received after " + RESPONSE_TIMEOUT
                                        + " ms for the command " + command);
                            } else {
                                response = receivedResponse;
                            }
                        } catch (InterruptedException e) {
                            // If the thread is interrupted, do nothing special, just end the treatment.
                        } finally {
                            // Reset the context for next command to send.
                            receivedResponse = null;
                            sentCommand = null;
                        }
                    } else {
                        // NONE response if no response is expected or we do not want to wait for it.
                        response = Response.getReponseNone(ipControlCommand.getZone());
                    }
                }
            } catch (IOException ioException) {
                logger.error("Error occurred when sending command", ioException);
                // If an error occurs, close the connection
                close();
            }
        }

        if (response != null) {
            logger.debug("Response received from AVR@{}: {}", getConnectionName(), response);
        } else {
            notifyDisconnection(new IOException("Connection failed."));
        }

        return response;
    }

    @Override
    public void sendBurstCommands(AvrCommand command, int count) {
        List<AvrCommand> commands = Stream.generate(() -> command).limit(count).collect(Collectors.toList());
        sendBurstCommands(commands);
    }

    @Override
    public void sendBurstCommands(List<AvrCommand> commands) {
        for (AvrCommand avrCommand : commands) {
            try {
                // Send the commands. Wait for responses only if the burst mode is disabled.
                sendCommand(avrCommand, !isBurstModeEnabled);

                if (isBurstModeEnabled && burstMessageDelay > 0) {
                    Thread.sleep(burstMessageDelay);
                }
            } catch (TimeoutException e) {
                // Should never happen since we do not wait for a response.
                logger.error("Timeout during a burst send. It is probably a bug. Thank you to report it.", e);
            } catch (InterruptedException e) {
                // Nothing to do, just stop to send.
            }
        }
    }

    @Override
    public void setBurstMessageDelay(int burstMessageDelay) {
        this.burstMessageDelay = burstMessageDelay;
    }

    @Override
    public void setBurstModeEnabled(boolean burstModeEnabled) {
        this.isBurstModeEnabled = burstModeEnabled;
    }

    /**
     * Notify the listeners about the disconnection with the given cause.
     *
     * @param cause
     */
    private void notifyDisconnection(IOException cause) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (CollectionUtils.isNotEmpty(connectionListeners)) {
                    for (AvrConnectionListener pioneerAvrDisconnectionListener : connectionListeners) {
                        pioneerAvrDisconnectionListener.onDisconnection(StreamAvrConnection.this, cause);
                    }
                } else {
                    logger.warn("The AVR @{} is disconnected.", getConnectionName(), cause);
                }
            }
        });
    }

    /**
     * Notify the listeners about the connection.
     */
    private void notifyConnection() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (CollectionUtils.isNotEmpty(connectionListeners)) {
                    for (AvrConnectionListener connectionListener : connectionListeners) {
                        connectionListener.onConnection(StreamAvrConnection.this);
                    }
                } else {
                    logger.info("The AVR @{} is connected.", getConnectionName());
                }
            }
        });
    }

    /**
     * Read incoming data from the AVR and notify listeners for dataReceived and disconnection.
     *
     * @author Antoine Besnard
     *
     */
    private class IpControlInputStreamReader implements Runnable {

        private BufferedReader bufferedReader = null;

        private volatile boolean stopReader;

        /**
         * Construct a reader that read the given inputStream
         *
         * @param ipControlSocket
         * @throws IOException
         */
        public IpControlInputStreamReader(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            try {
                while (!stopReader && !Thread.currentThread().isInterrupted()) {
                    String receivedData = null;
                    try {
                        receivedData = bufferedReader.readLine();
                    } catch (SocketTimeoutException e) {
                        // Do nothing. The timeout is configured to not infinitely block on read and to allow the thread
                        // to check if it has to stop.
                    }

                    if (receivedData != null && !receivedData.trim().isEmpty()) {
                        logger.debug("Data received from AVR @{}: {}", getConnectionName(), receivedData);

                        try {
                            // Parse the message
                            AvrResponse message = RequestResponseFactory.getIpControlResponse(receivedData);

                            synchronized (responseLock) {
                                // It is a response if a command has been sent, the message type is the same as the
                                // expected response type and the response is for the requested zone.
                                boolean isError = message.getResponseType().isError();
                                boolean isSameType = sentCommand != null && message.getResponseType()
                                        .equals(sentCommand.getCommandType().getExpectedResponse());
                                boolean isSameZone = sentCommand != null && sentCommand.getZone() == message.getZone();
                                boolean isResponse = isError || (isSameType && isSameZone);

                                if (isResponse) {
                                    // If it is a response, save it and notify the request sender.
                                    receivedResponse = message;
                                    responseLock.notify();
                                } else {
                                    // If it is not a response to a request, then it is a notification
                                    // => Notify the listeners
                                    AvrNotificationEvent event = new AvrNotificationEvent(StreamAvrConnection.this,
                                            message);
                                    notifyListeners(event);
                                }
                            }
                        } catch (AvrConnectionException e) {
                            logger.debug("Message dropped. Unknown reponseType: {}", e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                notifyDisconnection(e);
            } catch (Exception e) {
                logger.error("Error during AVR @{} inputStream reading.", getConnectionName(), e);
            }
        }
    }

}
