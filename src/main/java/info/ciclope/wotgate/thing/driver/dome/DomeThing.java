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

package info.ciclope.wotgate.thing.driver.dome;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.dome.actions.*;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

public class DomeThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/dome/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    private static final String THING_INTERACTION_OPEN_ELEMENT = "openWindow";
    private static final String THING_INTERACTION_CLOSE_ELEMENT = "closeWindow";
    private static final String THING_INTERACTION_ACTIVATE_TRACKING = "activateTracking";
    private static final String THING_INTERACTION_DEACTIVATE_TRACKING = "deactivateTracking";
    private static final String THING_INTERACTION_GO_GOME = "goHome";
    private static final String THING_INTERACTION_PARK = "park";
    private static final String THING_INTERACTION_SET_PARKING_POSITION = "setParkingPosition";
    private static final String THING_INTERACTION_GO_AZIMUTH = "goAzimuth";


    private OpenElementAction openElementAction;
    private CloseElementAction closeElementAction;
    private ActivateTrackingAction activateTrackingAction;
    private DeactivateTrackingAction deactivateTrackingAction;
    private GoHomeAction goHomeAction;
    private ParkAction parkAction;
    private SetParkingPositionAction setParkingPositionAction;
    private GoAzimuthAction goAzimuthAction;
    private JsonObject stateProperty;

    @Override
    public void addHandlers(HandlerRegister register) {
//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getThingState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_OPEN_ELEMENT, openElementAction::openElement);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_OPEN_ELEMENT, openElementAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CLOSE_ELEMENT, closeElementAction::closeElement);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_CLOSE_ELEMENT, closeElementAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ACTIVATE_TRACKING, activateTrackingAction::activateTracking);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_ACTIVATE_TRACKING, activateTrackingAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DEACTIVATE_TRACKING, deactivateTrackingAction::deactivateTracking);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_DEACTIVATE_TRACKING, deactivateTrackingAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GO_GOME, goHomeAction::goHome);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_GO_GOME, goHomeAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GO_AZIMUTH, goAzimuthAction::goAzimuth);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_GO_AZIMUTH, goAzimuthAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_PARK, parkAction::park);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_PARK, parkAction::getTaskState);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_SET_PARKING_POSITION, setParkingPositionAction::setParkingPosition);
//        register.registerGetActionTaskHandler(getThingDescription(), THING_INTERACTION_SET_PARKING_POSITION, setParkingPositionAction::getTaskState);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        this.openElementAction = new OpenElementAction(this.stateProperty, vertx);
        this.closeElementAction = new CloseElementAction(this.stateProperty, vertx);
        this.activateTrackingAction = new ActivateTrackingAction(this.stateProperty, vertx);
        this.deactivateTrackingAction = new DeactivateTrackingAction(this.stateProperty, vertx);
        this.goHomeAction = new GoHomeAction(this.stateProperty, this.stateProperty, vertx);
        this.parkAction = new ParkAction(this.stateProperty, this.stateProperty, vertx);
        this.setParkingPositionAction = new SetParkingPositionAction(this.stateProperty, vertx);
        this.goAzimuthAction = new GoAzimuthAction(this.stateProperty, vertx);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/dome/DomeStateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = null;
            e.printStackTrace();
        }
    }

    public void getThingState(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), stateProperty);
        message.reply(response.getResponse());
    }

}
