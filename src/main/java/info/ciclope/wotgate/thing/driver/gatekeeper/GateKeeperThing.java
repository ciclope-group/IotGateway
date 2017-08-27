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
import info.ciclope.wotgate.thing.component.ThingAddress;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingRequestParameter;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.Authorizer;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.Calendar;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.Role;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.User;
import info.ciclope.wotgate.thing.handler.ThingHandlerRegister;
import info.ciclope.wotgate.thingmanager.InteractionAuthorization;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class GateKeeperThing extends AbstractThing {
    private static final String THING_DESCRIPTION_PATH = "things/gatekeeper/ThingDescription.json";
    private static final String THING_INTERACTION_STATE = "state";
    // Role interactions
    private static final String THING_INTERACTION_GET_ALL_ROLES = "getAllRoles";
    private static final String THING_INTERACTION_GET_ROLE_BY_NAME = "getRoleByName";
    private static final String THING_INTERACTION_GET_ROLES_BY_LEVEL = "getRolesByLevel";
    private static final String THING_INTERACTION_ADD_ROLE = "addRole";
    private static final String THING_INTERACTION_DELETE_ROLE_BY_NAME = "deleteRoleByName";
    private static final String THING_INTERACTION_ADD_USER_TO_ROLE = "addUserToRole";
    private static final String THING_INTERACTION_DELETE_USER_FROM_ROLE = "deleteUserFromRole";
    // User interactions
    private static final String THING_INTERACTION_GET_ALL_USERS = "getAllUsers";
    private static final String THING_INTERACTION_GET_USER = "getUser";
    private static final String THING_INTERACTION_GET_USER_BY_NAME = "getUserByName";
    private static final String THING_INTERACTION_GET_USER_BY_EMAIL = "getUserByEmail";
    private static final String THING_INTERACTION_DELETE_USER = "deleteUser";
    private static final String THING_INTERACTION_DELETE_USER_BY_NAME = "deleteUserByName";
    private static final String THING_INTERACTION_CHANGE_USER_HASH = "changeUserPassword";
    private static final String THING_INTERACTION_REGISTER_USER = "registerUser";
    private static final String THING_INTERACTION_CONFIRM_USER_REGISTRATION = "confirmUserRegistration";
    private static final String THING_INTERACTION_RECOVER_USER_HASH = "recoverUserPassword";
    // Reservation interactions
    private static final String THING_INTERACTION_SEARCH_RESERVATIONS = "searchReservations";
    private static final String THING_INTERACTION_ADD_USER_RESERVATION = "addUserReservation";
    private static final String THING_INTERACTION_DELETE_USER_RESERVATION = "deleteUserReservation";
    private static final String THING_INTERACTION_ACK_RESERVATION = "ackReservation";
    // Authorization interactions
    private static final String THING_INTERACTION_GENERATE_USER_TOKEN = "generateUserToken";
    private static final String THING_INTERACTION_REVOKE_USER_TOKEN = "revokeUserToken";
    private static final String THING_INTERACTION_GET_AUTHORIZATION = "getAuthorization";

    private User user;
    private Role role;
    private Authorizer authorizer;
    private Calendar calendar;
    private GatekeeperDatabase gatekeeperDatabase;
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
        // Rol operations
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_ROLES, role::getAllRoles);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLE_BY_NAME, role::getRoleByName);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLES_BY_LEVEL, role::getRolesByLevel);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_ROLE, role::addRole);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_ROLE_BY_NAME, role::deleteRoleByName);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_TO_ROLE, role::addUserToRole);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_FROM_ROLE, role::deleteUserFromRole);
        // User operations
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USERS, user::getAllUsers);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER, user::getUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_NAME, user::getUserByName);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_EMAIL, user::getUserByEmail);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER, user::deleteUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_BY_NAME, user::deleteUserByName);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CHANGE_USER_HASH, user::changeUserPassword);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REGISTER_USER, this::registerUser);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_USER_REGISTRATION, this::confirmUserRegistration);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_RECOVER_USER_HASH, this::recoverUserPassword);
        // Reservation operations
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_SEARCH_RESERVATIONS, this::searchReservations);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_RESERVATION, this::addUserReservation);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_RESERVATION, this::deleteUserReservation);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ACK_RESERVATION, this::ackReservation);
        // Authorization operations
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GENERATE_USER_TOKEN, this::generateUserToken);
        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REVOKE_USER_TOKEN, this::revokeUserToken);
        vertx.eventBus().consumer(ThingAddress.getThingInteractionAuthenticationAddress(), this::getAuthorization);
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        this.gatekeeperDatabase = new GatekeeperDatabase(databaseStorage);
        gatekeeperDatabase.initDatabaseStorage(result -> {
            if (result.succeeded()) {
                this.user = new User(gatekeeperDatabase);
                this.role = new Role(gatekeeperDatabase);
                this.authorizer = new Authorizer(databaseStorage);
                this.calendar = new Calendar(databaseStorage);
                ObjectMapper objectMapper = new ObjectMapper();
                registerStateProperty(objectMapper);
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
        URL url = getClass().getClassLoader().getResource("things/gatekeeper/GateKeeperStateProperty.json");
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

    private void searchReservations(Message<JsonObject> message) {
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

        calendar.getCalendar(inputData, request.getInteractionAuthorization().getUsername(), calendarResult -> {
            if (calendarResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(calendarResult.cause().getMessage()), "").getResponse());
            } else {
                ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), calendarResult.result());
                message.reply(response.getResponse());
            }
        });
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
        authorizer.generateUserToken(authorization[0], authorization[1], result -> {
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
                authorizer.revokeUserToken(getResult.result(), revokeResult -> {
                    if (revokeResult.failed()) {
                        message.reply(getErrorThingResponse(Integer.decode(revokeResult.cause().getMessage()), "").getResponse());
                    }
                    ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
                    message.reply(response.getResponse());
                });
            }
        });
    }

//    private void registerUser(Message<JsonObject> message) {
//        ThingRequest request = new ThingRequest(message.body());
//        JsonObject data = request.getBody();
//        if (data == null) {
//            message.reply(getErrorThingResponse(HttpResponseStatus.BAD_REQUEST, "").getResponse());
//            return;
//        }
//        user.registerUser(data, result -> {
//            if (result.failed()) {
//                message.reply(getErrorThingResponse(Integer.decode(result.cause().getMessage()), "").getResponse());
//                return;
//            }
//            ThingResponse response = new ThingResponse(HttpResponseStatus.CREATED, new JsonObject(), "");
//            message.reply(response.getResponse());
//        });
//    }
//

    private void addUserReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName = request.getInteractionAuthorization().getUsername();
        JsonObject reservationData;
        reservationData = request.getBody();
        calendar.addUserReservation(reservationData, userName, reservationResult -> {
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
        calendar.deleteUserReservation(reservationData, userName, deleteReservationResult -> {
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
        calendar.ackReservation(userName, ackResult -> {
            if (ackResult.failed()) {
                message.reply(getErrorThingResponse(Integer.decode(ackResult.cause().getMessage()), "").getResponse());
            } else {
                this.stateProperty.put("ackReservation", true);
                ThingResponse response = new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "");
                message.reply(response.getResponse());
            }
        });
    }

    public void setWotGateWorkingMode(String mode) {
        workingMode = mode;
    }

    private void getAuthorization(Message<String> message) {
        final String token;
        if (message.body() != null && !message.body().isEmpty()) {
            token = message.body();
        } else {
            InteractionAuthorization authorization = new InteractionAuthorization("", new JsonArray());
            ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), authorization.getAccessInformation());
            message.reply(response.getResponse());
            return;
        }

        authorizer.getTokenOwner(token, ownerResult -> {
            if (ownerResult.failed()) {
                InteractionAuthorization authorization = new InteractionAuthorization("", new JsonArray());
                ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), authorization.getAccessInformation());
                message.reply(response.getResponse());
            } else {
                authorizer.getTokenOwnerRoles(token, rolesResult -> {
                    if (rolesResult.succeeded()) {
                        InteractionAuthorization authorization = new InteractionAuthorization(ownerResult.result(), rolesResult.result());
                        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), authorization.getAccessInformation());
                        message.reply(response.getResponse());
                    } else {
                        InteractionAuthorization authorization = new InteractionAuthorization(ownerResult.result(), new JsonArray());
                        ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), authorization.getAccessInformation());
                        message.reply(response.getResponse());
                    }
                });
            }
        });
    }

    public void getTokenOwner(String token, Handler<AsyncResult<String>> handler) {
        authorizer.getTokenOwner(token, result -> {
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
        return new ThingResponse(status, headers, message);
    }

}
