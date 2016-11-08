/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.channeltype;

import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.pioneeravr.internal.models.AbstractModel;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;

/**
 * Factory that allows to create {@link ChannelDefinition} and {@link ChannelGroupDefinition} from zones and
 * {@link ModelProperties}.
 *
 * Mainly used by {@link AbstractModel} to build the corresponding {@link ThingType} of an AVR model based on its
 * {@link ModelProperties}.
 *
 * @author Antoine Besnard
 *
 */
public interface ChannelDefinitionFactory {

    /**
     * Return the {@link ChannelGroupDefinition} corresponding to the given zone for the given {@link ModelProperties}.
     * Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelGroupDefinition getZoneChannelGroupDefinition(int zone, ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the PowerChannel corresponding to the given zone for the given
     * {@link ModelProperties}. Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getPowerChannelDefinition(int zone, ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the VolumePercentChannel corresponding to the given zone for the given
     * {@link ModelProperties}. Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getVolumePercentChannelDefinition(int zone, ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the VolumeDbChannel corresponding to the given zone for the given
     * {@link ModelProperties}. Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getVolumeDbChannelDefinition(int zone, ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the MuteChannel corresponding to the given zone for the given
     * {@link ModelProperties}. Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getMuteChannelDefinition(int zone, ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the InformationChannel for the given {@link ModelProperties}. Create it
     * if it does not yet exists.
     *
     * This channel is unique on the AVR so it does not depends of a zone.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getInformationChannelDefinition(ModelProperties modelProperties);

    /**
     * Return the {@link ChannelDefinition} of the InputSourceChannel corresponding to the given zone for the given
     * {@link ModelProperties}. Create it if it does not yet exists.
     *
     * @param zone
     * @param modelProperties
     * @return
     */
    ChannelDefinition getInputSourceChannelDefinition(int zone, ModelProperties modelProperties);

}
