/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;
import org.openhab.binding.pioneeravr.protocol.AvrCommand;

/**
 * A simple command without parameters.
 *
 * @author Antoine Besnard
 *
 */
public class SimpleCommand implements AvrCommand {

    /**
     * List of the simple command types.
     *
     * @author Antoine Besnard
     *
     */
    public enum SimpleCommandType implements AvrCommand.CommandType {

    POWER_ON(ResponseType.NONE, "PO", "APO", "BPO"),
    POWER_OFF(ResponseType.NONE, "PF", "APF", "BPF"),
    POWER_QUERY(ResponseType.POWER_STATE, "?P", "?AP", "?BP"),
    VOLUME_UP(ResponseType.VOLUME_LEVEL, "VU", "ZU", "YU"),
    VOLUME_DOWN(ResponseType.VOLUME_LEVEL, "VD", "ZD", "YD"),
    VOLUME_QUERY(ResponseType.VOLUME_LEVEL, "?V", "?ZV", "?YV"),
    MUTE_ON(ResponseType.MUTE_STATE, "MO", "Z2MO", "Z3MO"),
    MUTE_OFF(ResponseType.MUTE_STATE, "MF", "Z2MF", "Z3MF"),
    MUTE_QUERY(ResponseType.MUTE_STATE, "?M", "?Z2M", "?Z3M"),
    INPUT_CHANGE_CYCLIC(ResponseType.INPUT_SOURCE_CHANNEL, "FU"),
    INPUT_CHANGE_REVERSE(ResponseType.INPUT_SOURCE_CHANNEL, "FD"),
    INPUT_QUERY(ResponseType.INPUT_SOURCE_CHANNEL, "?F", "?ZS", "?ZT"),
    DISPLAY_QUERY(ResponseType.DISPLAY_INFORMATION, "?FL");

        private String zoneCommands[];
        private ResponseType expectedResponse;

        private SimpleCommandType(ResponseType expectedResponse, String... command) {
            this.zoneCommands = command;
            this.expectedResponse = expectedResponse;
        }

        @Override
        public String getCommand(int zone) {
            return zoneCommands[zone - 1];
        }

        @Override
        public ResponseType getExpectedResponse() {
            return expectedResponse;
        }
    }

    private CommandType commandType;
    private int zone;

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
    public boolean isResponseExpected() {
        return commandType.getExpectedResponse() != null && commandType.getExpectedResponse() != ResponseType.NONE;
    }

}
