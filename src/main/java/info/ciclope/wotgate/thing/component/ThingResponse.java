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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

import static info.ciclope.wotgate.http.HttpHeader.HEADER_CONTENT_TYPE;
import static info.ciclope.wotgate.http.HttpHeader.HEADER_CONTENT_TYPE_JSON;
import static info.ciclope.wotgate.http.HttpHeader.HEADER_CONTENT_TYPE_TEXT;

public class ThingResponse {
    private static final String THING_REQUEST_STATUS = "status";
    private static final String THING_REQUEST_HEADERS = "headers";
    private static final String THING_REQUEST_BODY = "body";
    private static final String THING_REQUEST_BODY_TYPE = "bodytype";
    private static final String THING_REQUEST_BODY_TYPE_STRING = "string";
    private static final String THING_REQUEST_BODY_TYPE_JSON_OBJECT = "jsonobject";
    private static final String THING_REQUEST_BODY_TYPE_JSON_ARRAY = "jsonarray";
    private JsonObject response = new JsonObject();

    public ThingResponse(Integer statusCode, JsonObject headers, String body) {
        headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_TEXT);
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_STRING);
        buildThingResponse(statusCode, headers, body);
    }

    public ThingResponse(Integer statusCode, JsonObject headers, JsonObject body) {
        headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_JSON_OBJECT);
        buildThingResponse(statusCode, headers, body.toString());
    }

    public ThingResponse(Integer statusCode, JsonObject headers, JsonArray body) {
        headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_JSON_ARRAY);
        buildThingResponse(statusCode, headers, body.toString());
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

    public String getStringBody() {
        return response.getString(THING_REQUEST_BODY);
    }

    public JsonObject getJsonObjectBody() {
        return new JsonObject(response.getString(THING_REQUEST_BODY));
    }

    public JsonArray getJsonArrayBody() {
        return new JsonArray(response.getString(THING_REQUEST_BODY));
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

    private void buildThingResponse(Integer statusCode, JsonObject headers, String body) {
        this.response.put(THING_REQUEST_STATUS, statusCode);
        this.response.put(THING_REQUEST_HEADERS, headers);
        this.response.put(THING_REQUEST_BODY, body);
        this.response.put(THING_REQUEST_BODY_TYPE, THING_REQUEST_BODY_TYPE_STRING);
    }

    private JsonObject parseMultiMap(MultiMap multimap) {
        JsonObject multimapJson = new JsonObject();
        for (Map.Entry<String, String> entry : multimap) {
            multimapJson.put(entry.getKey(), entry.getValue());
        }

        return multimapJson;
    }
}
