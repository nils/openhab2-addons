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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Superclass for all response types that represent acknowledgement
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class AcknowledgeResponse extends PrefixedResponse<AcknowledgeResponseValue> {
    public AcknowledgeResponse(Command<? extends AcknowledgeResponse> command, String prefix, String response)
            throws ResponseException {
        this(command, prefix, null, response);
    }

    public AcknowledgeResponse(Command<? extends AcknowledgeResponse> command, String prefix,
            @Nullable HashSet<ErrorCode> specifiedErrorcodes, String response) throws ResponseException {
        super(command, prefix, specifiedErrorcodes, response);
    }

    @Override
    protected AcknowledgeResponseValue parseResponseWithoutPrefix(String responseWithoutPrefix)
            throws ResponseException {
        if (this.getCommand().getDevice().isIgnoringAcknowledgementResponses()) {
            return AcknowledgeResponseValue.OK;
        }

        return AcknowledgeResponseValue.getValueForCode(responseWithoutPrefix);
    }
}
