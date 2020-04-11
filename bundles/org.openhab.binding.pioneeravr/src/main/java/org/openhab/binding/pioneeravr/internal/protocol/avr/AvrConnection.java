/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pioneeravr.internal.protocol.avr;

import java.io.IOException;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrDisconnectionListener;
import org.openhab.binding.pioneeravr.internal.protocol.event.AvrUpdateListener;

/**
 * Represent a connection to a remote Pioneer AVR.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public interface AvrConnection {

    /**
     * Add an update listener. It is notified when an update is received from the AVR.
     *
     * @param listener
     */
    public void addUpdateListener(AvrUpdateListener listener);

    /**
     * Add a disconnection listener. It is notified when the AVR is disconnected.
     *
     * @param listener
     */
    public void addDisconnectionListener(AvrDisconnectionListener listener);

    /**
     * Connect to the receiver. Return true if the connection has succeeded or if already connected.
     *
     **/
    public boolean connect();

    /**
     * Return true if this manager is connected to the AVR.
     *
     * @return
     */
    public boolean isConnected();

    /**
     * Closes the connection.
     **/
    public void close();

    /**
     * Send a power state query to the AVR
     *
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendPowerQuery(int zone) throws IOException;

    /**
     * Send a volume level query to the AVR
     *
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendVolumeQuery(int zone) throws IOException;

    /**
     * Send a mute state query to the AVR
     *
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendMuteQuery(int zone) throws IOException;

    /**
     * Send a source input state query to the AVR
     *
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendSourceInputQuery(int zone) throws IOException;

    /**
     * Send a listening mode state query to the AVR
     *
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendListeningModeQuery(int zone) throws IOException;

    /**
     * Send a power command ot the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendPowerCommand(Command command, int zone) throws CommandTypeNotSupportedException, IOException;

    /**
     * Send a volume command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendVolumeCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, IOException;

    /**
     * Send a source input selection command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendInputSourceCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, IOException;

    /**
     * Send a listening mode selection command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendListeningModeCommand(Command command, int zone)
            throws CommandTypeNotSupportedException, IOException;

    /**
     * Send a mute command to the AVR based on the openHAB command
     *
     * @param command
     * @param zone
     * @return
     * @throws IOException
     */
    public AvrResponse sendMuteCommand(Command command, int zone) throws CommandTypeNotSupportedException, IOException;

    /**
     * Return the connection name
     *
     * @return
     */
    public String getConnectionName();

}
