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

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class ThingResponse {
    private static final String THING_REQUEST_STATUS = "status";
    private static final String THING_REQUEST_HEADERS = "headers";
    private static final String THING_REQUEST_BODY = "body";
    private static final String THING_REQUEST_BODY_TYPE = "bodytype";
    private static final String THING_REQUEST_BODY_TYPE_STRING = "string";
    private static final String THING_REQUEST_BODY_TYPE_JSON_OBJECT = "jsonobject";
    private static final String THING_REQUEST_BODY_TYPE_JSON_ARRAY = "jsonarray";
    private final JsonObject response;

    public ThingResponse(Integer statusCode, JsonObject headers, String body) {
        this.response = new JsonObject();
        this.response.put(THING_REQUEST_STATUS, statusCode);
        this.response.put(THING_REQUEST_HEADERS, headers);
        this.response.put(THING_REQUEST_BODY, body);
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_STRING);
    }

    public ThingResponse(JsonObject httpResponseJson) {
        response = httpResponseJson.copy();
    }

    public JsonObject getResponse() {
        return response;
    }

    public Integer getStatus() {
        return response.getInteger(THING_REQUEST_STATUS);
    }

    public JsonObject getHeaders() {
        return response.getJsonObject(THING_REQUEST_HEADERS);
    }

    public String getHeader(String name) {
        return response.getJsonObject(THING_REQUEST_HEADERS).getString(name);
    }

    public String getBody() {
        return response.getString(THING_REQUEST_BODY);
    }

    public void setStringBodyType() {
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_STRING);
    }

    public void setJsonObjectBodyType() {
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_JSON_OBJECT);
    }

    public void setJsonArrayBodyType() {
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_JSON_ARRAY);
    }

    public boolean isStringBody() {
        return this.response.getString(THING_REQUEST_BODY_TYPE).equals(THING_REQUEST_BODY_TYPE_STRING);
    }

    public boolean isJsonObjectBody() {
        return this.response.getString(THING_REQUEST_BODY_TYPE).equals(THING_REQUEST_BODY_TYPE_JSON_OBJECT);
    }

    public boolean isJsonArrayBody() {
        return this.response.getString(THING_REQUEST_BODY_TYPE).equals(THING_REQUEST_BODY_TYPE_JSON_ARRAY);
    }

    private JsonObject parseMultiMap(MultiMap multimap) {
        JsonObject multimapJson = new JsonObject();
        for (Map.Entry<String, String> entry : multimap) {
            multimapJson.put(entry.getKey(), entry.getValue());
        }

        return multimapJson;
    }
}
