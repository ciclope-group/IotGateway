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

package info.ciclope.wotgate.thing.component;

import info.ciclope.wotgate.thingmanager.InteractionAuthorization;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class ThingRequest {
    private static final String THING_REQUEST_METHOD = "method";
    private static final String THING_REQUEST_HEADERS = "headers";
    private static final String THING_REQUEST_PARAMETERS = "parameters";
    private static final String THING_REQUEST_BODY = "body";
    private static final String THING_REQUEST_AUTHORIZATION = "authorization";

    private final JsonObject request;

    public ThingRequest(RoutingContext routingContext, InteractionAuthorization interactionAuthorization) {
        this.request = new JsonObject();
        this.request.put(THING_REQUEST_METHOD, routingContext.request().method().name());
        this.request.put(THING_REQUEST_HEADERS, parseMultiMap(routingContext.request().headers()));
        this.request.put(THING_REQUEST_PARAMETERS, parseMultiMap(routingContext.request().params()));
        String body = routingContext.getBodyAsString();
        JsonObject bodyJson = new JsonObject();
        try {
            bodyJson = routingContext.getBodyAsJson();
        } catch (DecodeException exception) {
            bodyJson = new JsonObject();
        }
        this.request.put(THING_REQUEST_BODY, bodyJson);
        this.request.put(THING_REQUEST_AUTHORIZATION, interactionAuthorization.getAccessInformation());
    }

    public ThingRequest(JsonObject httpRequestJson) {
        request = httpRequestJson.copy();
    }

    public JsonObject getRequest() {
        return request;
    }

    public HttpMethod getMethod() {
        return Enum.valueOf(HttpMethod.class, request.getString(THING_REQUEST_METHOD));
    }

    public JsonObject getHeaders() {
        return request.getJsonObject(THING_REQUEST_HEADERS);
    }

    public String getHeader(String name) {
        return request.getJsonObject(THING_REQUEST_HEADERS).getString(name);
    }

    public JsonObject getParameters() {
        return request.getJsonObject(THING_REQUEST_PARAMETERS);
    }

    public String getStringParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getString(name);
    }

    public Integer getIntegerParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getInteger(name);
    }

    public Long getLongParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getLong(name);
    }

    public Float getFloatParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getFloat(name);
    }

    public Double getDoubleParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getDouble(name);
    }

    public Boolean getBooleanParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getBoolean(name);
    }

    public JsonObject getJsonObjectParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getJsonObject(name);
    }

    public JsonArray getJsonArrayParameter(String name) {
        return request.getJsonObject(THING_REQUEST_PARAMETERS).getJsonArray(name);
    }

    public JsonObject getBody() {
        return request.getJsonObject(THING_REQUEST_BODY);
    }

    public InteractionAuthorization getInteractionAuthorization() {
        return new InteractionAuthorization(request.getJsonObject(THING_REQUEST_AUTHORIZATION));
    }

    private JsonObject parseMultiMap(MultiMap multimap) {
        JsonObject multimapJson = new JsonObject();
        for (Map.Entry<String, String> entry : multimap) {
            multimapJson.put(entry.getKey(), entry.getValue());
        }

        return multimapJson;
    }

}
