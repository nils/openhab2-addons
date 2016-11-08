/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.pioneeravr.internal.models.AbstractModel;
import org.openhab.binding.pioneeravr.internal.thingtype.ThingTypeManager;

/**
 * The {@link AvrHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class AvrHandlerFactory extends BaseThingHandlerFactory {

    private ThingTypeManager thingTypeManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeManager.getRegisteredThingTypesUIDs().contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        AbstractModel model = thingTypeManager.getModelFromThingType(thingTypeUID);

        if (model != null) {
            return new AvrHandler(thing, model.getModelProperties());
        }

        return null;
    }

    public void setModelManager(ThingTypeManager modelManager) {
        this.thingTypeManager = modelManager;
    }
}
