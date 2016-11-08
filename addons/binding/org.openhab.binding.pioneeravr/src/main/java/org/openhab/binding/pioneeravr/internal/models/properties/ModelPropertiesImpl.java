/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.models.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;

import com.google.common.collect.Lists;

/**
 *
 * @author Antoine Besnard
 *
 */
public class ModelPropertiesImpl implements ModelProperties {

    private String modelName;
    private String modelDescription;
    private boolean isSetVolumeCommandEnabled;
    private int nbZones;
    private List<Double> zonesVolumeMinDb;
    private List<Double> zonesVolumeMaxDb;
    private List<Double> zonesVolumeStepDb;
    private List<Collection<InputSource>> inputSources;
    private boolean areDbChannelsEnabled;
    private int burstMessageDelay;
    private boolean isBurstModeEnabled;

    public ModelPropertiesImpl(String modelName, int nbZones) {
        this.modelName = modelName;
        this.modelDescription = "A Pioneer AVR " + modelName;
        this.nbZones = nbZones;
        this.zonesVolumeMinDb = new ArrayList<>(nbZones);
        this.zonesVolumeMaxDb = new ArrayList<>(nbZones);
        this.zonesVolumeStepDb = new ArrayList<>(nbZones);
        this.inputSources = new ArrayList<>(nbZones);
        this.isSetVolumeCommandEnabled = true;
        this.areDbChannelsEnabled = true;
        this.burstMessageDelay = DEFAULT_BURST_MESSAGE_DELAY;
        this.isBurstModeEnabled = true;

        // Set default values
        for (int zone = 1; zone <= nbZones; zone++) {
            this.zonesVolumeMaxDb.add(DEFAULT_MAX_DB[zone - 1]);
            this.zonesVolumeMinDb.add(DEFAULT_MIN_DB[zone - 1]);
            this.zonesVolumeStepDb.add(DEFAULT_STEP_DB[zone - 1]);
            this.inputSources.add(Lists.newArrayList(InputSource.values()));
        }
    }

    /**
     * Clone the given ModelProperties.
     *
     * If the source {@link ModelProperties} has more zones than nbZones, the extra zones from the source model are
     * discarded.
     *
     * If the source {@link ModelProperties} has less zones than nbZones, the extra zones in the target model are
     * initialized with default values.
     *
     * @param baseModelProperties
     */
    public ModelPropertiesImpl(ModelProperties baseModelProperties, int nbZones) {
        this.modelName = baseModelProperties.getModelName();
        this.modelDescription = baseModelProperties.getModelDescription();
        this.nbZones = nbZones;
        this.zonesVolumeMinDb = new ArrayList<>(nbZones);
        this.zonesVolumeMaxDb = new ArrayList<>(nbZones);
        this.zonesVolumeStepDb = new ArrayList<>(nbZones);
        this.inputSources = new ArrayList<>(nbZones);
        this.isSetVolumeCommandEnabled = baseModelProperties.isSetVolumeCommandEnabled();
        this.areDbChannelsEnabled = baseModelProperties.areDbChannelsEnabled();
        this.burstMessageDelay = baseModelProperties.getBurstMessageDelay();
        this.isBurstModeEnabled = baseModelProperties.isBurstModeEnabled();

        // Copy the properties from the source ModelProperties.
        int nextZone = 1;
        for (; nextZone <= baseModelProperties.getNbZones(); nextZone++) {
            this.zonesVolumeMaxDb.add(baseModelProperties.getVolumeMaxDb(nextZone));
            this.zonesVolumeMinDb.add(baseModelProperties.getVolumeMinDb(nextZone));
            this.zonesVolumeStepDb.add(baseModelProperties.getVolumeStepDb(nextZone));
            this.inputSources.add(baseModelProperties.getInputSources(nextZone));
        }

        // SThen set extra zone to default values.
        for (; nextZone <= nbZones; nextZone++) {
            this.zonesVolumeMaxDb.add(DEFAULT_MAX_DB[nextZone - 1]);
            this.zonesVolumeMinDb.add(DEFAULT_MIN_DB[nextZone - 1]);
            this.zonesVolumeStepDb.add(DEFAULT_STEP_DB[nextZone - 1]);
            this.inputSources.add(Lists.newArrayList(InputSource.values()));
        }
    }

    @Override
    public boolean isSetVolumeCommandEnabled() {
        return isSetVolumeCommandEnabled;
    }

    public void setSetVolumeCommandEnabled(boolean isEnabled) {
        this.isSetVolumeCommandEnabled = isEnabled;
    }

    @Override
    public double getVolumeMinDb(int zone) {
        return isZoneValid(zone) ? zonesVolumeMinDb.get(zone - 1) : 0;
    }

    public void setVolumeMinDb(int zone, double volumeMinDb) {
        if (isZoneValid(zone)) {
            zonesVolumeMinDb.set(zone - 1, volumeMinDb);
        }
    }

    @Override
    public double getVolumeMaxDb(int zone) {
        return isZoneValid(zone) ? zonesVolumeMaxDb.get(zone - 1) : 0;
    }

    public void setVolumeMaxDb(int zone, double volumeMaxDb) {
        if (isZoneValid(zone)) {
            zonesVolumeMaxDb.set(zone - 1, volumeMaxDb);
        }
    }

    @Override
    public double getVolumeStepDb(int zone) {
        return isZoneValid(zone) ? zonesVolumeStepDb.get(zone - 1) : 0;
    }

    public void setVolumeStepDb(int zone, double volumeStepDb) {
        if (isZoneValid(zone)) {
            zonesVolumeStepDb.set(zone - 1, volumeStepDb);
        }
    }

    @Override
    public String getModelDescription() {
        return modelDescription;
    }

    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    @Override
    public int getNbZones() {
        return nbZones;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setInputSources(int zone, Collection<InputSource> inputSources) {
        if (isZoneValid(zone)) {
            this.inputSources.set(zone - 1, inputSources);
        }
    }

    @Override
    public Collection<InputSource> getInputSources(int zone) {
        return isZoneValid(zone) ? inputSources.get(zone - 1) : new ArrayList<>();
    }

    public void setDbChannelsEnabled(boolean areDbChannelsEnabled) {
        this.areDbChannelsEnabled = areDbChannelsEnabled;
    }

    @Override
    public boolean areDbChannelsEnabled() {
        return areDbChannelsEnabled;
    }

    public void setBurstMessageDelay(int burstMessageDelay) {
        this.burstMessageDelay = burstMessageDelay;
    }

    @Override
    public int getBurstMessageDelay() {
        return burstMessageDelay;
    }

    public void setBusrtModeEnabled(Boolean isBurstModeEnabled) {
        this.isBurstModeEnabled = isBurstModeEnabled;
    }

    @Override
    public boolean isBurstModeEnabled() {
        return isBurstModeEnabled;
    }

    /**
     * Return true if the given zone is supported, else false.
     *
     * @param zone
     * @return
     */
    protected boolean isZoneValid(int zone) {
        return zone > 0 && zone <= getNbZones();
    }

    /**
     * Create a new {@link ModelProperties} with the same properties values as the given one, but override them if
     * needed with the values present in the given configuration.
     *
     * @param modelProperties
     * @param configuration
     * @return
     */
    public static ModelProperties cloneModelPropertiesAndOverrideWithConfiguration(ModelProperties modelProperties,
            Configuration configuration) {
        int newNbZone = modelProperties.getNbZones();

        Object value = configuration.get(PioneerAvrBindingConstants.PARAMETER_NB_ZONES);
        if (value != null) {
            newNbZone = ((Number) value).intValue();
        }

        ModelPropertiesImpl newModelProperties = new ModelPropertiesImpl(modelProperties, newNbZone);

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_USE_SET_VOLUME_COMMAND);
        if (value != null) {
            newModelProperties.setSetVolumeCommandEnabled((Boolean) value);
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_BURST_MESSAGE_DELAY);
        if (value != null) {
            newModelProperties.setBurstMessageDelay(((Number) value).intValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_USE_BURST_MODE);
        if (value != null) {
            newModelProperties.setBusrtModeEnabled((Boolean) value);
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MIN_DB_ZONE1);
        if (value != null) {
            newModelProperties.setVolumeMinDb(1, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MAX_DB_ZONE1);
        if (value != null) {
            newModelProperties.setVolumeMaxDb(1, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_STEP_DB_ZONE1);
        if (value != null) {
            newModelProperties.setVolumeStepDb(1, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MIN_DB_ZONE2);
        if (value != null) {
            newModelProperties.setVolumeMinDb(2, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MAX_DB_ZONE2);
        if (value != null) {
            newModelProperties.setVolumeMaxDb(2, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_STEP_DB_ZONE2);
        if (value != null) {
            newModelProperties.setVolumeStepDb(2, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MIN_DB_ZONE3);
        if (value != null) {
            newModelProperties.setVolumeMinDb(3, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MAX_DB_ZONE3);
        if (value != null) {
            newModelProperties.setVolumeMaxDb(3, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_STEP_DB_ZONE3);
        if (value != null) {
            newModelProperties.setVolumeStepDb(3, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MIN_DB_ZONE4);
        if (value != null) {
            newModelProperties.setVolumeMinDb(4, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_MAX_DB_ZONE4);
        if (value != null) {
            newModelProperties.setVolumeMaxDb(4, ((Number) value).doubleValue());
        }

        value = configuration.get(PioneerAvrBindingConstants.PARAMETER_VOLUME_STEP_DB_ZONE4);
        if (value != null) {
            newModelProperties.setVolumeStepDb(4, ((Number) value).doubleValue());
        }

        return newModelProperties;
    }

}
