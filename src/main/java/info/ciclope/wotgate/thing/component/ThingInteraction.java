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

import io.vertx.core.json.JsonObject;

import static info.ciclope.wotgate.thing.component.ThingDescriptionTag.*;

public class ThingInteraction {
    public final JsonObject thingInteraction;

    public ThingInteraction(JsonObject interaction) {
        this.thingInteraction = interaction;
    }

    public JsonObject getThingInteraction() {
        return thingInteraction;
    }

    public String getThingInteractionType() {
        return thingInteraction.getString(THING_DESCRIPTION_INTERACTION_TYPE);
    }

    public String getThingActionType() {
        return thingInteraction.getString(THING_DESCRIPTION_INTERACTION_METHOD, THING_DESCRIPTION_INTERACTION_METHOD_POST);
    }

    public boolean isThingProperty() {
        return getThingInteractionType()
                .equals(THING_DESCRIPTION_INTERACTION_TYPE_PROPERTY);
    }

    public boolean isThingObjectProperty() {
        return isThingProperty() &&
                thingInteraction.getJsonObject(THING_DESCRIPTION_INTERACTION_OUTPUTDATA)
                        .getJsonObject(THING_DESCRIPTION_INTERACTION_DATA_VALUETYPE)
                        .getString(THING_DESCRIPTION_INTERACTION_DATA_TYPE)
                        .equals(THING_DESCRIPTION_INTERACTION_DATA_TYPE_OBJECT);
    }

    public boolean isThingArrayProperty() {
        return isThingProperty() &&
                thingInteraction.getJsonObject(THING_DESCRIPTION_INTERACTION_OUTPUTDATA)
                        .getJsonObject(THING_DESCRIPTION_INTERACTION_DATA_VALUETYPE)
                        .getString(THING_DESCRIPTION_INTERACTION_DATA_TYPE)
                        .equals(THING_DESCRIPTION_INTERACTION_DATA_TYPE_ARRAY);
    }

    public boolean isThingAction() {
        return getThingInteractionType().equals(THING_DESCRIPTION_INTERACTION_TYPE_ACTION);
    }

    public boolean isWritable() {
        return thingInteraction.getBoolean(THING_DESCRIPTION_INTERACTION_WRITABLE, false);
    }

    public boolean isWritableProperty() {
        return isThingProperty() && isWritable();
    }

    public boolean isWritableAction() {
        return isThingAction() && isWritable();
    }

    public boolean isGetAction() {
        return isThingAction() &&
                getThingActionType().equals(THING_DESCRIPTION_INTERACTION_METHOD_GET);
    }

    public boolean isPostAction() {
        return isThingAction() &&
                getThingActionType().equals(THING_DESCRIPTION_INTERACTION_METHOD_POST);
    }

    public boolean isObservableAction(String name) {
        return isPostAction() &&
                thingInteraction.getBoolean(THING_DESCRIPTION_INTERACTION_OBSERVABLE, true);
    }
}
