/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.utils;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;

/**
 *
 * @author Antoine Besnard - Initial contribution
 */
public final class VolumeConverter {

    private ModelProperties modelProperties;

    public VolumeConverter(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

    public double getIpUnitsByDb(int zone) {
        // 1-step of Zone 1: 0.5dB => 2 units by dB
        // 1-step of other Zones: 1dB => 1 unit by dB
        return zone == 1 ? 2 : 1;
    }

    public double getStepDbVolume(int zone) {
        return modelProperties.getVolumeStepDb(zone);
    }

    public double getMaxDbVolume(int zone) {
        return modelProperties.getVolumeMaxDb(zone);
    }

    public double getMinDbVolume(int zone) {
        return modelProperties.getVolumeMinDb(zone);
    }

    /**
     * Return the double value of the volume from the value received in the IpControl response.
     *
     * @param ipControlVolume
     * @param zone
     * @return the volume in Db
     */
    public double convertFromIpControlVolumeToDb(String ipControlVolume, int zone) {
        validateZone(zone - 1);
        double volumePercent = convertFromIpControlVolumeToPercent(ipControlVolume, zone);
        return convertFromPercentToDb(volumePercent, zone);
    }

    /**
     * Return the string parameter to send to the AVR based on the given volume.
     *
     * @param volumeDb
     * @param zone
     * @return the volume for IpControlRequest
     */
    public String convertFromDbToIpControlVolume(double volumeDb, int zone) {
        validateZone(zone - 1);
        double volumePercent = convertFromDbToPercent(volumeDb, zone);
        return convertFromPercentToIpControlVolume(volumePercent, zone);
    }

    /**
     * Return the volume in percent from the volume in dB.
     *
     * @param volumeDb
     * @param zone
     * @return
     */
    public double convertFromDbToPercent(double volumeDb, int zone) {
        validateZone(zone - 1);
        double maxDb = modelProperties.getVolumeMaxDb(zone);
        double minDb = modelProperties.getVolumeMinDb(zone);
        double volumeRange = Math.abs(minDb) + Math.abs(maxDb);
        double volumeShift = Math.abs(minDb - volumeDb);

        return volumeShift * 100d / volumeRange;
    }

    /**
     * Return the volume in dB from the volume in percent.
     *
     * @param volumePercent
     * @param zone
     * @return
     */
    public double convertFromPercentToDb(double volumePercent, int zone) {
        validateZone(zone - 1);
        double maxDb = modelProperties.getVolumeMaxDb(zone);
        double minDb = modelProperties.getVolumeMinDb(zone);
        double volumeRange = Math.abs(minDb) + Math.abs(maxDb);
        double volumeShift = volumeRange * volumePercent / 100d;

        return minDb + volumeShift;
    }

    /**
     * Return the String parameter to send to the AVR based on the given percentage of the max volume level.
     *
     * @param volumePercent
     * @param zone
     * @return the volume for IpControlRequest
     */
    public String convertFromPercentToIpControlVolume(double volumePercent, int zone) {
        validateZone(zone - 1);
        double ipUnitsByDb = getIpUnitsByDb(zone);
        double maxDb = modelProperties.getVolumeMaxDb(zone);
        double minDb = modelProperties.getVolumeMinDb(zone);
        double maxIpControlVolume = ((maxDb - minDb) * ipUnitsByDb) + 1d;
        double ipControlVolume = volumePercent * maxIpControlVolume / 100;
        return formatIpControlVolume(ipControlVolume, zone);
    }

    /**
     * Return the percentage of the max volume level from the value received in the IpControl response.
     *
     * @param ipControlVolume
     * @param zone
     * @return the volume percentage
     */
    public double convertFromIpControlVolumeToPercent(String ipControlVolume, int zone) {
        validateZone(zone - 1);
        double ipControlVolumeInt = Double.parseDouble(ipControlVolume);
        double ipUnitsByDb = getIpUnitsByDb(zone);
        double maxDb = modelProperties.getVolumeMaxDb(zone);
        double minDb = modelProperties.getVolumeMinDb(zone);
        double maxIpControlVolume = ((maxDb - minDb) * ipUnitsByDb) + 1d;
        return (ipControlVolumeInt * 100d) / maxIpControlVolume;
    }

    /**
     * Return the formatter to use for the given zone.
     *
     * @return
     */
    public DecimalFormat getIpControlVolumeFormatter(int zone) {
        validateZone(zone);
        double maxDb = modelProperties.getVolumeMaxDb(zone);
        double minDb = modelProperties.getVolumeMinDb(zone);
        double stepDb = modelProperties.getVolumeStepDb(zone);
        int maxIpControlValue = Double.valueOf((maxDb - minDb) / stepDb).intValue();

        // Format is of type "000" where there are a number of 0 equals to the number of digits of the max value of the
        // ipControl volume for the given zone.
        DecimalFormat decimalFormat = new DecimalFormat(
                StringUtils.repeat("0", (int) (Math.log10(maxIpControlValue) + 1)));

        return decimalFormat;
    }

    /**
     * Format the given double value to an IpControl volume.
     *
     * @param ipControlVolume
     * @param zone
     * @return
     */
    public String formatIpControlVolume(double ipControlVolume, int zone) {
        validateZone(zone - 1);
        String result = getIpControlVolumeFormatter(zone).format(Math.round(ipControlVolume));
        return result;
    }

    private void validateZone(int zone) {
        if (zone < 0 || zone > modelProperties.getNbZones()) {
            throw new IllegalArgumentException("An unexpected zone was received, the value should be in the range 0-"
                    + (modelProperties.getNbZones() - 1));
        }
    }

}
