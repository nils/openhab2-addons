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
package org.openhab.binding.pjlinkdevice.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PJLinkDeviceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDeviceBindingConstants {

    private static final String BINDING_ID = "pjLinkDevice";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PJLINK = new ThingTypeUID(BINDING_ID, "pjLinkDevice");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "powerChannel";
    public static final String CHANNEL_INPUT = "inputChannel";
    public static final String CHANNEL_INPUT_DYNAMIC = "inputChannelDynamic";
    public static final String CHANNEL_AUDIO_MUTE = "audioMuteChannel";
    public static final String CHANNEL_VIDEO_MUTE = "videoMuteChannel";

    public static final int DEFAULT_PORT = 4352;
    public static final int DEFAULT_SCAN_TIMEOUT = 60;

    // configuration
    public static final String PARAMETER_HOSTNAME = "ipAddress";
    public static final String PARAMETER_PORT = "tcpPort";
    public static final String PARAMETER_PASSWORD = "adminPassword";
    public static final String PARAMETER_REFRESH = "refresh";
    public static final long DISCOVERY_RESULT_TTL = TimeUnit.MINUTES.toSeconds(10);

    // information disclosed by device
    public static final String PARAMETER_CLASS = "disclosedPjLinkClass";
    public static final String PARAMETER_NAME = "disclosedName";
    public static final String PARAMETER_MANUFACTURER = "disclosedManufacturer";
    public static final String PARAMETER_MODEL = "disclosedModel";
    public static final String PARAMETER_ERROR_STATUS = "disclosedErrorStatus";
    public static final String PARAMETER_LAMP_HOURS = "disclosedLampHours";
    public static final String PARAMETER_OTHER_INFORMATION = "disclosedOtherInformation";

    // calculated properties
    public static final String PARAMETER_AUTHENTICATION_REQUIRED = "authenticationRequired";
    public static final String PARAMETER_COMBINED_ID = "disclosedIdentificationCombined";
}
