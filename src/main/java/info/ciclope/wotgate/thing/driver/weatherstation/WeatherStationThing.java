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

package info.ciclope.wotgate.thing.driver.weatherstation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.handler.ThingHandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static info.ciclope.wotgate.http.HttpHeader.*;

public class WeatherStationThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/weatherstation/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    private static final String THING_INTERACTION_HISTORICAL_STATE = "historicalState";

    private JsonObject stateProperty;
    private long timerId;

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
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getStateProperty);
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_HISTORICAL_STATE, this::getHistoricalState);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        createStorage(result -> {
            if (result.succeeded()) {
                startUpdatingProcess();
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        stopUpdatingProcess();
        handler.handle(Future.succeededFuture());
    }

    public void startUpdatingProcess() {
        updateMeasurements(0);
        timerId = vertx.setPeriodic(300000, this::updateMeasurements);
    }

    public void stopUpdatingProcess() {
        vertx.cancelTimer(timerId);
    }

    private void createStorage(Handler<AsyncResult<Void>> next) {
        List<String> batch = new ArrayList<>();
        batch.add("CREATE TABLE IF NOT EXISTS property_historicalstate (id INTEGER PRIMARY KEY ASC, data TEXT);");
        databaseStorage.executeBatch(batch, next);
    }

    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/weatherstation/StateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = new JsonObject();
            e.printStackTrace();
        }
    }

    public void getStateProperty(Message<JsonObject> message) {
        JsonObject headers = new JsonObject();
        headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, headers, stateProperty.toString());
        response.setJsonObjectBodyType();
        message.reply(response.getResponse());
    }

    private void getHistoricalState(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String query = "SELECT count(data) FROM property_historicalstate;";
        databaseStorage.query(query, resultSet -> {
            if (resultSet.succeeded()) {
                Integer perPage, page;
                try {
                    perPage = Integer.valueOf(request.getParameter("perPage"));
                } catch (NumberFormatException e) {
                    perPage = 10;
                }
                try {
                    page = Integer.valueOf(request.getParameter("page")) - 1;
                } catch (NumberFormatException e) {
                    page = 0;
                }
                Integer lastIndex = resultSet.result().getRows().get(0).getInteger("count(data)");
                if (perPage <= 0 || page < 0 || page * perPage >= lastIndex) {
                    message.reply(getErrorThingResponse(HttpResponseStatus.RESOURCE_NOT_FOUND, "").getResponse());
                    return;
                }
                if (perPage > 100) {
                    perPage = 100;
                }

                Integer i = page * perPage;
                Integer resultsPerPage = perPage;
                String sql = "SELECT group_concat(data) FROM (SELECT data FROM property_historicalstate LIMIT ? OFFSET ? );";
                JsonArray parameters = new JsonArray().add(resultsPerPage).add(i);
                databaseStorage.queryWithParameters(sql, parameters, resultSet2 -> {
                    if (resultSet2.succeeded()) {
                        ResultSet finalResult = resultSet2.result();
                        JsonObject headers = new JsonObject();
                        headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
                        headers.put(HEADER_RESULT_COUNT, Integer.toString(lastIndex));
                        headers.put(HEADER_PAGE_SIZE, Integer.toString(resultsPerPage));
                        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, headers, "[" + finalResult.getResults().get(0).getString(0) + "]");
                        response.setJsonArrayBodyType();
                        message.reply(response.getResponse());
                        return;
                    } else {
                        // Failed to read measurements.
                        message.reply(getErrorThingResponse(HttpResponseStatus.INTERNAL_ERROR, "").getResponse());
                        return;
                    }
                });
            } else {
                // Failed to read measurements.
                message.reply(getErrorThingResponse(HttpResponseStatus.RESOURCE_NOT_FOUND, "").getResponse());
                return;
            }

        });

    }

    private void updateMeasurements(long id) {
        JsonObject newState = stateProperty;
        Float temperature = newState.getFloat("temperature");
        Random random = new Random();
        Boolean randomBoolean = random.nextBoolean();
        if (temperature > 40) {
            temperature = 39.5f;
        } else if (randomBoolean) {
            temperature++;
        } else {
            temperature--;
        }
        newState.put("temperature", temperature);

        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentTimestamp = now.toString();
        newState.put("timestamp", currentTimestamp);
        stateProperty = newState;

        updateState(newState);
    }

    private void updateState(JsonObject newState) {
        String update = "INSERT INTO property_historicalstate (data) VALUES(json(?));";
        JsonArray parameters = new JsonArray().add(newState);
        databaseStorage.updateWithParameters(update, parameters, updateResult -> {
            return;
        });
    }

    private ThingResponse getErrorThingResponse(Integer status, String message) {
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_TEXT);
        return new ThingResponse(HttpResponseStatus.NOT_IMPLEMENTED, headers, message);
    }

}
