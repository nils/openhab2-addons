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
package org.openhab.binding.pioneeravr.internal.protocol;

import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrCommand;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrResponse;

/**
 * A simple command without parameters.
 *
 * @author Antoine Besnard - Initial contribution
 * @author Leroy Foerster - Listening Mode, Playing Listening Mode
 */
public class SimpleCommand implements AvrCommand {

    /**
     * List of the simple command types.
     */
    public enum SimpleCommandType implements AvrCommand.CommandType {

        POWER_ON(ResponseType.NONE, "PO", "APO", "BPO", "ZEO"),
        POWER_OFF(ResponseType.NONE, "PF", "APF", "BPF", "ZEF"),
        POWER_QUERY(ResponseType.POWER_STATE, "?P", "?AP", "?BP", "?ZEP"),
        VOLUME_UP(ResponseType.VOLUME_LEVEL, "VU", "ZU", "YU", "HZU"),
        VOLUME_DOWN(ResponseType.VOLUME_LEVEL, "VD", "ZD", "YD", "HZD"),
        VOLUME_QUERY(ResponseType.VOLUME_LEVEL, "?V", "?ZV", "?YV", "?HZV"),
        MUTE_ON(ResponseType.MUTE_STATE, "MO", "Z2MO", "Z3MO", "HZMO"),
        MUTE_OFF(ResponseType.MUTE_STATE, "MF", "Z2MF", "Z3MF", "HZMF"),
        MUTE_QUERY(ResponseType.MUTE_STATE, "?M", "?Z2M", "?Z3M", "?HZM"),
        INPUT_CHANGE_CYCLIC(ResponseType.INPUT_SOURCE_CHANNEL, "FU"),
        INPUT_CHANGE_REVERSE(ResponseType.INPUT_SOURCE_CHANNEL, "FD"),
        LISTENING_MODE_CHANGE_CYCLIC(ResponseType.NONE, "0010SR"),
        LISTENING_MODE_QUERY(ResponseType.NONE, "?S"),
        INPUT_QUERY(ResponseType.INPUT_SOURCE_CHANNEL, "?F", "?ZS", "?ZT", "?ZEA");

        private String zoneCommands[];
        private ResponseType expectedResponse;

        private SimpleCommandType(ResponseType expectedResponse, String... command) {
            this.expectedResponse = expectedResponse;
            this.zoneCommands = command;
        }

        @Override
        public String getCommand(int zone) {
            return zoneCommands[zone - 1];
        }

        @Override
        public ResponseType getExpectedResponse() {
            return this.expectedResponse;
        }
    }

    private CommandType commandType;
    private int zone;
    private AvrResponse response;

    public SimpleCommand(CommandType commandType, int zone) {
        this.commandType = commandType;
        this.zone = zone;
    }

    @Override
    public String getCommand() {
        return commandType.getCommand(zone) + "\r";
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public int getZone() {
        return zone;
    }

    @Override
    public boolean isResponse(AvrResponse message) {
        boolean isError = message.getResponseType().isError();
        boolean isSameType = message.getResponseType().equals(this.getCommandType().getExpectedResponse());
        boolean isSameZone = this.getZone() == message.getZone();
        return isError || (isSameType && isSameZone);
    }

    @Override
    public void setResponse(AvrResponse message) {
        this.response = message;
    }

    @Override
    public AvrResponse getResponse() {
        return response;
    }

    @Override
    public boolean isResponseExpected() {
        return this.getCommandType().getExpectedResponse() != ResponseType.NONE;
    }

}
