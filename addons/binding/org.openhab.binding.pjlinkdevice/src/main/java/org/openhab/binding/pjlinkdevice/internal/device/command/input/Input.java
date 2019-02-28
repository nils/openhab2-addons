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

import java.util.Arrays;

import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class Input {

    enum InputType {
        RGB("RGB", "1"),
        VIDEO("Video", "2"),
        DIGITAL("Digital", "3"),
        STORAGE("Storage", "4"),
        NETWORK("Network", "5");

        private String text;
        private String code;

        private InputType(String text, String code) {
            this.text = text;
            this.code = code;
        }

        public String getText() {
            return this.text;
        }

        public static InputType parseString(String value) throws ResponseException {
            for (InputType result : InputType.values()) {
                if (result.code.equals(value.substring(0, 1))) {
                    return result;
                }
            }

            throw new ResponseException("Unknown input channel type: " + value);
        }
    }

    public Input(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Input other = (Input) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    String value;

    public InputType getInputType() throws ResponseException {
        return InputType.parseString(this.value);
    }

    public String getInputNumber() throws ResponseException {
        return this.value.substring(1, 2);
    }

    public void validate() throws ResponseException {
        if (this.value.length() != 2) {
            throw new ResponseException("Illegal input description: " + value);
        }
        this.getInputType();
        if (!Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
                .contains(this.getInputNumber())) {
            throw new ResponseException("Illegal channel number: " + this.value.substring(1, 2));
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getPJLinkRepresentation() {
        return this.value;
    }

    public String getText() throws ResponseException {
        return this.getInputType().getText() + " " + this.getInputNumber();
    }
}
