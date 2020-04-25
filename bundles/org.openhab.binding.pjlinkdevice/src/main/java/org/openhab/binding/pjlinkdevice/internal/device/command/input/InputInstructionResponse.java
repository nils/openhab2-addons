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
package org.openhab.binding.pjlinkdevice.internal.device.command.input;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.command.AcknowledgeResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ErrorCode;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * The response part of {@link InputInstructionCommand}
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class InputInstructionResponse extends AcknowledgeResponse {
    private static final HashSet<ErrorCode> SPECIFIED_ERRORCODES = new HashSet<>(
            Arrays.asList(ErrorCode.OUT_OF_PARAMETER, ErrorCode.UNAVAILABLE_TIME, ErrorCode.DEVICE_FAILURE));

    public InputInstructionResponse(InputInstructionCommand command, String response) throws ResponseException {
        super(command, "INPT=", SPECIFIED_ERRORCODES, response);
    }
}
