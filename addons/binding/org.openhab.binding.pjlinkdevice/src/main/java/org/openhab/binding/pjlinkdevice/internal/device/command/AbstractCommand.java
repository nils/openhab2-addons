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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.io.IOException;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;

/**
 * @author Nils Schnabel - Initial contribution
 */
public abstract class AbstractCommand<RequestType extends Request, ResponseType extends Response>
        implements Command<ResponseType> {
    private PJLinkDevice pjLinkDevice = null;

    public AbstractCommand(PJLinkDevice pjLinkDevice) {
        this.pjLinkDevice = pjLinkDevice;
    }

    public PJLinkDevice getDevice() {
        return this.pjLinkDevice;
    }

    protected abstract RequestType createRequest();

    protected abstract ResponseType parseResponse(String response) throws ResponseException;

    @Override
    public ResponseType execute() throws ResponseException, IOException, AuthenticationException {
        RequestType request = createRequest();
        String responseString = this.pjLinkDevice.execute(request.getRequestString() + "\r");
        if (responseString == null) {
            throw new ResponseException("Response is null");
        }
        if (responseString.equals("PJLINK ERRA")) {
            throw new AuthenticationException("Authentication error, wrong password provided?");
        }
        return this.parseResponse(responseString);
    }
}
