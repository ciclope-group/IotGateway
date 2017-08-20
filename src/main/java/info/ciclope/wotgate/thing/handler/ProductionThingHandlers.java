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
import info.ciclope.wotgate.thing.component.ThingDescription;
import info.ciclope.wotgate.thing.component.ThingRequest;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class ProductionThingHandlers implements ThingHandlers {
    private ThingHandlerRegister handlerRegister;
    private DatabaseStorage databaseStorage;

    public ProductionThingHandlers(ThingHandlerRegister register, DatabaseStorage storage) {
        this.handlerRegister = register;
        this.databaseStorage = storage;
    }

    @Override
    public void getThingConfiguration(Message<Void> message) {
        message.reply(handlerRegister.getThingConfiguration());
    }

    @Override
    public void provideThingDescription(Message<Void> message) {
        message.reply(handlerRegister.getThingDescription().getDescription());
    }

    @Override
    public void getThingDescription(Message<JsonObject> message) {
        message.reply(handlerRegister.getThingDescription().getDescription());
    }

    @Override
    public void setThingDescription(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        handlerRegister.setThingDescription(new ThingDescription(request.getBody()));
    }

    @Override
    public void getThingProperty(Message<JsonObject> message) {
        if (handlerRegister.containsAddressHandler(message.address())) {
            handlerRegister.getAddressHandler(message.address()).handle(message);
        } else {
            return;
        }
    }

    @Override
    public void postThingProperty(Message<JsonObject> message) {

    }

    @Override
    public void putThingProperty(Message<JsonObject> message) {

    }

    @Override
    public void deleteThingProperty(Message<JsonObject> message) {

    }

    @Override
    public void getThingArrayPropertyElement(Message<JsonObject> message) {

    }

    @Override
    public void putThingArrayPropertyElement(Message<JsonObject> message) {

    }

    @Override
    public void deleteThingArrayPropertyElement(Message<JsonObject> message) {

    }

    @Override
    public void getThingAction(Message<JsonObject> message) {

    }

    @Override
    public void postThingAction(Message<JsonObject> message) {

    }

    @Override
    public void getThingActionObservable(Message<JsonObject> message) {

    }
}
