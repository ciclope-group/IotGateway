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

package info.ciclope.wotgate.thing.driver.dome.actions;

import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.component.ThingObservable;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class SetParkingPositionAction {
    private final Vertx vertx;
    private final JsonObject parkingPositionProperty;
    private final Map<Integer, JsonObject> taskStorage;
    private final Map<Long, Integer> timerTaskMap;
    private Integer index;

    public SetParkingPositionAction(JsonObject parkingPositionProperty, Vertx vertx) {
        this.vertx = vertx;
        this.parkingPositionProperty = parkingPositionProperty;
        taskStorage = new HashMap<>();
        timerTaskMap = new HashMap<>();
        index = 1;
    }

    public void getTaskState(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        Integer taskId = Integer.parseInt(request.getStringParameter("argument"));

        if (taskStorage.containsKey(taskId)) {
            ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), taskStorage.get(taskId));
            message.reply(response.getResponse());
        } else {
            ThingResponse response = new ThingResponse(HttpResponseStatus.RESOURCE_NOT_FOUND, new JsonObject(), "");
            message.reply(response.getResponse());
        }
    }

    public void setParkingPosition(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject userData;
        try {
            userData = request.getBody();
        } catch (DecodeException exception) {
            userData = null;
        }
        Float azimuth = userData.getFloat("azimuth");
        if (userData == null || azimuth == null) {
            ThingResponse response = new ThingResponse(HttpResponseStatus.RESOURCE_NOT_FOUND, new JsonObject(), "");
            message.reply(response.getResponse());
            return;
        }
        Integer taskId = new Integer(index);
        index++;
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        JsonObject task = new JsonObject();
        task.put("id", taskId).put("status", ThingObservable.PENDING_STATE);
        task.put("inputData", userData).put("timestamp", now.toString());
        taskStorage.put(taskId, task);

        Long timerId = vertx.setTimer(15000, this::executeTask);
        timerTaskMap.put(timerId, taskId);

        JsonObject headers = new JsonObject().put(HttpHeader.HEADER_LOCATION, "/" + taskId.toString());
        ThingResponse response = new ThingResponse(HttpResponseStatus.CREATED, headers, taskStorage.get(taskId));
        message.reply(response.getResponse());
    }

    private void executeTask(long id) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        Integer taskId = timerTaskMap.get(id);
        taskStorage.get(taskId).put("status", ThingObservable.EXECUTING_STATE);
        taskStorage.get(taskId).put("timestamp", now.toString());
        timerTaskMap.remove(id);
        Long timerId = vertx.setTimer(15000, this::completeTask);
        timerTaskMap.put(timerId, taskId);
    }

    private void completeTask(long id) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        Integer taskId = timerTaskMap.get(id);
        taskStorage.get(taskId).put("status", ThingObservable.COMPLETED_STATE);
        taskStorage.get(taskId).put("timestamp", now.toString());
        parkingPositionProperty.put("timestamp", now.toString());
        parkingPositionProperty.getJsonObject("parkingPosition").put("azimuth", taskStorage.get(taskId).getJsonObject("inputData").getFloat("azimuth"));
        timerTaskMap.remove(id);
        Long timerId = vertx.setTimer(300000, this::removeTask);
        timerTaskMap.put(timerId, taskId);
    }

    private void removeTask(long id) {
        taskStorage.remove(timerTaskMap.get(id));
        timerTaskMap.remove(id);
    }


}
