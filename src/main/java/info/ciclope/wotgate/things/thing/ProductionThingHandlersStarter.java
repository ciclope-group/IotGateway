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

package info.ciclope.wotgate.things.thing;

import info.ciclope.wotgate.storage.database.DatabaseStorage;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ProductionThingHandlersStarter implements ThingHandlersStarter {
    private final String thingName;
    private ThingHandlerRegister handlerRegister;
    private DatabaseStorage databaseStorage;
    private ThingHandlers thingHandlers;

    public ProductionThingHandlersStarter(ThingHandlerRegister register, DatabaseStorage storage,
                                          ThingHandlers thingHandlers) {
        this.handlerRegister = register;
        this.databaseStorage = storage;
        this.thingHandlers = thingHandlers;
        this.thingName = register.getThingConfiguration().getThingName();
    }

    @Override
    public void startThingHandlers(EventBus eventBus) {
        registerDefaultThingConfigurationHandlers(eventBus);
        registerDefaultThingDescriptionHandlers(eventBus);
        registerDefaultThingInteractionHandlers(eventBus);
    }

    private void registerDefaultThingConfigurationHandlers(EventBus eventBus) {
        eventBus.consumer(ThingAddress.getGetThingConfigurationAddress(thingName), thingHandlers::getThingConfiguration);
    }

    private void registerDefaultThingDescriptionHandlers(EventBus eventBus) {
        eventBus.consumer(ThingAddress.getProvideThingThingDescriptionAddress(thingName), thingHandlers::provideThingDescription);
        eventBus.consumer(ThingAddress.getGetThingThingDescriptionAddress(thingName), thingHandlers::getThingDescription);
        eventBus.consumer(ThingAddress.getPutThingThingDescriptionAddress(thingName), thingHandlers::setThingDescription);
    }

    private void registerDefaultThingInteractionHandlers(EventBus eventBus) {
        ThingDescription thingDescription = handlerRegister.getThingDescription();
        JsonArray interactions = thingDescription.getDescription().getJsonArray(ThingDescription.THING_DESCRIPTION_INTERACTIONS);
        for (Object interaction : interactions) {
            JsonObject element = (JsonObject) interaction;
            String type = element.getString(ThingDescription.THING_DESCRIPTION_INTERACTION_TYPE);
            if (type.equals(ThingDescription.THING_DESCRIPTION_INTERACTION_TYPE_PROPERTY)) {
                registerDefaultPropertyInteractionHandlers(element, eventBus);
            } else if (type.equals(ThingDescription.THING_DESCRIPTION_INTERACTION_TYPE_ACTION)) {
                registerDefaultActionInteractionHandlers(element, eventBus);
            }
        }
    }

    private void registerDefaultPropertyInteractionHandlers(JsonObject interaction, EventBus eventBus) {
        ThingDescription thingDescription = handlerRegister.getThingDescription();
        String name = interaction.getString(ThingDescription.THING_DESCRIPTION_INTERACTION_NAME);
        eventBus.consumer(ThingAddress.getGetThingInteractionAddress(thingName, name), thingHandlers::getThingProperty);
        if (thingDescription.isWritableProperty(name)) {
            eventBus.consumer(ThingAddress.getPutThingInteractionAddress(thingName, name), thingHandlers::putThingProperty);
        }
        if (thingDescription.isThingArrayProperty(name)) {
            eventBus.consumer(ThingAddress.getGetThingInteractionExtraDataAddress(thingName, name), thingHandlers::getThingArrayPropertyElement);
            if (thingDescription.isWritableProperty(name)) {
                eventBus.consumer(ThingAddress.getPostThingInteractionAddress(thingName, name), thingHandlers::postThingProperty);
                eventBus.consumer(ThingAddress.getPutThingInteractionExtraDataAddress(thingName, name), thingHandlers::putThingArrayPropertyElement);
                eventBus.consumer(ThingAddress.getDeleteThingInteractionExtraDataAddress(thingName, name), thingHandlers::deleteThingArrayPropertyElement);
            }
        }
    }

    private void registerDefaultActionInteractionHandlers(JsonObject interaction, EventBus eventBus) {
        String name = interaction.getString(ThingDescription.THING_DESCRIPTION_INTERACTION_NAME);
        eventBus.consumer(ThingAddress.getGetThingInteractionAddress(thingName, name), thingHandlers::getThingAction);
    }

}
