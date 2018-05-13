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
import com.google.inject.Inject;
import info.ciclope.wotgate.WoTGateStates;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.AuthorityService;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.Calendar;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.Role;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.UserService;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import info.ciclope.wotgate.thingmanager.InteractionAuthorization;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

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
    private static final String THING_INTERACTION_CONFIRM_HASH_RECOVERY = "confirmPasswordRecovery";
    // Reservation interactions
    private static final String THING_INTERACTION_GET_ALL_RESERVATIONS_BY_DATE = "getAllReservationsByDate";
    private static final String THING_INTERACTION_GET_ALL_RESERVATIONS = "getAllReservations";
    private static final String THING_INTERACTION_GET_USER_RESERVATIONS_BY_DATE = "getUserReservationsByDate";
    private static final String THING_INTERACTION_GET_USER_RESERVATIONS_BY_NAME_AND_DATE = "getUserReservationsByNameAndDate";
    private static final String THING_INTERACTION_GET_ALL_USER_RESERVATIONS = "getAllUserReservations";
    private static final String THING_INTERACTION_GET_ALL_USER_RESERVATIONS_BY_NAME = "getAllUserReservationsByName";
    private static final String THING_INTERACTION_GET_DATE_AVAILABILITY = "getDateAvailability";
    private static final String THING_INTERACTION_ADD_USER_RESERVATION = "addUserReservation";
    private static final String THING_INTERACTION_DELETE_USER_RESERVATION = "deleteUserReservation";
    private static final String THING_INTERACTION_GET_ONGOING_RESERVATION = "getOngoingReservation";
    // Authorization interactions
    private static final String THING_INTERACTION_GENERATE_USER_TOKEN = "generateUserToken";
    private static final String THING_INTERACTION_REVOKE_USER_TOKEN = "revokeUserToken";
    private static final String THING_INTERACTION_GET_USER_PERMISSIONS = "getUserPermissions";

    private final long processExecution = 60000;

    private UserService userService;
    private Role role;

    @Inject
    private AuthorityService authorityService;
    private Calendar calendar;

    @Inject
    private GatekeeperDatabase gatekeeperDatabase;
    private JsonObject stateProperty;
    private String workingMode = WoTGateStates.MODE_UNRESTRICTED;
    private long deleteExpiredUserRegistrationsProcess;
    private long deleteExpiredPasswordRecoveriesProcess;

    @Override
    public String getThingDescriptionPath() {
        return THING_DESCRIPTION_PATH;
    }

    @Override
    public boolean loadThingExtraConfiguration() {
        return true;
    }

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        gatekeeperDatabase.initDatabaseStorage(result -> {
            if (result.succeeded()) {
                this.userService = new UserService(gatekeeperDatabase, getVertx());
                this.role = new Role(gatekeeperDatabase);
                this.calendar = new Calendar(gatekeeperDatabase);
                ObjectMapper objectMapper = new ObjectMapper();
                registerStateProperty(objectMapper);
                deleteExpiredUserRegistrationsProcess = getVertx().setPeriodic(processExecution, userService::deleteExpiredUserRegistrations);
                deleteExpiredPasswordRecoveriesProcess = getVertx().setPeriodic(processExecution, userService::deleteExpiredPasswordRecoveries);
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        register.addHandler(GateKeeperInfo.LOGIN, authorityService::login);
        register.addHandler(GateKeeperInfo.REGISTER, authorityService::register);
        register.addHandler(GateKeeperInfo.ACTIVATE_USER, authorityService::activateUser);

//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getState);
//        // Rol operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_ROLES, role::getAllRoles);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLE_BY_NAME, role::getRoleByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLES_BY_LEVEL, role::getRolesByLevel);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_ROLE, role::addRole);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_ROLE_BY_NAME, role::deleteRoleByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_TO_ROLE, role::addUserToRole);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_FROM_ROLE, role::deleteUserFromRole);
//        // User operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USERS, user::getAllUsers);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER, user::getUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_NAME, user::getUserByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_EMAIL, user::getUserByEmail);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER, user::deleteUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_BY_NAME, user::deleteUserByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CHANGE_USER_HASH, user::changeUserPassword);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REGISTER_USER, user::registerUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_USER_REGISTRATION, user::confirmUserRegistration);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_RECOVER_USER_HASH, user::recoverUserPassword);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_HASH_RECOVERY, user::confirmPasswordRecovery);
//        // Reservation operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_RESERVATIONS_BY_DATE, calendar::getAllReservationsByDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_RESERVATIONS, calendar::getAllReservations);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_RESERVATIONS_BY_DATE, calendar::getUserReservationsByDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_RESERVATIONS_BY_NAME_AND_DATE, calendar::getUserReservationsByNameAndDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USER_RESERVATIONS, calendar::getAllUserReservations);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USER_RESERVATIONS_BY_NAME, calendar::getAllUserReservationsByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_DATE_AVAILABILITY, calendar::getDateAvailability);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_RESERVATION, calendar::addUserReservation);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_RESERVATION, calendar::deleteUserReservation);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ONGOING_RESERVATION, calendar::getOngoingReservation);
//        // Authorization operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GENERATE_USER_TOKEN, authorityService::generateUserToken);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REVOKE_USER_TOKEN, authorityService::revokeUserToken);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_PERMISSIONS, authorityService::getUserPermissions);
//        getVertx().eventBus().consumer(ThingAddress.getThingInteractionAuthenticationAddress(), this::getAuthorization);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
        getVertx().cancelTimer(deleteExpiredUserRegistrationsProcess);
        getVertx().cancelTimer(deleteExpiredPasswordRecoveriesProcess);
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

        authorityService.getTokenOwner(token, ownerResult -> {
            if (ownerResult.failed()) {
                InteractionAuthorization authorization = new InteractionAuthorization("", new JsonArray());
                ThingResponse response = new ThingResponse(HttpResponseStatus.OK, new JsonObject(), authorization.getAccessInformation());
                message.reply(response.getResponse());
            } else {
                authorityService.getTokenOwnerRoles(token, rolesResult -> {
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

    private ThingResponse getErrorThingResponse(Integer status, String message) {
        JsonObject headers = new JsonObject();
        headers.put(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_TEXT);
        return new ThingResponse(status, headers, message);
    }

}
