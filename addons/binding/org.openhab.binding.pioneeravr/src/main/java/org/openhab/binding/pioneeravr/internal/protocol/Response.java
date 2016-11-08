/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;

/**
 * Represent an AVR response.
 *
 * @author Antoine Besnard
 *
 */
public class Response implements AvrResponse {

    /**
     * List of all supported responses coming from AVR.
     *
     * @author Antoine Besnard
     *
     */
    public enum ResponseType implements AvrResponse.AvrResponseType {
    // NONE is a fake response for requests that do not expect responses or when responses should be treated as
    // notifications only.
    NONE(false, "", ""),
    POWER_STATE(false, "[0-1]", "PWR", "APR", "BPR"),
    VOLUME_LEVEL(false, "[0-9]{2,3}", "VOL", "ZV", "YV"),
    MUTE_STATE(false, "[0-1]", "MUT", "Z2MUT", "Z3MUT"),
    INPUT_SOURCE_CHANNEL(false, "[0-9]{2}", "FN", "Z2F", "Z3F"),
    DISPLAY_INFORMATION(false, "[0-9a-fA-F]{30}", "FL"),
    UNKNOWN_COMMAND(true, "", "E4"),
    UNKNOWN_PARAMETER(true, "", "E6"),
    GENERIC_ERROR(true, "", "R");

        private String[] responsePrefixZone;

        private String parameterPattern;

        private Pattern[] matchPatternZone;

        private boolean isError;

        private ResponseType(boolean isError, String parameterPattern, String... responsePrefixZone) {
            this.isError = isError;
            this.responsePrefixZone = responsePrefixZone;
            this.parameterPattern = parameterPattern;

            matchPatternZone = new Pattern[responsePrefixZone.length];

            for (int zoneIndex = 0; zoneIndex < responsePrefixZone.length; zoneIndex++) {
                String responsePrefix = responsePrefixZone[zoneIndex];
                matchPatternZone[zoneIndex] = Pattern.compile(responsePrefix + "("
                        + (StringUtils.isNotEmpty(parameterPattern) ? parameterPattern : "") + ")");
            }
        }

        @Override
        public String getResponsePrefix(int zone) {
            return responsePrefixZone[zone - 1];
        }

        @Override
        public boolean hasParameter() {
            return StringUtils.isNotEmpty(parameterPattern);
        }

        @Override
        public String getParameterPattern() {
            return parameterPattern;
        }

        @Override
        public boolean isError() {
            return isError;
        }

        @Override
        public Integer match(String responseData) {
            Integer zone = null;
            // Check the response data against all zone prefixes.
            for (int zoneIndex = 0; zoneIndex < matchPatternZone.length; zoneIndex++) {
                if (matchPatternZone[zoneIndex].matcher(responseData).matches()) {
                    zone = zoneIndex + 1;
                    break;
                }
            }

            return zone;
        }

        /**
         * Return the parameter value of the given responseData.
         *
         * @param responseData
         * @return
         */
        @Override
        public String parseParameter(String responseData) {
            String result = null;
            // Check the response data against all zone prefixes.
            for (int zoneIndex = 0; zoneIndex < matchPatternZone.length; zoneIndex++) {
                Matcher matcher = matchPatternZone[zoneIndex].matcher(responseData);
                if (matcher.find()) {
                    result = matcher.group(1);
                    break;
                }
            }
            return result;
        }
    }

    private ResponseType responseType;

    private Integer zone;

    private String parameter;

    public static Response getReponseNone(int zone) {
        return new Response(ResponseType.NONE, zone);
    }

    private Response(ResponseType responseType, int zone) {
        this(responseType, zone, null);
    }

    protected Response(ResponseType responseType, int zone, String parameter) {
        this.responseType = responseType;
        this.zone = zone;
        this.parameter = parameter;
    }

    public Response(String responseData) throws AvrConnectionException {
        if (StringUtils.isEmpty(responseData)) {
            throw new AvrConnectionException("responseData is empty. Cannot parse the response.");
        }

        parseResponse(responseData);

        if (this.responseType == null) {
            throw new AvrConnectionException("Cannot find the responseType of the responseData " + responseData);
        }

        if (this.responseType.hasParameter()) {
            this.parameter = this.responseType.parseParameter(responseData);
        }
    }

    /**
     * Parse the given response data and fill the
     *
     * @param responseData
     * @return
     */
    private void parseResponse(String responseData) {
        for (ResponseType responseType : ResponseType.values()) {
            zone = responseType.match(responseData);
            if (zone != null) {
                this.responseType = responseType;
                break;
            }
        }
    }

    @Override
    public ResponseType getResponseType() {
        return this.responseType;
    }

    @Override
    public String getParameterValue() {
        return parameter;
    }

    @Override
    public boolean hasParameter() {
        return responseType.hasParameter();
    }

    @Override
    public Integer getZone() {
        return this.zone;
    }

    @Override
    public String toString() {
        return "Response [responseType=" + responseType + ", zone=" + zone + ", parameter=" + parameter + "]";
    }

}
