/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.thingtype;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.pioneeravr.internal.models.AbstractModel;

/**
 *
 * @author Antoine Besnard
 *
 */
public interface ThingTypeManager {

    /**
     * Return the {@link AbstractModel} from the given {@link ThingTypeUID} if it is registered. Else return null.
     *
     * @param thingTypeUID
     * @return
     */
    AbstractModel getModelFromThingType(ThingTypeUID thingTypeUID);

    /**
     * Register the given {@link AbstractModel} in this manager. Create the corresponding {@link ThingType}.
     *
     * @param model
     */
    void registerModel(AbstractModel model);

    /**
     * Return all the {@link ThingTypeUID} registered in this manager.
     *
     * @return
     */
    Set<ThingTypeUID> getRegisteredThingTypesUIDs();

}
