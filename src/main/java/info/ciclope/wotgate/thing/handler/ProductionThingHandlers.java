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

import info.ciclope.wotgate.ErrorCode;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.component.ThingContainer;
import info.ciclope.wotgate.thing.component.ThingDescription;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class ProductionThingHandlers implements ThingHandlers {
    private ThingContainer container;
    private ThingHandlerRegister handlerRegister;

    public ProductionThingHandlers(ThingContainer container, ThingHandlerRegister register, DatabaseStorage storage) {
        this.container = container;
        this.handlerRegister = register;
    }

    @Override
    public void getThingConfiguration(Message<Void> message) {
        message.reply(container.getThingConfiguration());
    }

    @Override
    public void provideThingDescription(Message<Void> message) {
        message.reply(container.getThingDescription().getDescription());
    }

    @Override
    public void getThingDescription(Message<JsonObject> message) {
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON);
        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, headers,
                container.getThingDescription().getDescription().toString());
        response.setJsonBodyType();
        message.reply(response.getResponse());
    }

    @Override
    public void setThingDescription(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        container.setThingDescription(new ThingDescription(request.getBody()));
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_TEXT);
        ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, headers,
                "");
        message.reply(response.getResponse());
    }

    @Override
    public void getThingProperty(Message<JsonObject> message) {
        if (handlerRegister.containsAddressHandler(message.address())) {
            handlerRegister.getAddressHandler(message.address()).handle(message);
        } else {
            message.reply(createNotImplementedErrorThingResponse().getResponse());
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

    private JsonObject createHttpResponseHeaders() {
        JsonObject response = new JsonObject();

        return response;
    }

    private ThingResponse createNotImplementedErrorThingResponse() {
        return new ThingResponse(HttpResponseStatus.NOT_IMPLEMENTED, new JsonObject(),
                ErrorCode.ERROR_THING_INTERACTION_NOT_IMPLEMENTED);
    }
}
