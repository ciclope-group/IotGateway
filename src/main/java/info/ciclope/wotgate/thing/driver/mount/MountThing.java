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

package info.ciclope.wotgate.thing.driver.mount;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.http.HttpStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.mount.actions.*;
import info.ciclope.wotgate.thing.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

public class MountThing extends AbstractThing {
    public static final String OBJECT_SUN = "SUN";
    public static final String OBJECT_MOON = "MOON";
    public static final String OBJECT_UNKNOWN = "UNKNOWN";
    public static final String OBJECT_NONE = "NONE";
    private static final String THING_DESCRIPTION_PATH = "things/dome/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    private static final String THING_INTERACTION_MOVE_ENABLE_MONITORING = "enableMonitoring";
    private static final String THING_INTERACTION_ENABLE_OBJECT_MONITORING = "enableObjectMonitoring";
    private static final String THING_INTERACTION_DISABLE_MONITORING = "disableMonitoring";
    private static final String THING_INTERACTION_MOVE_BY_TIME = "moveByTime";
    private static final String THING_INTERACTION_MOVE_BY_ALTAZIMUTH_COORDINATES = "moveByAltazimuthCoordinates";
    private static final String THING_INTERACTION_MOVE_BY_EQUATORIAL_COORDINATES = "moveByEquatorialCoordinates";

    private EnableMonitoringAction enableMonitoringAction;
    private EnableObjectMonitoringAction enableObjectMonitoringAction;
    private DisableMonitoringAction disableMonitoringAction;
    private MoveByTimeAction moveByTimeAction;
    private MoveByAltazimuthCoordinatesAction moveByAltazimuthCoordinatesAction;
    private MoveByEquatorialCoordinatesAction moveByEquatorialCoordinatesAction;
    private JsonObject stateProperty;

    @Override
    public void addHandlers(HandlerRegister register) {
//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_MOVE_ENABLE_MONITORING, enableMonitoringAction::enableMonitoring);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_MOVE_ENABLE_MONITORING, enableMonitoringAction::getTaskState);
////        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ENABLE_OBJECT_MONITORING, enableObjectMonitoringAction::enableObjectMonitoring);
////        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_ENABLE_OBJECT_MONITORING, enableObjectMonitoringAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DISABLE_MONITORING, disableMonitoringAction::disableMonitoring);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_DISABLE_MONITORING, disableMonitoringAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_TIME, moveByTimeAction::moveByTime);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_TIME, moveByTimeAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_ALTAZIMUTH_COORDINATES, moveByAltazimuthCoordinatesAction::moveByAltazimuthCoordinates);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_ALTAZIMUTH_COORDINATES, moveByAltazimuthCoordinatesAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_EQUATORIAL_COORDINATES, moveByEquatorialCoordinatesAction::moveByEquatorialCoordinates);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_MOVE_BY_EQUATORIAL_COORDINATES, moveByEquatorialCoordinatesAction::getTaskState);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        this.enableMonitoringAction = new EnableMonitoringAction(this.stateProperty, vertx);
        this.enableObjectMonitoringAction = new EnableObjectMonitoringAction(this.stateProperty, vertx);
        this.disableMonitoringAction = new DisableMonitoringAction(this.stateProperty, vertx);
        this.moveByTimeAction = new MoveByTimeAction(this.stateProperty, vertx);
        this.moveByAltazimuthCoordinatesAction = new MoveByAltazimuthCoordinatesAction(this.stateProperty, vertx);
        this.moveByEquatorialCoordinatesAction = new MoveByEquatorialCoordinatesAction(this.stateProperty, vertx);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/mount/MountStateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = new JsonObject();
            e.printStackTrace();
        }
    }

    private void getState(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpStatus.OK, new JsonObject(), stateProperty);
        message.reply(response.getResponse());
    }

}
