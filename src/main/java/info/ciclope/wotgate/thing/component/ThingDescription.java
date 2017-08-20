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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static info.ciclope.wotgate.thing.component.ThingDescriptionTag.*;

public class ThingDescription {
    final JsonObject description;
    final Map<String, JsonObject> propertyMap;
    final Map<String, JsonObject> actionMap;

    public ThingDescription(JsonObject thingDescription) {
        this.description = thingDescription;
        propertyMap = new HashMap<>();
        actionMap = new HashMap<>();
        parseThingDescription(thingDescription);
    }

    public JsonObject getDescription() {
        return description;
    }

    public JsonObject getPropertyDescription(String name) {
        return propertyMap.get(name);
    }

    public JsonObject getActionDescription(String name) {
        return actionMap.get(name);
    }

    public Map<String, JsonObject> getPropertyMap() {
        return propertyMap;
    }

    public Map<String, JsonObject> getActionMap() {
        return actionMap;
    }

    public boolean containsInteraction(String name) {
        return (propertyMap.containsKey(name) || actionMap.containsKey(name));
    }

    public boolean containsProperty(String name) {
        return propertyMap.containsKey(name);
    }

    public boolean containsAction(String name) {
        return actionMap.containsKey(name);
    }

    public boolean isThingArrayProperty(String name) {
        return propertyMap.containsKey(name) &&
                propertyMap.get(name).getJsonObject(THING_DESCRIPTION_INTERACTION_OUTPUTDATA)
                .getJsonObject(THING_DESCRIPTION_INTERACTION_OUTPUTDATA_VALUETYPE)
                .getString(THING_DESCRIPTION_INTERACTION_OUTPUTDATA_TYPE)
                .equals(THING_DESCRIPTION_INTERACTION_OUTPUTDATA_TYPE_ARRAY);
    }

    public boolean isWritableProperty(String name) {
        return propertyMap.containsKey(name) &&
                propertyMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_WRITABLE, true);
    }

    public boolean isGetAction(String name) {
        return actionMap.containsKey(name) &&
                actionMap.get(name).getString(THING_DESCRIPTION_INTERACTION_METHOD, THING_DESCRIPTION_INTERACTION_METHOD_GET)
                .equals(THING_DESCRIPTION_INTERACTION_METHOD_GET);
    }

    public boolean isPostAction(String name) {
        return actionMap.containsKey(name) &&
                actionMap.get(name).getString(THING_DESCRIPTION_INTERACTION_METHOD, THING_DESCRIPTION_INTERACTION_METHOD_GET)
                .equals(THING_DESCRIPTION_INTERACTION_METHOD_POST);
    }

    public boolean isObservableAction(String name) {
        return actionMap.containsKey(name) &&
                isPostAction(name) &&
                actionMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_OBSERVABLE, true);
    }

    private void parseThingDescription(JsonObject thingDescription) {
        JsonArray interactions = thingDescription.getJsonArray(THING_DESCRIPTION_INTERACTIONS);
        for (Object interaction : interactions) {
            JsonObject element = (JsonObject) interaction;
            String name = element.getString(THING_DESCRIPTION_INTERACTION_NAME);
            String type = element.getString(THING_DESCRIPTION_INTERACTION_TYPE);
            if (type.equalsIgnoreCase(THING_DESCRIPTION_INTERACTION_TYPE_PROPERTY)) {
                propertyMap.put(name, element);
            } else if (type.equalsIgnoreCase(THING_DESCRIPTION_INTERACTION_TYPE_ACTION)) {
                actionMap.put(name, element);
            }
        }
    }
}
