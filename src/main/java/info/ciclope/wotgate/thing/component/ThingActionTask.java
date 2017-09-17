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

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static info.ciclope.wotgate.thing.component.ThingDescriptionTag.*;

public class ThingActionTask {
    public static final String TASK_STATUS_OK = "OK";
    public static final String TASK_STATUS_PENDING = "PENDING";
    public static final String TASK_STATUS_ERROR = "ERROR";
    public static final String TASK_STATUS_RUNNING = "RUNNING";

    private final JsonObject task;

    public ThingActionTask() {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, new JsonObject());
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, new JsonObject());
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, TASK_STATUS_PENDING);
        this.task = task;
    }

    public ThingActionTask(JsonObject inputData) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, new JsonObject());
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, TASK_STATUS_PENDING);
        this.task = task;
    }

    public ThingActionTask(JsonArray inputData) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, new JsonObject());
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, TASK_STATUS_PENDING);
        this.task = task;
    }

    public ThingActionTask(JsonObject inputData, JsonObject outputData, String status) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, status);
        this.task = task;
    }

    public ThingActionTask(JsonArray inputData, JsonObject outputData, String status) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, status);
        this.task = task;
    }

    public ThingActionTask(JsonObject inputData, JsonArray outputData, String status) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, status);
        this.task = task;
    }

    public ThingActionTask(JsonArray inputData, JsonArray outputData, String status) {
        JsonObject task = new JsonObject();
        task.put(THING_DESCRIPTION_INTERACTION_INPUTDATA, inputData);
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, status);
        this.task = task;
    }

    public void setOutputData(JsonObject outputData) {
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
    }

    public void setOutputData(JsonArray outputData) {
        task.put(THING_DESCRIPTION_INTERACTION_OUTPUTDATA, outputData);
    }

    public void setStatus(String status) {
        task.put(THING_DESCRIPTION_INTERACTION_TASK_STATUS, status);
    }

    public String getThingActionTaskJson() {
        return task.encodePrettily();
    }
}
