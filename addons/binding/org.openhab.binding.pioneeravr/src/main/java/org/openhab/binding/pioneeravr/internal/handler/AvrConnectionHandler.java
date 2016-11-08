/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;
import org.openhab.binding.pioneeravr.internal.protocol.ParameterizedCommand.ParameterizedCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.RequestResponseFactory;
import org.openhab.binding.pioneeravr.internal.protocol.SimpleCommand.SimpleCommandType;
import org.openhab.binding.pioneeravr.protocol.AvrCommand;
import org.openhab.binding.pioneeravr.protocol.AvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;
import org.openhab.binding.pioneeravr.protocol.CommandTypeNotSupportedException;
import org.openhab.binding.pioneeravr.protocol.utils.VolumeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles an AvrConnection. Translates an openHAB command to an AVR command and conversely.
 *
 * @author Antoine Besnard
 */
public class AvrConnectionHandler {

    private final Logger logger = LoggerFactory.getLogger(AvrConnectionHandler.class);

    private AvrConnection avrConnection;
    private VolumeConverter volumeConverter;
    private ReentrantLock setVolumeLock;

    private boolean isSetVolumeCommandEnabled;

    public AvrConnectionHandler(AvrConnection avrConnection, VolumeConverter volumeConverter,
            ModelProperties modelProperties) {
        this.avrConnection = avrConnection;
        this.volumeConverter = volumeConverter;
        this.setVolumeLock = new ReentrantLock(true);
        this.isSetVolumeCommandEnabled = modelProperties.isSetVolumeCommandEnabled();

        avrConnection.setBurstModeEnabled(modelProperties.isBurstModeEnabled());
        avrConnection.setBurstMessageDelay(modelProperties.getBurstMessageDelay());
    }

    /**
     * Send a power state query to the AVR
     *
     * @param zone
     * @return the response or null if the request has not been sent or the response has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendPowerQuery(int zone) throws TimeoutException {
        return avrConnection
                .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_QUERY, zone));
    }

    /**
     * Send a volume level query to the AVR.
     *
     * @param zone
     * @return the response or null if the request has not been sent or the response has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendVolumeQuery(int zone) throws TimeoutException {
        return avrConnection
                .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_QUERY, zone));
    }

    /**
     * Send a mute state query to the AVR
     *
     * @param zone
     * @return the response or null if the request has not been sent or the response has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendMuteQuery(int zone) throws TimeoutException {
        return avrConnection
                .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_QUERY, zone));
    }

    /**
     * Send a source input state query to the AVR
     *
     * @param zone
     * @return the response or null if the request has not been sent or the response has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendSourceInputQuery(int zone) throws TimeoutException {
        return avrConnection
                .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_QUERY, zone));
    }

    /**
     * Send a display query command to the AVR
     *
     * @return
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendDisplayQuery() throws TimeoutException {
        return avrConnection
                .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.DISPLAY_QUERY, 1));
    }

    /**
     * Send a power command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return a None response or null if the request has not been sent.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendPowerCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, TimeoutException {
        AvrResponse response = null;

        if (command == OnOffType.ON) {
            // Send the first Power ON command.
            avrConnection.sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_ON, zone));

            // According to the Pioneer Specs, the first request only wakeup the
            // AVR CPU, the second one Power ON the AVR. Still according to the Pioneer Specs, the second
            // request has to be delayed of 100 ms.
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            // Then send the second request.
            response = avrConnection
                    .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_ON, zone));
        } else if (command == OnOffType.OFF) {
            avrConnection.sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.POWER_OFF, zone));
        } else if (command == RefreshType.REFRESH) {
            response = sendPowerQuery(zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return response;
    }

    /**
     * Send a volume command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return the response or null if the request has not been sent or has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendVolumeCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, TimeoutException {
        AvrResponse response = null;

        // The OnOffType for volume is equal to the Mute command
        if (command instanceof OnOffType) {
            response = sendMuteCommand(command, zone);
        } else if (command == IncreaseDecreaseType.DECREASE) {
            AvrCommand commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_DOWN, zone);
            response = avrConnection.sendCommand(commandToSend);
        } else if (command == IncreaseDecreaseType.INCREASE) {
            AvrCommand commandToSend = RequestResponseFactory.getIpControlCommand(SimpleCommandType.VOLUME_UP, zone);
            response = avrConnection.sendCommand(commandToSend);
        } else if (command instanceof PercentType) {
            String ipControlVolume = volumeConverter
                    .convertFromPercentToIpControlVolume(((PercentType) command).doubleValue(), zone);
            response = sendSetVolume(ipControlVolume, zone);
            logger.debug("Set volume to {} %", ((PercentType) command).doubleValue());
        } else if (command instanceof DecimalType) {
            String ipControlVolume = volumeConverter
                    .convertFromDbToIpControlVolume(((DecimalType) command).doubleValue(), zone);
            response = sendSetVolume(ipControlVolume, zone);
            logger.debug("Set volume to {} dB", ((DecimalType) command).doubleValue());
        } else if (command == RefreshType.REFRESH) {
            response = sendVolumeQuery(zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }
        return response;
    }

    /**
     * Set the given volume on the AVR. Use directly the "Set Volume" command if it is supported, else send multiple
     * "Volume Up" or "Volume Down" commands until the request volume is set.
     *
     * @param requestedIpControlVolume
     * @param zone
     * @return The last response of the process.
     * @throws TimeoutException
     */
    protected AvrResponse sendSetVolume(String requestedIpControlVolume, int zone) throws TimeoutException {
        AvrResponse response = null;

        if (isSetVolumeCommandEnabled) {
            // If the "Set Volume" command is supported, use it.
            response = avrConnection
                    .sendCommand(RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.VOLUME_SET, zone)
                            .setParameter(requestedIpControlVolume));
        } else {
            // If the "Set Volume" command is not enabled, send as many "Volume Up" or "Volume Down" commands as
            // needed.
            response = sendSetVolumeBurstMode(requestedIpControlVolume, zone);
        }

        return response;
    }

    /**
     * Set the given volume on the AVR using a burst of Volume UP/DOWN commands.
     *
     * @param requestedIpControlVolume
     * @param zone
     * @return
     * @throws TimeoutException
     */
    protected AvrResponse sendSetVolumeBurstMode(String requestedIpControlVolume, int zone) throws TimeoutException {
        AvrResponse response = null;

        // Lock (with fairness) to serialize requests.
        setVolumeLock.lock();

        try {
            // Get the current volume from the AVR.
            response = sendVolumeQuery(zone);

            // If the AVR is online.
            if (response != null) {
                // Compute the number of requests to send (each request increments/decrements the volume of
                // volumeStepDb)
                Integer currentIpControlVolumeLevel = Integer.parseInt(response.getParameterValue());
                Integer requestedIpControlVolumeLevel = Integer.parseInt(requestedIpControlVolume);
                Integer ipControlVolumeDelta = requestedIpControlVolumeLevel - currentIpControlVolumeLevel;
                logger.debug(
                        "currentIpControlVolumeLevel: {}, requestedIpControlVolumeLevel: {}, ipControlVolumeDelta: {}",
                        currentIpControlVolumeLevel, requestedIpControlVolumeLevel, ipControlVolumeDelta);

                // Define the command to send (UP or DOWN)
                SimpleCommandType commandType = ipControlVolumeDelta < 0 ? SimpleCommandType.VOLUME_DOWN
                        : SimpleCommandType.VOLUME_UP;

                // Set the number of commands to send.
                Integer nbCommandsToSend = Math.abs(ipControlVolumeDelta);
                logger.debug("nbCommandsToSend: {}", nbCommandsToSend);

                // Send the commands in burst.
                avrConnection.sendBurstCommands(RequestResponseFactory.getIpControlCommand(commandType, zone),
                        nbCommandsToSend);
            }
        } finally {
            // Unlock to release waiting threads.
            setVolumeLock.unlock();
        }

        return response;
    }

    /**
     * Send a source input selection command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return the response or null if the request has not been sent or has timed out.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendInputSourceCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, TimeoutException {
        AvrResponse response = null;

        if (command == IncreaseDecreaseType.INCREASE) {
            response = avrConnection.sendCommand(
                    RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_CHANGE_CYCLIC, zone));
        } else if (command == IncreaseDecreaseType.DECREASE) {
            response = avrConnection.sendCommand(
                    RequestResponseFactory.getIpControlCommand(SimpleCommandType.INPUT_CHANGE_REVERSE, zone));
        } else if (command instanceof StringType) {
            String inputSourceValue = ((StringType) command).toString();
            response = avrConnection.sendCommand(
                    RequestResponseFactory.getIpControlCommand(ParameterizedCommandType.INPUT_CHANNEL_SET, zone)
                            .setParameter(inputSourceValue));
        } else if (command == RefreshType.REFRESH) {
            response = sendSourceInputQuery(zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return response;
    }

    /**
     * Send a mute command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return a None response or null if the request has not been sent.
     * @throws TimeoutException if no response is received.
     */
    public AvrResponse sendMuteCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, TimeoutException {
        AvrResponse response = null;

        if (command == OnOffType.ON) {
            response = avrConnection
                    .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_ON, zone));
        } else if (command == OnOffType.OFF) {
            response = avrConnection
                    .sendCommand(RequestResponseFactory.getIpControlCommand(SimpleCommandType.MUTE_OFF, zone));
        } else if (command == RefreshType.REFRESH) {
            response = sendMuteQuery(zone);
        } else {
            throw new CommandTypeNotSupportedException("Command type not supported.");
        }

        return response;
    }

    /**
     * Close the underlying connection.
     */
    public void close() {
        avrConnection.close();
    }

    /**
     * Return the name of the underlying connection.
     *
     * @return
     */
    public String getConnectionName() {
        return avrConnection.getConnectionName();
    }
}
