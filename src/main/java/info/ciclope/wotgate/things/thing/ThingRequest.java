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

import info.ciclope.wotgate.thingmanager.InteractionAuthorization;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class ThingRequest {
    private static final String THING_REQUEST_METHOD = "method";
    private static final String THING_REQUEST_HEADERS = "headers";
    private static final String THING_REQUEST_PARAMETERS = "parameters";
    private static final String THING_REQUEST_BODY = "body";
    private static final String THING_REQUEST_AUTHORIZATION = "authorization";

    private final JsonObject thingRequest;

    public ThingRequest(RoutingContext routingContext, InteractionAuthorization interactionAuthorization) {
        this.thingRequest = new JsonObject();
        this.thingRequest.put(THING_REQUEST_METHOD, routingContext.request().method().name());
        this.thingRequest.put(THING_REQUEST_HEADERS, parseMultiMap(routingContext.request().headers()));
        this.thingRequest.put(THING_REQUEST_PARAMETERS, parseMultiMap(routingContext.request().params()));
        String body = routingContext.getBodyAsString();
        JsonObject bodyJson = new JsonObject();
        if (!body.isEmpty()) {
            bodyJson = routingContext.getBodyAsJson();
        }
        this.thingRequest.put(THING_REQUEST_BODY, bodyJson);
        this.thingRequest.put(THING_REQUEST_AUTHORIZATION, interactionAuthorization.getAuthorization());
    }

    public ThingRequest(JsonObject httpRequestJson) {
        thingRequest = httpRequestJson.copy();
    }

    public JsonObject getThingRequest() {
        return thingRequest;
    }

    public HttpMethod getMethod() {
        return Enum.valueOf(HttpMethod.class, thingRequest.getString(THING_REQUEST_METHOD));
    }

    public JsonObject getHeaders() {
        return thingRequest.getJsonObject(THING_REQUEST_HEADERS);
    }

    public String getHeader(String name) {
        return thingRequest.getJsonObject(THING_REQUEST_HEADERS).getString(name);
    }

    public JsonObject getParameters() {
        return thingRequest.getJsonObject(THING_REQUEST_PARAMETERS);
    }

    public String getParameter(String name) {
        return thingRequest.getJsonObject(THING_REQUEST_PARAMETERS).getString(name);
    }

    public JsonObject getBody() {
        return thingRequest.getJsonObject(THING_REQUEST_BODY);
    }

    public InteractionAuthorization getInteractionAuthorization() {
        return new InteractionAuthorization(thingRequest.getJsonObject(THING_REQUEST_AUTHORIZATION));
    }

    private JsonObject parseMultiMap(MultiMap multimap) {
        JsonObject multimapJson = new JsonObject();
        for (Map.Entry<String, String> entry : multimap) {
            multimapJson.put(entry.getKey(), entry.getValue());
        }

        return multimapJson;
    }

}
