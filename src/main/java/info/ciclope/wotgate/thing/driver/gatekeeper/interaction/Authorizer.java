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

package info.ciclope.wotgate.thing.driver.gatekeeper.interaction;

import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.component.ThingActionTask;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;

public class Authorizer {
    private static final Integer TOKEN_LIFE_WINDOW = 3600;
    private final DatabaseStorage databaseStorage;
    private final GatekeeperDatabase database;

    public Authorizer(DatabaseStorage databaseStorage, GatekeeperDatabase database) {
        this.databaseStorage = databaseStorage;
        this.database = database;
    }

    public void getTokenOwner(String token, Handler<AsyncResult<String>> handler) {
        String query = "SELECT name FROM users WHERE token='" + token + "';";
        databaseStorage.query(query, resultSet -> {
            if (resultSet.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
            } else if (resultSet.result().getRows().isEmpty()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.UNAUTHORIZED.toString())));
            } else {
                handler.handle(Future.succeededFuture(resultSet.result().getResults().get(0).getString(0)));
            }
        });
    }

    public void getTokenOwnerRoles(String token, Handler<AsyncResult<JsonArray>> handler) {
        String query = "SELECT json_group_array(roles.name) FROM users LEFT JOIN user_in_role ON users.id = user_in_role.user LEFT JOIN roles ON roles.id = user_in_role.role WHERE users.token='" + token + "' GROUP BY users.id, users.name;";
        databaseStorage.query(query, resultSet -> {
            if (resultSet.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
            } else if (resultSet.result().getRows().isEmpty()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.UNAUTHORIZED.toString())));
            } else {
                handler.handle(Future.succeededFuture(new JsonArray(resultSet.result().getResults().get(0).getString(0))));
            }
        });
    }

    public void generateUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userPassword = request.getHeader(HttpHeader.HEADER_AUTHORIZATION);
        if (userPassword == null || userPassword.length() < 9 || !userPassword.substring(0, 6).equals("Basic ")) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        byte[] bytes = Base64.getDecoder().decode(userPassword.substring(6));
        if (bytes == null) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        userPassword = new String(bytes);
        String[] authorization = userPassword.split(":", 2);
        if (authorization == null || authorization.length < 2) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        JsonObject tokenObject = generateToken(authorization[0], TOKEN_LIFE_WINDOW);

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.insertUserToken(authorization[0], authorization[1],
                tokenObject.getString("token"), tokenObject.getString("expirationTime"), result -> {
                    if (result.succeeded()) {
                        if (result.result().getTotal() > 0) {
                            task.setStatus(ThingActionTask.TASK_STATUS_OK);
                            task.setOutputData(tokenObject);
                            message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                        } else {
                            task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                        }
                    } else {
                        task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                        message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                    }
                });
    }

    public void revokeUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserToken(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getUserPermissions(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getUserPermissions(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    task.setOutputData(result.result().getResult());
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    private JsonObject generateToken(String userName, long lifeTimeSeconds) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentTimeStamp = now.plusSeconds(lifeTimeSeconds).toString();
        PasswordManager tokenManager = new PasswordManager();

        String token = tokenManager.hash((userName + currentTimeStamp).toCharArray());
        JsonObject result = new JsonObject();
        result.put("token", token);
        result.put("expirationTime", currentTimeStamp);
        result.put("lifeWindow", lifeTimeSeconds);

        return result;
    }

    private boolean arePasswordsIdentical(String password, String hashedPassword) {
        PasswordManager passwordManager = new PasswordManager();

        return passwordManager.authenticate(password.toCharArray(), hashedPassword);
    }

}
