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

import info.ciclope.wotgate.thing.component.ThingAddress;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.component.ThingDescription;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ThingHandlerRegister {
    private final Map<String, Handler<Message<JsonObject>>> handlerMap;
    private ThingConfiguration thingConfiguration;
    private ThingDescription thingDescription;
    private String thingName;

    public ThingHandlerRegister(ThingConfiguration thingConfiguration, ThingDescription thingDescription) {
        this.handlerMap = new HashMap<>();
        this.thingConfiguration = thingConfiguration;
        this.thingDescription = thingDescription;
        this.thingName = thingConfiguration.getThingName();
    }

    public ThingConfiguration getThingConfiguration() {
        return thingConfiguration;
    }

    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(ThingDescription thingDescription) {
        this.thingDescription = thingDescription;
    }

    public void registerGetInteractionHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getGetThingInteractionAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerPostInteractionHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getPostThingInteractionAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerPutInteractionHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getPutThingInteractionAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerDeleteInteractionHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getGetThingInteractionAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerGetInteractionExtraDataHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getGetThingInteractionExtraDataAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerPutInteractionExtraDataHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getPutThingInteractionExtraDataAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public void registerDeleteInteractionExtraDataHandler(String interactionName, Handler<Message<JsonObject>> handler) {
        if (thingDescription.containsProperty(interactionName)) {
            String address = ThingAddress.getDeleteThingInteractionExtraDataAddress(thingName, interactionName);
            handlerMap.put(address, handler);
        }
    }

    public boolean containsAddressHandler(String address) {
        return handlerMap.containsKey(address);
    }

    public Handler<Message<JsonObject>> getAddressHandler(String address) {
        return handlerMap.get(address);
    }

}
