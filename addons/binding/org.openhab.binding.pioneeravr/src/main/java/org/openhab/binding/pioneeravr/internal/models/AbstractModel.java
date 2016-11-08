/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.models;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.channeltype.ChannelDefinitionFactory;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;

/**
 * Base class for an AVR Model.
 *
 * @author Antoine Besnard
 *
 */
public abstract class AbstractModel {

    public static final URI GENERIC_AVR_CONFIG_DESCRIPTION_URI = URI.create("thing-type:pioneeravr:genericConfig");
    public static final URI SUPPORTED_AVR_CONFIG_DESCRIPTION_URI = URI.create("thing-type:pioneeravr:supportedConfig");

    private ModelProperties modelProperties;

    private ChannelDefinitionFactory channelDefinitionFactory;

    private ThingTypeUID thingTypeUID;

    public AbstractModel(ChannelDefinitionFactory channelDefinitionFactory) {
        this.channelDefinitionFactory = channelDefinitionFactory;
        this.modelProperties = initModelProperties();
        this.thingTypeUID = new ThingTypeUID(PioneerAvrBindingConstants.BINDING_ID, getThingTypeId());
    }

    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    public String getThingTypeId() {
        return modelProperties.getModelName();
    }

    public String getThingTypeLabel() {
        return modelProperties.getModelName();
    }

    public int getNbZones() {
        return modelProperties.getNbZones();
    }

    public List<ChannelDefinition> getChannelDefinitions() {
        List<ChannelDefinition> channelDefinitions = new ArrayList<>();
        channelDefinitions.add(channelDefinitionFactory.getInformationChannelDefinition(modelProperties));
        return channelDefinitions;
    }

    public List<ChannelGroupDefinition> getChannelGroupDefinitions() {
        List<ChannelGroupDefinition> channelGroupDefinitions = new ArrayList<>();
        for (int zone = 1; zone <= getNbZones(); zone++) {
            channelGroupDefinitions.add(channelDefinitionFactory.getZoneChannelGroupDefinition(zone, modelProperties));
        }
        return channelGroupDefinitions;
    }

    public String getThingTypeDescription() {
        return modelProperties.getModelDescription();
    }

    public URI getThingTypeConfigDescriptionURI() {
        return SUPPORTED_AVR_CONFIG_DESCRIPTION_URI;
    }

    public ModelProperties getModelProperties() {
        return modelProperties;
    }

    /**
     * Called by the constructor to initialize the model properties.
     *
     * @return
     */
    protected abstract ModelProperties initModelProperties();

}
