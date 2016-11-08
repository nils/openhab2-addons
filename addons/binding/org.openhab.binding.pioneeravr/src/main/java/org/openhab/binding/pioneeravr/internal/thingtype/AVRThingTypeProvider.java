/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.thingtype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingTypeProvider;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.pioneeravr.internal.channeltype.ChannelDefinitionFactory;
import org.openhab.binding.pioneeravr.internal.models.AbstractModel;
import org.openhab.binding.pioneeravr.internal.models.ConfigurableModel;
import org.openhab.binding.pioneeravr.internal.models.VSX1021;
import org.openhab.binding.pioneeravr.internal.models.VSX1120;
import org.openhab.binding.pioneeravr.internal.models.VSX1122;
import org.openhab.binding.pioneeravr.internal.models.VSX921;

/**
 *
 * @author Antoine Besnard
 *
 */
public class AVRThingTypeProvider implements ThingTypeProvider, ThingTypeManager {

    private ChannelDefinitionFactory channelDefinitionFactory;

    private Map<ThingTypeUID, ThingType> thingTypesByUIDs;
    private Map<ThingTypeUID, AbstractModel> modelsByUIDs;

    public AVRThingTypeProvider() {
        this.thingTypesByUIDs = new HashMap<>();
        this.modelsByUIDs = new HashMap<>();
    }

    public void activate() {
        // Initialize all models
        registerModel(new ConfigurableModel(channelDefinitionFactory));
        registerModel(new VSX921(channelDefinitionFactory));
        registerModel(new VSX1021(channelDefinitionFactory));
        registerModel(new VSX1120(channelDefinitionFactory));
        registerModel(new VSX1122(channelDefinitionFactory));
    }

    @Override
    public Collection<ThingType> getThingTypes(Locale locale) {
        return thingTypesByUIDs.values();
    }

    @Override
    public ThingType getThingType(ThingTypeUID thingTypeUID, Locale locale) {
        return thingTypesByUIDs.get(thingTypeUID);
    }

    @Override
    public AbstractModel getModelFromThingType(ThingTypeUID thingTypeUID) {
        return modelsByUIDs.get(thingTypeUID);
    }

    @Override
    public void registerModel(AbstractModel model) {
        ThingType thingType = new ThingType(model.getThingTypeUID(), null, model.getThingTypeLabel(),
                model.getThingTypeDescription(), model.getChannelDefinitions(), model.getChannelGroupDefinitions(),
                null, model.getThingTypeConfigDescriptionURI());
        thingTypesByUIDs.put(thingType.getUID(), thingType);
        modelsByUIDs.put(thingType.getUID(), model);
    }

    @Override
    public Set<ThingTypeUID> getRegisteredThingTypesUIDs() {
        return thingTypesByUIDs.keySet();
    }

    public void setChannelDefinitionFactory(ChannelDefinitionFactory channelDefinitionFactory) {
        this.channelDefinitionFactory = channelDefinitionFactory;
    }

}
