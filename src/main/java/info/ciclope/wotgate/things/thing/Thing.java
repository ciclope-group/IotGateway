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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.storage.database.DatabaseStorage;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

public abstract class Thing {
    private final ThingConfiguration thingConfiguration;
    private final DatabaseStorage storageManager;
    private ThingDescription thingDescription;

    public Thing(ThingConfiguration thingConfiguration, DatabaseStorage storageManager) {
        this.thingConfiguration = thingConfiguration;
        this.storageManager = storageManager;
        loadThingDescription(getThingDescriptionPath());
        loadThingExtraConfiguration();
        storageManager.startStorageManager(thingConfiguration.getThingName());
    }

    public ThingConfiguration getThingConfiguration() {
        return thingConfiguration;
    }

    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(JsonObject thingDescription) {
        this.thingDescription = new ThingDescription(thingDescription);
    }

    public abstract String getThingDescriptionPath();

    public abstract void loadThingExtraConfiguration();

    public abstract JsonObject getProperty(String name);

    private void loadThingDescription(String thingDescriptionPath) {
        ObjectMapper objectMapper = new ObjectMapper();
        URL thingDescriptionUrl = getClass().getClassLoader().getResource(thingDescriptionPath);
        JsonObject description = new JsonObject();
        try {
            description = new JsonObject((objectMapper.readValue(thingDescriptionUrl, JsonNode.class)).toString());
        } catch (IOException e) {
            description = new JsonObject();
            e.printStackTrace();
        }
        thingDescription = new ThingDescription(description);
    }
}
