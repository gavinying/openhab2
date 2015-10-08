/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eniwise.internal;

import static org.openhab.binding.eniwise.EniWiseBindingConstants.*;

import java.util.Set;

import org.openhab.binding.eniwise.EniWiseBindingConstants;
import org.openhab.binding.eniwise.handler.EniWiseSCPMS6Handler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link EniWiseHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Ying Shaodong - Initial contribution
 */
public class EniWiseHandlerFactory extends BaseThingHandlerFactory {
    
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(EniWiseBindingConstants.THING_TYPE_SCPMS6,
    		EniWiseBindingConstants.THING_TYPE_SCPMT12);
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SCPMS6)) {
            return new EniWiseSCPMS6Handler(thing);
        }

        return null;
    }
}

