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

package info.ciclope.wotgate.thing.driver.camera;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.http.HttpStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

public class CameraThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/camera/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    private static final String THING_INTERACTION_IMAGES = "images";
    private static final String THING_INTERACTION_CAPTURE_IMAGE = "captureImage";

    private JsonObject stateProperty;
    private JsonArray imagesProperty;
    private JsonObject capturedImage;

    @Override
    public void addHandlers(HandlerRegister register) {
//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getState);
//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_CAPTURE_IMAGE, this::getImages);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        registerImagesProperty(objectMapper);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/camera/CameraStateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = new JsonObject();
            e.printStackTrace();
        }
    }

    private void registerImagesProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/camera/CameraImagesProperty.json");
        URL url2 = getClass().getClassLoader().getResource("things/camera/CapturedImage.json");
        try {
            imagesProperty = new JsonArray((objectMapper.readValue(url, JsonNode.class)).toString());
            capturedImage = new JsonObject((objectMapper.readValue(url2, JsonNode.class)).toString());
        } catch (IOException e) {
            imagesProperty = new JsonArray();
            capturedImage = new JsonObject();
            e.printStackTrace();
        }
    }

    private void getState(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpStatus.OK, new JsonObject(), stateProperty);
        message.reply(response.getResponse());
    }

    private void getImages(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpStatus.OK, new JsonObject(), imagesProperty);
        message.reply(response.getResponse());
    }

    private void captureImage(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpStatus.OK, new JsonObject(), capturedImage);
        message.reply(response.getResponse());
    }

}
