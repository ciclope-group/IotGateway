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

package info.ciclope.wotgate.thingmanager;

import io.vertx.core.json.JsonObject;

public class ThingManagerConfiguration {
    private static final String DEFAULT_HTTP_SERVER_BASE_URI = "http://localhost";
    private static final Integer DEFAULT_HTTP_SERVER_PORT = 8080;
    private static final String HTTP_SERVER_PORT_KEY = "http.port";
    private static final String HTTP_SERVER_BASE_URI_KEY = "http.baseUri";

    private final JsonObject configuration;
    private final String defaultHttpServerBaseUri;

    public ThingManagerConfiguration(JsonObject configuration) {
        this.configuration = configuration;
        if (getHttpServerPort() != 80) {
            this.defaultHttpServerBaseUri = DEFAULT_HTTP_SERVER_BASE_URI + "/";
        } else {
            this.defaultHttpServerBaseUri = DEFAULT_HTTP_SERVER_BASE_URI + ":" + getHttpServerPort() + "/";
        }
    }

    public Integer getHttpServerPort() {
        return configuration.getInteger(HTTP_SERVER_PORT_KEY, DEFAULT_HTTP_SERVER_PORT);
    }

    public String getHttpServerBaseUri() {
        return configuration.getString(HTTP_SERVER_BASE_URI_KEY, defaultHttpServerBaseUri);
    }
}
