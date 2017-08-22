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

package info.ciclope.wotgate.thing.driver.gatekeeper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.WoTGateStates;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingRequestParameter;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.authorizer.AuthorizerThing;
import info.ciclope.wotgate.thing.driver.gatekeeper.calendar.CalendarThing;
import info.ciclope.wotgate.thing.driver.gatekeeper.roles.RoleThing;
import info.ciclope.wotgate.thing.driver.gatekeeper.users.UserThing;
import info.ciclope.wotgate.thing.handler.ThingHandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static info.ciclope.wotgate.http.HttpHeader.*;

public class GateKeeperThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/gatekeeper/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    private static final String THING_INTERACTION_USERS = "users";
    private static final String THING_INTERACTION_ROLES = "roles";
    private static final String THING_INTERACTION_CALENDAR = "calendar";
    private static final String THING_INTERACTION_REGISTER_USER = "registerUser";
    private static final String THING_INTERACTION_CONFIRM_USER_REGISTRATION = "confirmUserRegistration";
    private static final String THING_INTERACTION_MODIFY_USER = "modifyUser";
    private static final String THING_INTERACTION_DELETE_USER = "deleteUser";
    private static final String THING_INTERACTION_RECOVER_USER_PASSWORD = "recoverUserPassword";
    private static final String THING_INTERACTION_GENERATE_USER_TOKEN = "generateUserToken";
    private static final String THING_INTERACTION_REVOKE_USER_TOKEN = "revokeUserToken";
    private static final String THING_INTERACTION_ADD_USER_RESERVATION = "addUserReservation";
    private static final String THING_INTERACTION_DELETE_USER_RESERVATION = "deleteUserReservation";
    private static final String THING_INTERACTION_ACK_RESERVATION = "ackReservation";

    private UserThing userThing;
    private RoleThing roleThing;
    private AuthorizerThing authorizerThing;
    private CalendarThing calendarThing;
    private JsonObject stateProperty;
    private String workingMode = WoTGateStates.MODE_UNRESTRICTED;

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
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getState);
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_USERS, this::getUsers);
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_ROLES, this::getRoles);
        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_CALENDAR, this::getCalendar);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REGISTER_USER, this::registerUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_USER_REGISTRATION, this::confirmUserRegistration);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_RECOVER_USER_PASSWORD, this::recoverUserPassword);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER, this::deleteUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_MODIFY_USER, this::modifyUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GENERATE_USER_TOKEN, this::generateUserToken);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REVOKE_USER_TOKEN, this::revokeUserToken);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_RESERVATION, this::addUserReservation);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_RESERVATION, this::deleteUserReservation);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ACK_RESERVATION, this::ackReservation);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        this.userThing = new UserThing(databaseStorage);
        this.roleThing = new RoleThing(databaseStorage);
        this.authorizerThing = new AuthorizerThing(databaseStorage);
        this.calendarThing = new CalendarThing(databaseStorage);
        ObjectMapper objectMapper = new ObjectMapper();
        registerStateProperty(objectMapper);
        createStorage(result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }


    private void registerStateProperty(ObjectMapper objectMapper) {
        URL url = getClass().getClassLoader().getResource("webthings/gatekeeper/GateKeeperStateProperty.json");
        try {
            stateProperty = new JsonObject((objectMapper.readValue(url, JsonNode.class)).toString());
        } catch (IOException e) {
            stateProperty = new JsonObject();
            e.printStackTrace();
        }
    }

    private void getState(Message<JsonObject> message) {
        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), stateProperty);
        message.reply(response.getResponse());
    }

    private void getCalendar(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject inputData = new JsonObject();
        String startDate = request.getStringParameter("startDate");
        String freebusy = request.getStringParameter("freebusy");
        String freebusytype = request.getStringParameter("freebusytype");
        if (startDate != null) {
            inputData.put("startDate", startDate);
        }
        if (freebusy != null) {
            inputData.put("freebusy", freebusy);
        }
        if (freebusytype != null) {
            inputData.put("freebusytype", freebusytype);
        }

        calendarThing.getCalendar(inputData, request.getInteractionAuthorization().getUsername(), calendarResult -> {
            if (calendarResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(calendarResult.cause().getMessage()), "").getResponse());
            } else {
                ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), calendarResult.result());
                message.reply(response.getResponse());
            }
        });
    }

    private void modifyUser(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject userData = request.getBody();
        String userName = userData.getString("name");
        String email = userData.getString("email");
        String password = userData.getString("password");
        if (userName == null || (email == null && password == null)) {
            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
            return;
        }
        if (userName.equals("administrator")) {
            ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
            message.reply(response.getResponse());
        } else if (userName.equals("authenticated")) {
            message.reply(getErrorThingResponse(HttpResponseStatus.FORBIDDEN, "").getResponse());
        } else {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
        }
    }

    private void deleteUser(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject userData = request.getBody();
        String userName = userData.getString("name");
        if (userName.equals("administrator")) {
            ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
            message.reply(response.getResponse());
        } else if (userName.equals("authenticated")) {
            message.reply(getErrorThingResponse(HttpResponseStatus.FORBIDDEN, "").getResponse());
        } else {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
        }
    }

    private void recoverUserPassword(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject userData = request.getBody();
        String userName = userData.getString("name");
        String email = userData.getString("email");
        if (userName == null || email == null) {
            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
            return;
        }
        if (userName.equals("administrator") && email.equals("a@gogogogo.com")) {
            ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
            message.reply(response.getResponse());
        } else {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
        }
    }

    private void confirmUserRegistration(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
        message.reply(response.getResponse());
    }

    private void generateUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userpassword = request.getHeader(HttpHeader.HEADER_AUTHORIZATION);
        if (userpassword == null || userpassword.length() < 9 || !userpassword.substring(0, 6).equals("Basic ")) {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
            return;
        }
        byte[] bytes = Base64.getDecoder().decode(userpassword.substring(6));
        if (bytes == null) {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
            return;
        }
        userpassword = new String(bytes);
        String[] authorization = userpassword.split(":", 2);
        if (authorization == null || authorization.length < 2) {
            message.reply(getErrorThingResponse(HttpResponseStatus.UNAUTHORIZED, "").getResponse());
            return;
        }
        authorizerThing.generateUserToken(authorization[0], authorization[1], result -> {
            if (result.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(result.cause().getMessage()), "").getResponse());
                return;
            }
            ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result());
            message.reply(response.getResponse());
        });
    }

    private void revokeUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String token = request.getStringParameter(ThingRequestParameter.PARAMETER_TOKEN);
        getTokenOwner(token, getResult -> {
            if (getResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(getResult.cause().getMessage()), "").getResponse());
            } else {
                authorizerThing.revokeUserToken(getResult.result(), revokeResult -> {
                    if (revokeResult.failed()) {
                        message.reply(getErrorThingResponse(Integer.decode(revokeResult.cause().getMessage()), "").getResponse());
                    }
                    ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
                    message.reply(response.getResponse());
                });
            }
        });
    }

    private void getRoles(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        Integer perPage, page;
        String name;
        try {
            perPage = Integer.valueOf(request.getStringParameter(ThingRequestParameter.PARAMETER_PER_PAGE));
        } catch (NumberFormatException e) {
            perPage = 10;
        }
        try {
            page = Integer.valueOf(request.getStringParameter(ThingRequestParameter.PARAMETER_PAGE)) - 1;
        } catch (NumberFormatException e) {
            page = 0;
        }
        name = request.getStringParameter("name");
        roleThing.getRoles(page, perPage, name, result -> {
            if (result.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(result.cause().getMessage()), "").getResponse());
                return;
            }
            JsonObject headers = new JsonObject();
            headers.put(HEADER_RESULT_COUNT, Integer.toString(result.result().getInteger("total")));
            headers.put(HEADER_PAGE_SIZE, Integer.toString(result.result().getInteger("perPage")));
            ThingResponse response = new ThingResponse(HttpResponseStatus.OK, headers, result.result().getJsonArray("results"));
            message.reply(response.getResponse());
        });
    }

    private void registerUser(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        JsonObject data = request.getBody();
        if (data == null) {
            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
            return;
        }
        userThing.registerUser(data, result -> {
            if (result.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(result.cause().getMessage()), "").getResponse());
                return;
            }
            ThingResponse response = new ThingResponse(HttpResponseStatus.CREATED, new JsonObject(), "");
            message.reply(response.getResponse());
        });
    }

    private void getUsers(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        Integer perPage, page;
        String name;
        try {
            perPage = Integer.valueOf(request.getStringParameter(ThingRequestParameter.PARAMETER_PER_PAGE));
        } catch (NumberFormatException e) {
            perPage = 10;
        }
        try {
            page = Integer.valueOf(request.getStringParameter(ThingRequestParameter.PARAMETER_PAGE)) - 1;
        } catch (NumberFormatException e) {
            page = 0;
        }
        name = request.getStringParameter("name");
        userThing.getUsers(page, perPage, name, result -> {
            if (result.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(result.cause().getMessage()), "").getResponse());
                return;
            }
            JsonObject headers = new JsonObject();
            headers.put(HEADER_RESULT_COUNT, Integer.toString(result.result().getInteger("total")));
            headers.put(HEADER_PAGE_SIZE, Integer.toString(result.result().getInteger("perPage")));
            ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result().getJsonArray("results"));
            message.reply(response.getResponse());
        });
    }

    private void addUserReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName = request.getInteractionAuthorization().getUsername();
        JsonObject reservationData;
        reservationData = request.getBody();
        calendarThing.addUserReservation(reservationData, userName, reservationResult -> {
            if (reservationResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(reservationResult.cause().getMessage()), "").getResponse());
            } else {
                ThingResponse response = new ThingResponse(HttpResponseStatus.CREATED, new JsonObject(), "");
                message.reply(response.getResponse());
            }
        });
    }


    private void deleteUserReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName = request.getInteractionAuthorization().getUsername();
        JsonObject reservationData;
        reservationData = request.getBody();
        calendarThing.deleteUserReservation(reservationData, userName, deleteReservationResult -> {
            if (deleteReservationResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(deleteReservationResult.cause().getMessage()), "").getResponse());
            } else {
                ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
                message.reply(response.getResponse());
            }
        });
    }

    private void ackReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName = request.getInteractionAuthorization().getUsername();
        calendarThing.ackReservation(userName, ackResult -> {
            if (ackResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(ackResult.cause().getMessage()), "").getResponse());
            } else {
                this.stateProperty.put("ackReservation", true);
                ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
                message.reply(response.getResponse());
            }
        });
    }

    public void startGateKeeperThing(Handler<AsyncResult<Void>> next) {
        createStorage(storageCreation -> {
            if (storageCreation.failed()) {
                next.handle(Future.failedFuture(storageCreation.cause()));
                return;
            }
            next.handle(Future.succeededFuture());
        });
    }

    private void createStorage(Handler<AsyncResult<Void>> handler) {
        List<String> batch = new ArrayList<>();
        batch.add("CREATE TABLE IF NOT EXISTS gatekeeper_users (id INTEGER PRIMARY KEY ASC, data TEXT);");
        batch.add("CREATE TABLE IF NOT EXISTS gatekeeper_roles (id INTEGER PRIMARY KEY ASC, data TEXT);");
        batch.add("CREATE TABLE IF NOT EXISTS gatekeeper_users_in_role (id INTEGER PRIMARY KEY ASC, user INTEGER, role INTEGER, UNIQUE (user, role));");
        batch.add("CREATE TABLE IF NOT EXISTS gatekeeper_reservations (id INTEGER PRIMARY KEY ASC, data TEXT);");
        databaseStorage.executeBatch(batch, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void setWotGateWorkingMode(String mode) {
        workingMode = mode;
    }

    public void checkUserRoleAuthorization(String token, String processRole, String userName, Handler<AsyncResult<Integer>> handler) {
        authorizerThing.checkUserRoleAuthorization(token, processRole, userName, workingMode, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(result.cause()));
            } else {
                handler.handle(Future.succeededFuture(result.result()));
            }
        });
    }

    public void checkRoleAuthorization(String token, String processRole, Handler<AsyncResult<Integer>> handler) {
        authorizerThing.checkRoleAuthorization(token, processRole, workingMode, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(result.cause()));
            } else {
                handler.handle(Future.succeededFuture(result.result()));
            }
        });
    }

    public void getTokenOwner(String token, Handler<AsyncResult<String>> handler) {
        authorizerThing.getTokenOwner(token, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(result.cause()));
            } else {
                handler.handle(Future.succeededFuture(result.result()));
            }
        });
    }

    private ThingResponse getErrorThingResponse(Integer status, String message) {
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_TEXT);
        return new ThingResponse(HttpResponseStatus.NOT_IMPLEMENTED, headers, message);
    }

}
