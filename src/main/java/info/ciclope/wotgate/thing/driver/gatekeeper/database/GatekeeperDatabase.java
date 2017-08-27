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

package info.ciclope.wotgate.thing.driver.gatekeeper.database;

import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.storage.SqlArrayResult;
import info.ciclope.wotgate.storage.SqlObjectResult;
import info.ciclope.wotgate.storage.SqlStringResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static info.ciclope.wotgate.thing.driver.gatekeeper.database.DatabaseSql.*;

public class GatekeeperDatabase {
    final DatabaseStorage databaseStorage;

    public GatekeeperDatabase(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void initDatabaseStorage(Handler<AsyncResult<Void>> handler) {
        List<String> batch = new ArrayList<>();
        batch.add(CREATE_USERS_TABLE);
        batch.add(CREATE_ROLES_TABLE);
        batch.add(CREATE_USER_ROLE_TABLE);
        batch.add(CREATE_RESERVATIONS_TABLE);
        batch.add(INSERT_ROLE_ADMINISTRATOR);
        batch.add(INSERT_ROLE_PRIVILEGED);
        batch.add(INSERT_ROLE_AUTHENTICATED);
        batch.add(INSERT_USER_ADMINISTRATOR);
        batch.add(INSERT_USER_PRIVILEGED);
        batch.add(INSERT_USER_AUTHENTICATED);
        batch.add(INSERT_USER_ROLE_ADMINISTRATOR);
        batch.add(INSERT_USER_ROLE_PRIVILEGED);
        batch.add(INSERT_USER_ROLE_AUTHENTICATED);

        databaseStorage.executeBatch(batch, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void insertRole(String name, Integer level, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(name).add(level);
        databaseStorage.updateWithParameters(INSERT_ROLE, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getAllRoles(Handler<AsyncResult<SqlArrayResult>> handler) {
        databaseStorage.query(GET_ALL_ROLES, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getRoleByName(String name, Handler<AsyncResult<SqlObjectResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.queryWithParameters(GET_ROLE_BY_NAME, parameters, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlObjectResult(new JsonObject(), 0)));
                } else {
                    JsonObject roleObject = new JsonObject(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlObjectResult(roleObject, result.result().getNumRows())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getRolesByLevel(Integer level, Handler<AsyncResult<SqlArrayResult>> handler) {
        JsonArray parameters = new JsonArray().add(level);
        databaseStorage.queryWithParameters(GET_ROLES_BY_LEVEL, parameters, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void deleteRoleByName(String name, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.updateWithParameters(DELETE_ROLE_BY_NAME, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void addUserToRole(String userName, String roleName, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(userName).add(roleName);
        databaseStorage.updateWithParameters(ADD_USER_ROLE, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void deleteUserFromRole(String userName, String roleName, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(userName).add(roleName);
        databaseStorage.updateWithParameters(DELETE_USER_ROLE, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getAllUsers(Handler<AsyncResult<SqlArrayResult>> handler) {
        databaseStorage.query(GET_ALL_USERS, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUser(String name, Handler<AsyncResult<SqlObjectResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.queryWithParameters(GET_USER, parameters, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlObjectResult(new JsonObject(), 0)));
                } else {
                    JsonObject roleObject = new JsonObject(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlObjectResult(roleObject, result.result().getNumRows())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUserByName(String name, Handler<AsyncResult<SqlObjectResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.queryWithParameters(GET_USER_BY_NAME, parameters, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlObjectResult(new JsonObject(), 0)));
                } else {
                    JsonObject roleObject = new JsonObject(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlObjectResult(roleObject, result.result().getNumRows())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUserByEmail(String email, Handler<AsyncResult<SqlObjectResult>> handler) {
        JsonArray parameters = new JsonArray().add(email);
        databaseStorage.queryWithParameters(GET_USER_BY_EMAIL, parameters, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlObjectResult(new JsonObject(), 0)));
                } else {
                    JsonObject roleObject = new JsonObject(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlObjectResult(roleObject, result.result().getNumRows())));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void deleteUserByName(String name, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.updateWithParameters(DELETE_USER_BY_NAME, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void updateUserPassword(String name, String password, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(password).add(name);
        databaseStorage.updateWithParameters(UPDATE_USER_HASH, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }


}
