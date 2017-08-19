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

package info.ciclope.wotgate.things.weatherstation;

import info.ciclope.wotgate.things.thing.Thing;
import info.ciclope.wotgate.things.thing.ThingHandlerRegister;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class WeatherStationThing extends Thing {
    private static final String THING_DESCRIPTION_PATH = "things/weatherstation/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";

    @Override
    public String getThingDescriptionPath() {
        return THING_DESCRIPTION_PATH;
    }

    @Override
    public boolean loadThingExtraConfiguration() {
        return true;
    }

    @Override
    public void registerThingHandlers(ThingHandlerRegister register) {
        register.registerGetInteractionHandler(THING_INTERACTION_STATE, this::getStateProperty);
    }

    public void getStateProperty(Message<JsonObject> message) {
        message.reply(new JsonObject("{\"Prueba\": \"Conseguida\", \"Valor\": true}"));
    }

}
