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

    public String getThingDescriptionGatekeeper() {
        return description.getString(THING_DESCRIPTION_GATEKEEPER, "");
    }

    public String getThingDescriptionRoleBasedWritingAccesControl() {
        return description.getString(THING_DESCRIPTION_INTERACTION_ROLE_BASED_WRITING_ACCESS_CONTROL, "");
    }

    public String getInteractionRoleBasedAccesControl(String name) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name).getString(THING_DESCRIPTION_INTERACTION_ROLE_BASED_ACCESS_CONTROL, "");
        } else if (actionMap.containsKey(name)) {
            return actionMap.get(name).getString(THING_DESCRIPTION_INTERACTION_ROLE_BASED_ACCESS_CONTROL, "");
        }

        return null;
    }

    public String getInteractionRoleBasedWritingAccesControl(String name) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name).getString(THING_DESCRIPTION_INTERACTION_ROLE_BASED_WRITING_ACCESS_CONTROL, "");
        } else if (actionMap.containsKey(name)) {
            return actionMap.get(name).getString(THING_DESCRIPTION_INTERACTION_ROLE_BASED_WRITING_ACCESS_CONTROL, "");
        }

        return null;
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
                        .getString(THING_DESCRIPTION_INTERACTION_DATA_TYPE)
                        .equals(THING_DESCRIPTION_INTERACTION_DATA_TYPE_ARRAY);
    }

    public boolean isWritableThingDescription() {
        return this.description.getBoolean(THING_DESCRIPTION_INTERACTION_WRITABLE, true);
    }

    public boolean isAsynchronousAction(String name) {
        return actionMap.containsKey(name) &&
                actionMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_ASYNCHRONOUS, true);
    }

    public boolean isWritableInteraction(String name) {
        return ((actionMap.containsKey(name) &&
                actionMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_WRITABLE, true)) ||
                (propertyMap.containsKey(name) &&
                        propertyMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_WRITABLE, true)));
    }

    public boolean isReservableInteraction(String name) {
        return ((actionMap.containsKey(name) &&
                actionMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_RESERVABLE, true)) ||
                (propertyMap.containsKey(name) &&
                        propertyMap.get(name).getBoolean(THING_DESCRIPTION_INTERACTION_RESERVABLE, true)));
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
