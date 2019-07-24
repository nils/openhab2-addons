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
package org.openhab.binding.pjlinkdevice.internal.device.command.input;

import org.openhab.binding.pjlinkdevice.internal.device.command.Request;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class InputInstructionRequest implements Request {

    protected InputInstructionCommand command;

    public InputInstructionRequest(InputInstructionCommand command) {
        this.command = command;
    }

    @Override
    public String getRequestString() {
        return "%1INPT " + this.command.target.getPJLinkRepresentation();
    }

}
