/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command.power;

import java.util.HashMap;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class PowerInstructionCommand extends AbstractCommand<PowerInstructionRequest, PowerInstructionResponse> {

    public enum PowerInstructionState {
        ON,
        OFF;

        public String getPJLinkRepresentation() {
            final HashMap<PowerInstructionState, String> texts = new HashMap<PowerInstructionState, String>();
            texts.put(ON, "1");
            texts.put(OFF, "0");
            return texts.get(this);
        }
    }

    protected PowerInstructionState target;

    public PowerInstructionCommand(PJLinkDevice pjLinkDevice, PowerInstructionState target) {
        super(pjLinkDevice);
        this.target = target;
    }

    @Override
    public PowerInstructionRequest createRequest() {
        return new PowerInstructionRequest(this);
    }

    @Override
    public PowerInstructionResponse parseResponse(String response) throws ResponseException {
        PowerInstructionResponse result = new PowerInstructionResponse();
        result.parse(response);
        return result;
    }
}
