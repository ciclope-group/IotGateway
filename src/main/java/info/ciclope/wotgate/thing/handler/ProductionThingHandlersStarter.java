/*
 *  Copyright (c) 2017, Javier Martínez Villacampa
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.ciclope.wotgate.thing.handler;

import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.component.ThingAddress;
import info.ciclope.wotgate.thing.component.ThingDescription;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static info.ciclope.wotgate.thing.component.ThingDescriptionTag.*;

public class ProductionThingHandlersStarter implements ThingHandlersStarter {
    private final String thingName;
    private ThingHandlers thingHandlers;

    public ProductionThingHandlersStarter(String thingName, DatabaseStorage storage,
                                          ThingHandlers thingHandlers) {
        this.thingHandlers = thingHandlers;
        this.thingName = thingName;
    }

    @Override
    public void startThingHandlers(ThingDescription thingDescription, EventBus eventBus) {
        registerDefaultThingConfigurationHandlers(eventBus);
        registerDefaultThingDescriptionHandlers(thingDescription, eventBus);
        registerDefaultThingInteractionHandlers(thingDescription, eventBus);
    }

    private void registerDefaultThingConfigurationHandlers(EventBus eventBus) {
        eventBus.consumer(ThingAddress.getGetThingConfigurationAddress(thingName), thingHandlers::getThingConfiguration);
    }

    private void registerDefaultThingDescriptionHandlers(ThingDescription thingDescription, EventBus eventBus) {
        eventBus.consumer(ThingAddress.getProvideThingThingDescriptionAddress(thingName), thingHandlers::provideThingDescription);
        eventBus.consumer(ThingAddress.getGetThingThingDescriptionAddress(thingName), thingHandlers::getThingDescription);
        eventBus.consumer(ThingAddress.getPutThingThingDescriptionAddress(thingName), thingHandlers::setThingDescription);
    }

    private void registerDefaultThingInteractionHandlers(ThingDescription thingDescription, EventBus eventBus) {
        JsonArray interactions = thingDescription.getDescription().getJsonArray(THING_DESCRIPTION_INTERACTIONS);
        for (Object interaction : interactions) {
            JsonObject element = (JsonObject) interaction;
            String type = element.getString(THING_DESCRIPTION_INTERACTION_TYPE);
            if (type.equals(THING_DESCRIPTION_INTERACTION_TYPE_PROPERTY)) {
                registerDefaultPropertyInteractionHandlers(thingDescription, element, eventBus);
            } else if (type.equals(THING_DESCRIPTION_INTERACTION_TYPE_ACTION)) {
                registerDefaultActionInteractionHandlers(thingDescription, element, eventBus);
            }
        }
    }

    private void registerDefaultPropertyInteractionHandlers(ThingDescription thingDescription, JsonObject interaction, EventBus eventBus) {
        String name = interaction.getString(THING_DESCRIPTION_INTERACTION_NAME);
        eventBus.consumer(ThingAddress.getGetThingInteractionAddress(thingName, name), thingHandlers::getThingProperty);
        if (thingDescription.isWritableProperty(name)) {
            eventBus.consumer(ThingAddress.getPutThingInteractionAddress(thingName, name), thingHandlers::putThingProperty);
        }
        if (thingDescription.isThingArrayProperty(name)) {
            eventBus.consumer(ThingAddress.getGetThingInteractionExtraDataAddress(thingName, name), thingHandlers::getThingArrayPropertyElement);
            if (thingDescription.isWritableProperty(name)) {
                eventBus.consumer(ThingAddress.getPostThingInteractionAddress(thingName, name), thingHandlers::postThingProperty);
                eventBus.consumer(ThingAddress.getDeleteThingInteractionAddress(thingName, name), thingHandlers::deleteThingProperty);
                eventBus.consumer(ThingAddress.getPutThingInteractionExtraDataAddress(thingName, name), thingHandlers::putThingArrayPropertyElement);
                eventBus.consumer(ThingAddress.getDeleteThingInteractionExtraDataAddress(thingName, name), thingHandlers::deleteThingArrayPropertyElement);
            }
        }
    }

    private void registerDefaultActionInteractionHandlers(ThingDescription thingDescription, JsonObject interaction, EventBus eventBus) {
        String name = interaction.getString(THING_DESCRIPTION_INTERACTION_NAME);
        if (thingDescription.isGetAction(name)) {
            eventBus.consumer(ThingAddress.getGetThingInteractionAddress(thingName, name), thingHandlers::getThingAction);
        } else if (thingDescription.isPostAction(name)) {
            eventBus.consumer(ThingAddress.getPostThingInteractionAddress(thingName, name), thingHandlers::postThingAction);
            if (thingDescription.isObservableAction(name)) {
                eventBus.consumer(ThingAddress.getGetThingInteractionExtraDataAddress(thingName, name), thingHandlers::getThingActionObservable);
            }
        }
    }

}
