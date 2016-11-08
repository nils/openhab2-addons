/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr;

import java.util.regex.Pattern;

/**
 * The {@link PioneerAvrBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class PioneerAvrBindingConstants {

    public static final String BINDING_ID = "pioneeravr";

    public static final String PARAMETER_HOST = "ipAddress";
    public static final String PARAMETER_TCP_PORT = "tcpPort";
    public static final String PARAMETER_SERIAL_PORT = "serialPort";
    public static final String PARAMETER_USE_SERIAL = "useSerial";
    public static final String PARAMETER_USE_SET_VOLUME_COMMAND = "setVolumeCommandEnabled";
    public static final String PARAMETER_BURST_MESSAGE_DELAY = "burstMessageDelay";
    public static final String PARAMETER_USE_BURST_MODE = "setBurstModeEnabled";
    public static final String PARAMETER_NB_ZONES = "nbZones";
    public static final String PARAMETER_VOLUME_MIN_DB_ZONE1 = "volumeMinDbZone1";
    public static final String PARAMETER_VOLUME_MAX_DB_ZONE1 = "volumeMaxDbZone1";
    public static final String PARAMETER_VOLUME_STEP_DB_ZONE1 = "volumeStepDbZone1";
    public static final String PARAMETER_VOLUME_MIN_DB_ZONE2 = "volumeMinDbZone2";
    public static final String PARAMETER_VOLUME_MAX_DB_ZONE2 = "volumeMaxDbZone2";
    public static final String PARAMETER_VOLUME_STEP_DB_ZONE2 = "volumeStepDbZone2";
    public static final String PARAMETER_VOLUME_MIN_DB_ZONE3 = "volumeMinDbZone3";
    public static final String PARAMETER_VOLUME_MAX_DB_ZONE3 = "volumeMaxDbZone3";
    public static final String PARAMETER_VOLUME_STEP_DB_ZONE3 = "volumeStepDbZone3";
    public static final String PARAMETER_VOLUME_MIN_DB_ZONE4 = "volumeMinDbZone4";
    public static final String PARAMETER_VOLUME_MAX_DB_ZONE4 = "volumeMaxDbZone4";
    public static final String PARAMETER_VOLUME_STEP_DB_ZONE4 = "volumeStepDbZone4";

    public static final String USE_SET_VOLUME_AUTO = "auto";

    // List of all Channel ids
    public static final String CHANNEL_ID_POWER = "power";
    public static final String CHANNEL_ID_VOLUME_DIMMER = "volumeDimmer";
    public static final String CHANNEL_ID_VOLUME_DB = "volumeDb";
    public static final String CHANNEL_ID_MUTE = "mute";
    public static final String CHANNEL_ID_SET_INPUT_SOURCE = "setInputSource";
    public static final String CHANNEL_ID_DISPLAY_INFORMATION = "displayInformation";

    public static final String GROUP_CHANNEL_TYPE_ID_PATTERN = "%s-zone%s";

    public static final String GROUP_CHANNEL_ID_PATTERN = "zone%s#%s";
    public static final Pattern GROUP_CHANNEL_ID_ZONE_PATTERN = Pattern.compile("zone([1-4])#.*");

    // Used for Discovery service
    public static final String MANUFACTURER = "PIONEER";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

}
