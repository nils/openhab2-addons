/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.models.properties;

import java.util.Collection;

/**
 * Defines the properties for a specific AVR model.
 *
 * @author Antoine Besnard
 *
 */
public interface ModelProperties {

    // Volume Format / MAX_IP / Min_DB for Zones
    // Zone1 Command ***VL 000 to 185 (-80.0dB - +12.0dB - 1step = 0.5dB)
    // Zone2 Command **ZV 00 to 81 (-80.0dB - + 0.0dB - 1step = 1.0dB)
    // Zone3 Command **YV 00 to 81 (-80.0dB - + 0.0dB - 1step = 1.0dB)
    // HDZone Command **YV 00 to 81 (-80.0dB - + 0.0dB - 1step = 1.0dB)
    public static final double[] DEFAULT_MIN_DB = { -80, -80, -80, -80 };
    public static final double[] DEFAULT_MAX_DB = { 12, 0, 0, 0 };
    public static final double[] DEFAULT_STEP_DB = { 0.5, 1, 1, 1 };

    // In burst mode, wait for 10 ms between two messages.
    public static final int DEFAULT_BURST_MESSAGE_DELAY = 10;

    String getModelName();

    String getModelDescription();

    int getNbZones();

    double getVolumeMinDb(int zone);

    double getVolumeMaxDb(int zone);

    double getVolumeStepDb(int zone);

    Collection<InputSource> getInputSources(int zone);

    /**
     * Enable/Disable the use of the SetVolume command.
     *
     * @return
     */
    boolean isSetVolumeCommandEnabled();

    /**
     * Allow or not to control the AVR through the dB channels.
     *
     * @return
     */
    boolean areDbChannelsEnabled();

    /**
     * Return the delay (in milliseconds) between two messages send to the AVR in burst mode.
     * (mainly used to set the volume on the AVR when the SetVolume command is not enabled).
     *
     * If the delay is too short, some messages may be missed by the AVR and the volume level will not be accurate. If
     * too high, the volume will be longer to
     * adjust to the requested value.
     *
     * @return
     */
    int getBurstMessageDelay();

    /**
     * Enable/Disable the burst mode. If disabled, the burst commands are sent in "slow" mode. Should only be disabled
     * in last resort.
     *
     * @return
     */
    boolean isBurstModeEnabled();

    enum InputSource {
        DVD("DVD", "04"),
        BD("BD", "25"),
        TV_SAT("TV/SAT", "05"),
        DVR_BDR("DVR/BDR", "15"),
        VIDEO_1("VIDEO 1", "10"),
        VIDEO_2("VIDEO 2", "14"),
        HDMI_1("HDMI 1", "19"),
        HDMI_2("HDMI 2", "20"),
        HDMI_3("HDMI 3", "21"),
        HDMI_4("HDMI 4", "22"),
        HDMI_5("HDMI 5", "23"),
        HOME_MEDIA_GALLERY("HOME MEDIA GALLERY", "26"),
        IPOD_USB("iPod/USB", "17"),
        XM_RADIO("XM RADIO", "18"),
        CD("CD", "01"),
        CD_R_TAPE("CD-R/TAPE", "03"),
        TUNER("TUNER", "02"),
        PHONO("PHONO", "00"),
        MULTI_CH_IN("MULTI CH IN", "12"),
        ADAPTER_PORT("ADAPTER PORT", "33"),
        SIRIUS("SIRIUS", "27");

        private String name;
        private String value;

        private InputSource(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

}
