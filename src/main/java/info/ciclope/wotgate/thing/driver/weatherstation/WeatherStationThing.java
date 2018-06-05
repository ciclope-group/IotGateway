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
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class WeatherStationThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/weatherstation/ThingDescription.json";
    private static final String THING_INTERACTION_SEARCH_HISTORICAL_STATE = "searchHistoricalState";
    private static final int UPDATE_INTERVAL = 60000;
    private static final int UPDATE_HISTORY_INTERVAL = 600000;

    private static final String URL = "venus.datsi.fi.upm.es";
    private static final int PORT = 5000;

    private JsonObject stateProperty;
    private long timerId;
    private Status status;

    private WebClient webClient;

    @Override
    public void addHandlers(HandlerRegister handlerRegister) {
        handlerRegister.addHandler(WeatherStationInfo.STATUS, this::getStateProperty);
        handlerRegister.addHandler(THING_INTERACTION_SEARCH_HISTORICAL_STATE, this::getHistoricalState);

//        handlerRegister.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getStateProperty);
//        handlerRegister.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_SEARCH_HISTORICAL_STATE, this::getHistoricalState);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        WebClientOptions webClientOptions = new WebClientOptions().setDefaultHost(URL).setDefaultPort(PORT);
        webClient = WebClient.create(vertx, webClientOptions);
        status = new Status();

        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        handler.handle(Future.succeededFuture());
//        createStorage(result -> {
//            if (result.succeeded()) {
//                startUpdatingProcess();
//                handler.handle(Future.succeededFuture());
//            } else {
//                handler.handle(Future.failedFuture(result.cause()));
//            }
//        });
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        stopUpdatingProcess();
        handler.handle(Future.succeededFuture());
    }

    private void startUpdatingProcess() {
        updateMeasurements();
        timerId = vertx.setPeriodic(UPDATE_INTERVAL, event -> updateMeasurements());
    }

    private void stopUpdatingProcess() {
        vertx.cancelTimer(timerId);
    }

//    private void createStorage(Handler<AsyncResult<Void>> next) {
//        List<String> batch = new ArrayList<>();
//        batch.add("CREATE TABLE IF NOT EXISTS historicalstate (id INTEGER PRIMARY KEY ASC, data TEXT);");
//        databaseStorage.executeBatch(batch, next);
//    }

    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("things/weatherstation/StateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = new JsonObject();
            e.printStackTrace();
        }
    }

    private void getStateProperty(Message<JsonObject> message) {
//        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), stateProperty);
        message.reply(status.getEstacion().asJson());
    }

    private void getHistoricalState(Message<JsonObject> message) {
        JsonObject request = new ThingRequest(message.body()).getBody();
        final String dateString = request.getString("date");
        final Integer page = request.getInteger("page");
        final Integer perPage = request.getInteger("perPage");
        if (dateString == null || page == null || perPage == null) {
            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
            return;
        }
        final LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException exception) {
            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
            return;
        }


//        String query = "SELECT count(data) FROM historicalstate WHERE (DATE(json_extract(data, '$.timestamp')) = DATE('" + date.toString() + "'));";
//        databaseStorage.query(query, resultSet -> {
//            if (resultSet.succeeded()) {
//                final Integer lastIndex = resultSet.result().getRows().get(0).getInteger("count(data)");
//                if (perPage <= 0 || page < 0 || page * perPage >= lastIndex) {
//                    message.reply(getErrorThingResponse(HttpResponseStatus.RESOURCE_NOT_FOUND, "").getResponse());
//                    return;
//                }
//
//                Integer i = page * perPage;
//                Integer resultsPerPage = perPage;
//                String sql = "SELECT json_group_array(json(data)) FROM historicalstate WHERE (DATE(json_extract(data, '$.timestamp')) = DATE('" + date.toString() + "')) LIMIT ? OFFSET ?;";
//                JsonArray parameters = new JsonArray().add(resultsPerPage).add(i);
//                databaseStorage.queryWithParameters(sql, parameters, resultSet2 -> {
//                    if (resultSet2.succeeded()) {
//                        ResultSet finalResult = resultSet2.result();
//                        JsonObject results = new JsonObject();
//                        results.put("results", new JsonArray(finalResult.getResults().get(0).getString(0)));
//                        results.put("total", lastIndex);
//                        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), results);
//                        message.reply(response.getResponse());
//                        return;
//                    } else {
//                        // Failed to read measurements.
//                        message.reply(getErrorThingResponse(HttpResponseStatus.INTERNAL_ERROR, "").getResponse());
//                        return;
//                    }
//                });
//            } else {
//                // Failed to read measurements.
//                message.reply(getErrorThingResponse(HttpResponseStatus.INTERNAL_ERROR, "").getResponse());
//                return;
//            }
//
//        });

    }

    private void updateMeasurements() {
        // Make request to physical device
        webClient.get("/api/estacion/montegancedo").as(BodyCodec.json(Status.class)).send(event -> {
            if (event.succeeded()) {
                // Obtain response object
                status = event.result().body();
            } else {
                status.getEstacion().setStatus("No operativo");
            }
        });
    }

    private void updateState(JsonObject newState) {
        String update = "INSERT INTO historicalstate (data) VALUES(json(?));";
        JsonArray parameters = new JsonArray().add(newState);
//        databaseStorage.updateWithParameters(update, parameters, updateResult -> {
//            return;
//        });
    }

    private ThingResponse getErrorThingResponse(Integer status, String message) {
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.CONTENT_TYPE, HttpHeader.CONTENT_TYPE_TEXT);
        return new ThingResponse(status, headers, message);
    }

}
