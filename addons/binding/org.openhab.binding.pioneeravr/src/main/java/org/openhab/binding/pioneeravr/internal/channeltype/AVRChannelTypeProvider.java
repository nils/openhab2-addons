/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.channeltype;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties.InputSource;

/**
 * Provides the {@link ChannelType} of all channels needed by all models of registered AVR. The registered AVRs have
 * built there channels types through this {@link ChannelDefinitionFactory} implementation.
 *
 * @author Antoine Besnard
 *
 */
public class AVRChannelTypeProvider implements ChannelTypeProvider, ChannelDefinitionFactory {

    private Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypes;
    private Map<ChannelTypeUID, ChannelType> channelTypes;

    public AVRChannelTypeProvider() {
        this.channelGroupTypes = new HashMap<>();
        this.channelTypes = new HashMap<>();
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes.values();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return channelGroupTypes.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes.values();
    }

    @Override
    public ChannelGroupDefinition getZoneChannelGroupDefinition(int zone, ModelProperties modelProperties) {
        ChannelGroupDefinition result = new ChannelGroupDefinition("zone" + zone,
                getZoneChannelGroupUID(zone, modelProperties));
        return result;
    }

    @Override
    public ChannelDefinition getPowerChannelDefinition(int zone, ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_POWER,
                getPowerChannelType(zone, modelProperties).getUID(), null, "Power", null);
        return result;
    }

    @Override
    public ChannelDefinition getVolumePercentChannelDefinition(int zone, ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DIMMER,
                getVolumePercentChannelType(zone, modelProperties).getUID(), null, "Volume", null);
        return result;
    }

    @Override
    public ChannelDefinition getVolumeDbChannelDefinition(int zone, ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DB,
                getVolumeDbChannelType(zone, modelProperties).getUID(), null, "Volume", null);
        return result;
    }

    @Override
    public ChannelDefinition getMuteChannelDefinition(int zone, ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_MUTE,
                getMuteChannelType(zone, modelProperties).getUID(), null, "Mute", null);
        return result;
    }

    @Override
    public ChannelDefinition getInformationChannelDefinition(ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_DISPLAY_INFORMATION,
                getInformationChannelType(modelProperties).getUID(), null, "Display", null);
        return result;
    }

    @Override
    public ChannelDefinition getInputSourceChannelDefinition(int zone, ModelProperties modelProperties) {
        ChannelDefinition result = new ChannelDefinition(PioneerAvrBindingConstants.CHANNEL_ID_SET_INPUT_SOURCE,
                getInputSourceChannelType(zone, modelProperties).getUID());
        return result;
    }

    /**
     * Build the {@link ChannelGroupTypeUID} for the given zone and {@link ModelProperties}. If no
     * {@link ChannelGroupType} is currently registered for the built {@link ChannelGroupTypeUID}, it will be built and
     * registered.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    protected ChannelGroupTypeUID getZoneChannelGroupUID(int zone, ModelProperties modelProperties) {
        // Define the UID of the channelGroupType
        ChannelGroupTypeUID channelGroupTypeUID = new ChannelGroupTypeUID(PioneerAvrBindingConstants.BINDING_ID,
                String.format(PioneerAvrBindingConstants.GROUP_CHANNEL_TYPE_ID_PATTERN, modelProperties.getModelName(),
                        zone));

        if (!channelGroupTypes.containsKey(channelGroupTypeUID)) {
            // If not already done, create and register the channelGroupType for the UID.

            // List the channels of the channelGroupType
            List<ChannelDefinition> channelDefinitions = new ArrayList<>();
            channelDefinitions.add(getPowerChannelDefinition(zone, modelProperties));
            channelDefinitions.add(getVolumePercentChannelDefinition(zone, modelProperties));
            channelDefinitions.add(getMuteChannelDefinition(zone, modelProperties));
            channelDefinitions.add(getInputSourceChannelDefinition(zone, modelProperties));

            if (modelProperties.areDbChannelsEnabled()) {
                channelDefinitions.add(getVolumeDbChannelDefinition(zone, modelProperties));
            }

            // Create the channelGroupType
            ChannelGroupType channelGroupType = new ChannelGroupType(channelGroupTypeUID, false, "Zone " + zone, null,
                    null, channelDefinitions);

            // Then, register the channelGroupType
            channelGroupTypes.put(channelGroupTypeUID, channelGroupType);
        }

        return channelGroupTypeUID;
    }

    protected ChannelType getPowerChannelType(int zone, ModelProperties modelProperties) {
        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "powerChannel");

        // If the channel type does not exist, create and register it.
        ChannelType result = channelTypes.get(uid);
        if (result == null) {
            result = new ChannelType(uid, false, "Switch", "Power", "Power ON/OFF the AVR", null, null, null, null);
            channelTypes.put(uid, result);
        }

        return result;
    }

    protected ChannelType getVolumePercentChannelType(int zone, ModelProperties modelProperties) {
        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "volumePercentChannel");

        // If the channel type does not exist, create and register it.
        ChannelType result = channelTypes.get(uid);
        if (result == null) {
            StateDescription stateDescription = new StateDescription(BigDecimal.ZERO, BigDecimal.valueOf(100),
                    BigDecimal.ONE, "%d %%", false, null);

            result = new ChannelType(uid, false, "Dimmer", "Volume",
                    "Increase/Decrease the volume (%) and mute/un-mute", "SoundVolume", null, stateDescription, null);
            channelTypes.put(uid, result);
        }

        return result;
    }

    protected ChannelType getVolumeDbChannelType(int zone, ModelProperties modelProperties) {
        // Compute a hashCode to define channel uniquely by volume parameters
        int hash = BigDecimal.valueOf(modelProperties.getVolumeMinDb(zone)).hashCode() * 21
                + BigDecimal.valueOf(modelProperties.getVolumeMaxDb(zone)).hashCode() * 31
                + BigDecimal.valueOf(modelProperties.getVolumeStepDb(zone)).hashCode() * 41;

        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "volumeDbChannel" + hash);

        ChannelType result = channelTypes.get(uid);

        // If the channel type does not exist, create and register it.
        if (result == null) {
            StateDescription stateDescription = new StateDescription(
                    BigDecimal.valueOf(modelProperties.getVolumeMinDb(zone)),
                    BigDecimal.valueOf(modelProperties.getVolumeMaxDb(zone)),
                    BigDecimal.valueOf(modelProperties.getVolumeStepDb(zone)), "%.1f dB", false, null);

            result = new ChannelType(uid, true, "Number", "Volume", "Set the volume level (dB)", "SoundVolume", null,
                    stateDescription, null);
            channelTypes.put(uid, result);
        }

        return result;
    }

    protected ChannelType getMuteChannelType(int zone, ModelProperties modelProperties) {
        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "muteChannel");

        // If the channel type does not exist, create and register it.
        ChannelType result = channelTypes.get(uid);
        if (result == null) {
            result = new ChannelType(uid, false, "Switch", "Mute", "Enable/Disable mute on the AVR", null, null, null,
                    null);
            channelTypes.put(uid, result);
        }

        return result;
    }

    protected ChannelType getInformationChannelType(ModelProperties modelProperties) {
        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "displayInformationChannel");

        // If the channel type does not exist, create and register it.
        ChannelType result = channelTypes.get(uid);
        if (result == null) {
            StateDescription stateDescription = new StateDescription(null, null, null, null, true, null);
            result = new ChannelType(uid, false, "String", "Display",
                    "Display the information displayed on the AVR front screen", null, null, stateDescription, null);
            channelTypes.put(uid, result);
        }

        return result;
    }

    protected ChannelType getInputSourceChannelType(int zone, ModelProperties modelProperties) {
        int hash = 0;
        if (modelProperties.getInputSources(zone) != null) {
            for (InputSource inputSource : modelProperties.getInputSources(zone)) {
                hash = hash * 11 + 31 * inputSource.getValue().hashCode() + 17 * inputSource.getName().hashCode();
            }
        }

        ChannelTypeUID uid = new ChannelTypeUID(PioneerAvrBindingConstants.BINDING_ID, "setInputSourceChannel" + hash);

        // If the channel type does not exist, create and register it.
        ChannelType result = channelTypes.get(uid);
        if (result == null) {
            List<StateOption> stateOptions = new ArrayList<>();
            if (modelProperties.getInputSources(zone) != null) {
                for (InputSource inputSource : modelProperties.getInputSources(zone)) {
                    stateOptions.add(new StateOption(inputSource.getValue(), inputSource.getName()));
                }
            }

            StateDescription stateDescription = new StateDescription(null, null, null, null, false, stateOptions);
            result = new ChannelType(uid, false, "String", "Input source", "Select the input source of the AVR", null,
                    null, stateDescription, null);
            channelTypes.put(uid, result);
        }

        return result;
    }

}
