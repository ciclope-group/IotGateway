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
        batch.add(CREATE_USER_REGISTRATION_TABLE);
        batch.add(CREATE_PASSWORD_RECOVERY_TABLE);
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
                    JsonArray resultArray = parseUsers(new JsonArray(result.result().getResults().get(0).getString(0)));
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
                    JsonObject roleObject = parseUser(new JsonObject(result.result().getResults().get(0).getString(0)));
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
                    JsonObject roleObject = parseUser(new JsonObject(result.result().getResults().get(0).getString(0)));
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
                    JsonObject roleObject = parseUser(new JsonObject(result.result().getResults().get(0).getString(0)));
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

    public void registerUser(String name, String email, String password, String token, String expirationDateTime,
                             Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray registerParameters = new JsonArray().add(name).add(email).add(password);
        JsonArray registrationParameters = new JsonArray().add(token).add(name).add(expirationDateTime);
        databaseStorage.startTransactionConnection(connectionResult -> {
            if (connectionResult.succeeded()) {
                Integer connection = connectionResult.result();
                databaseStorage.updateWithParameters(connection, REGISTER_USER, registerParameters, register -> {
                    if (register.succeeded()) {
                        databaseStorage.updateWithParameters(connection, CREATE_USER_REGISTRATION, registrationParameters, registration -> {
                            if (registration.succeeded()) {
                                databaseStorage.commitTransaction(connection, commit -> {
                                    if (commit.succeeded()) {
                                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                            handler.handle(Future.succeededFuture(new SqlStringResult("", registration.result().getUpdated())));
                                        });
                                    } else {
                                        databaseStorage.rollbackTransaction(connection, rollback -> {
                                            databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                                handler.handle(Future.failedFuture(commit.cause()));
                                            });
                                        });
                                    }
                                });
                            } else {
                                databaseStorage.rollbackTransaction(connection, rollback -> {
                                    databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                        handler.handle(Future.failedFuture(registration.cause()));
                                    });
                                });
                            }
                        });
                    } else {
                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                            handler.handle(Future.failedFuture(register.cause()));
                        });
                    }
                });
            } else {
                handler.handle(Future.failedFuture(connectionResult.cause()));
            }
        });

    }

    public void validateUser(String name, String email, String token, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray updateParameters = new JsonArray().add(token).add(name).add(email);
        JsonArray deleteParameters = new JsonArray().add(token);
        databaseStorage.startTransactionConnection(connectionResult -> {
            if (connectionResult.succeeded()) {
                Integer connection = connectionResult.result();
                databaseStorage.updateWithParameters(connection, VALIDATE_USER, updateParameters, validate -> {
                    if (validate.succeeded()) {
                        databaseStorage.updateWithParameters(connection, DELETE_USER_REGISTRATION, deleteParameters, delete -> {
                            if (delete.succeeded()) {
                                databaseStorage.commitTransaction(connection, commit -> {
                                    if (commit.succeeded()) {
                                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                            handler.handle(Future.succeededFuture(new SqlStringResult("", delete.result().getUpdated())));
                                        });
                                    } else {
                                        databaseStorage.rollbackTransaction(connection, rollback -> {
                                            databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                                handler.handle(Future.failedFuture(commit.cause()));
                                            });
                                        });
                                    }
                                });
                            } else {
                                databaseStorage.rollbackTransaction(connection, rollback -> {
                                    databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                        handler.handle(Future.failedFuture(delete.cause()));
                                    });
                                });
                            }
                        });
                    } else {
                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                            handler.handle(Future.failedFuture(validate.cause()));
                        });
                    }
                });
            } else {
                handler.handle(Future.failedFuture(connectionResult.cause()));
            }
        });
    }

    public void recoverUserPassword(String token, String name, String email, String password, String expirationDateTime, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(token).add(name).add(email).add(password).add(expirationDateTime);
        databaseStorage.updateWithParameters(REQUEST_NEW_USER_HASH, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void validatePasswordRecovery(String name, String email, String token, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray recoverParameters = new JsonArray().add(name).add(email).add(token).add(token).add(name).add(email);
        JsonArray deleteParameters = new JsonArray().add(token);
        databaseStorage.startTransactionConnection(connectionResult -> {
            if (connectionResult.succeeded()) {
                Integer connection = connectionResult.result();
                databaseStorage.updateWithParameters(connection, RECOVER_HASH, recoverParameters, recover -> {
                    if (recover.succeeded()) {
                        databaseStorage.updateWithParameters(connection, DELETE_HASH_RECOVERY, deleteParameters, delete -> {
                            if (delete.succeeded()) {
                                databaseStorage.commitTransaction(connection, commit -> {
                                    if (commit.succeeded()) {
                                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                            handler.handle(Future.succeededFuture(new SqlStringResult("", delete.result().getUpdated())));
                                        });
                                    } else {
                                        databaseStorage.rollbackTransaction(connection, rollback -> {
                                            databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                                handler.handle(Future.failedFuture(commit.cause()));
                                            });
                                        });
                                    }
                                });
                            } else {
                                databaseStorage.rollbackTransaction(connection, rollback -> {
                                    databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                                        handler.handle(Future.failedFuture(delete.cause()));
                                    });
                                });
                            }
                        });
                    } else {
                        databaseStorage.stopTransactionConnection(connection, stopTransaction -> {
                            handler.handle(Future.failedFuture(recover.cause()));
                        });
                    }
                });
            } else {
                handler.handle(Future.failedFuture(connectionResult.cause()));
            }
        });

    }

    private JsonArray parseUsers(JsonArray users) {
        JsonArray parsedUsers = new JsonArray();
        for (Object user : users) {
            parsedUsers.add(parseUser((JsonObject) user));
        }

        return parsedUsers;
    }

    private JsonObject parseUser(JsonObject user) {
        String validatedKey = "validated";
        String onlineKey = "online";
        JsonObject parsedUser = user.copy();
        if (parsedUser.containsKey(validatedKey)) {
            parsedUser.put(validatedKey, parsedUser.getInteger(validatedKey) > 0);
        }
        if (parsedUser.containsKey(onlineKey)) {
            parsedUser.put(onlineKey, parsedUser.getInteger(onlineKey) > 0);
        }
        return parsedUser;
    }

}
