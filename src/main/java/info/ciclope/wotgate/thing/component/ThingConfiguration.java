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

import info.ciclope.wotgate.ErrorCode;
import io.vertx.core.json.JsonObject;

import java.security.InvalidParameterException;

public class ThingConfiguration {
    private static final String THING_NAME = "name";
    private static final String THING_CLASSNAME = "classname";
    private static final String THING_EXTRA_CONFIGURATION = "extra";

    private final JsonObject thingConfiguration;

    public ThingConfiguration(JsonObject configuration) {
        String thingName = configuration.getString(THING_NAME);
        String thingClassname = configuration.getString(THING_CLASSNAME);
        String thingExtraConfiguration = configuration.getString(THING_EXTRA_CONFIGURATION);

        if (thingName == null || thingName.isEmpty() ||
                thingClassname == null || thingClassname.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.ERROR_THING_CONFIGURATION);
        }
        thingConfiguration = configuration.copy();
    }

    public JsonObject getThingConfiguration() {
        return thingConfiguration;
    }

    public String getThingName() {
        return thingConfiguration.getString(THING_NAME);
    }

    public String getThingClassname() {
        return thingConfiguration.getString(THING_CLASSNAME);
    }

    public JsonObject getExtraConfiguration() {
        return thingConfiguration.getJsonObject(THING_EXTRA_CONFIGURATION);
    }

}
