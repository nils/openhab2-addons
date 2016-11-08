/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openhab.binding.pioneeravr.protocol.event.AvrConnectionListener;
import org.openhab.binding.pioneeravr.protocol.event.AvrNotificationListener;

/**
 * Represent a connection to a remote Pioneer AVR.
 *
 * @author Antoine Besnard - Initial contribution
 */
public interface AvrConnection {

    /**
     * Add a notification listener. It is notified when a notification is received from the AVR.
     *
     * @param listener
     */
    void addNotificationListener(AvrNotificationListener listener);

    /**
     * Add a disconnection listener. It is notified when the AVR is disconnected.
     *
     * @param listener
     */
    void addConnectionListener(AvrConnectionListener listener);

    /**
     * Connect to the receiver. Return true if the connection has succeeded or if already connected.
     *
     **/
    boolean connect();

    /**
     * Return true if this manager is connected to the AVR.
     *
     * @return
     */
    boolean isConnected();

    /**
     * Closes the connection.
     **/
    void close();

    /**
     * Sends the command to the receiver.
     *
     * If a disconnection is detected during the send, the listeners are notified about the disconnection and null is
     * returned.
     *
     * @param command the command to send.
     * @return the response to the command, or null if the request has not been sent.
     * @throws TimeoutException if no response to the command is received after the time out delay.
     **/
    AvrResponse sendCommand(AvrCommand command) throws TimeoutException;

    /**
     * Sends the commands to the receiver in burst. Does not wait for the last command response before sending the next
     * one. No response is expected from this burst.
     *
     * During the burst (until the last command is sent), all responses and notifications from the AVR will be
     * discarded. It means that if the AVR sends back responses for this commands and they are received after the last
     * command is sent, the responses will be discarded or considered as notifications (if the response type is a
     * notification).
     *
     * @param command the command to send.
     * @param the number of times the command will be sent.
     **/
    void sendBurstCommands(AvrCommand command, int count);

    /**
     * Sends the commands to the receiver in burst. Does not wait for the last command response before sending the next
     * one. No response is expected from this burst.
     *
     * During the burst (until the last command is sent), all responses and notifications from the AVR will be
     * discarded. It means that if the AVR sends back responses for this commands and they are received after the last
     * command is sent, the responses will be discarded or considered as notifications (if the response type is a
     * notification).
     *
     * @param command the commands to send.
     **/
    void sendBurstCommands(List<AvrCommand> commands);

    /**
     * Return the connection name.
     *
     * @return
     */
    String getConnectionName();

    /**
     * Define the delay between two messages in burst send.
     *
     * @param burstMessageDelay
     */
    void setBurstMessageDelay(int burstMessageDelay);

    /**
     * Enable/Disable the burst mode. If disabled, the burst commands are sent in "slow" mode.
     *
     * @param burstModeEnabled
     */
    void setBurstModeEnabled(boolean burstModeEnabled);

}
