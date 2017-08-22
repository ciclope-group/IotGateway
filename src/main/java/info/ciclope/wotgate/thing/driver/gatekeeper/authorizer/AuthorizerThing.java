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

package info.ciclope.wotgate.thing.driver.gatekeeper.authorizer;

import info.ciclope.wotgate.WoTGateStates;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneId;

public class AuthorizerThing {
    private static final Integer TOKEN_LIFE_WINDOW = 3600;
    private final DatabaseStorage databaseStorage;

    public AuthorizerThing(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void generateUserToken(String userName, String userPassword, Handler<AsyncResult<JsonObject>> handler) {
        String query = "SELECT (password) FROM gatekeeper_users WHERE name = '" + userName + "';";
        databaseStorage.startSimpleConnection(sqlConnection -> {
            Integer connection = sqlConnection.result();
            databaseStorage.query(connection, query, resultSet -> {
                if (resultSet.failed()) {
                    databaseStorage.stopSimpleConnection(connection, stopResult -> {
                    });
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
                } else if (resultSet.result().getRows().isEmpty()) {
                    databaseStorage.stopSimpleConnection(connection, stopResult -> {
                    });
                    handler.handle(Future.failedFuture(HttpResponseStatus.UNAUTHORIZED.toString()));
                } else {
                    String hashedPassword = resultSet.result().getResults().get(0).getString(0);
                    if (arePasswordsIdentical(userPassword, hashedPassword)) {
                        JsonObject tokenObject = generateToken(userName, TOKEN_LIFE_WINDOW);
                        String insertSql = "UPDATE gatekeeper_users SET token='" + tokenObject.getString("token") + "',token_expiration_datetime='" + tokenObject.getString("expirationTime") + "' WHERE name='" + userName + "';";
                        databaseStorage.update(connection, insertSql, insertion -> {
                            databaseStorage.stopSimpleConnection(connection, stopResult -> {
                            });
                            if (insertion.failed()) {
                                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
                            } else {
                                handler.handle(Future.succeededFuture(tokenObject));
                            }
                        });
                    } else {
                        databaseStorage.stopSimpleConnection(connection, stopResult -> {
                        });
                        handler.handle(Future.failedFuture(HttpResponseStatus.UNAUTHORIZED.toString()));
                    }
                }
            });
        });
    }

    public void revokeUserToken(String userName, Handler<AsyncResult<Void>> handler) {
        if (userName == null) {
            handler.handle(Future.failedFuture(HttpResponseStatus.UNAUTHORIZED.toString()));
            return;
        }
        String query = "UPDATE gatekeeper_users SET token=null, token_expiration_datetime=null WHERE name = '" + userName + "';";
        databaseStorage.update(query, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.FORBIDDEN.toString(), result.cause())));
            } else {
                handler.handle(Future.succeededFuture());
            }
        });
    }

    public void getTokenOwner(String token, Handler<AsyncResult<String>> handler) {
        String query = "SELECT name FROM gatekeeper_users WHERE token='" + token + "';";
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
        String query = "SELECT json_group_array(roles.name) FROM gatekeeper_users AS users LEFT JOIN gatekeeper_users_in_role ON users.id = gatekeeper_users_in_role.user LEFT JOIN gatekeeper_roles AS roles ON roles.id = gatekeeper_users_in_role.role WHERE users.token='" + token + "' GROUP BY users.id, users.name;";
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
