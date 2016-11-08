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

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.channeltype.ChannelDefinitionFactory;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelPropertiesImpl;

/**
 * The model that represent a generic AVR (i.e. a model that is not already supported).
 *
 * The properties of this model can be modified on the fly to test a configuration. Once a configuration is found for a
 * specific model, a well defined model should be created for it.
 *
 * @author Antoine Besnard
 *
 */
public class ConfigurableModel extends AbstractModel {

    public ConfigurableModel(ChannelDefinitionFactory channelManager) {
        super(channelManager);
    }

    public static final String MODEL_NAME = "ConfigurablePioneerAVR";

    public static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(PioneerAvrBindingConstants.BINDING_ID,
            MODEL_NAME);

    @Override
    protected ModelProperties initModelProperties() {
        // Keep default parameters.
        // Define 4 zones.
        ModelPropertiesImpl modelPropertiesImpl = new ModelPropertiesImpl(MODEL_NAME, 4);

        modelPropertiesImpl
                .setModelDescription("An unknown AVR model. Parameter of this model can be modified on the fly.");

        // Disable the dB channel since the dB values may be customized by the users
        // (The dB channel state may not reflect the configured dB values)
        modelPropertiesImpl.setDbChannelsEnabled(false);

        // A delay of 10 ms seems to work well.
        modelPropertiesImpl.setBurstMessageDelay(10);

        return modelPropertiesImpl;
    }

    @Override
    public ThingTypeUID getThingTypeUID() {
        return THING_TYPE_UID;
    }

    @Override
    public URI getThingTypeConfigDescriptionURI() {
        return AbstractModel.GENERIC_AVR_CONFIG_DESCRIPTION_URI;
    }

}
