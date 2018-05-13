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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.storage.SqlArrayResult;
import info.ciclope.wotgate.storage.SqlObjectResult;
import info.ciclope.wotgate.storage.SqlStringResult;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.PasswordManager;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.Authority;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static info.ciclope.wotgate.thing.driver.gatekeeper.database.DatabaseSql.*;

@Singleton
public class GatekeeperDatabase {
    final DatabaseStorage databaseStorage;

    @Inject
    public GatekeeperDatabase(@Named("gatekeeper") DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void initDatabaseStorage(Handler<AsyncResult<Void>> handler) {
        List<String> batch = new ArrayList<>();
        batch.add(CREATE_USERS_TABLE);
        batch.add(CREATE_ROLES_TABLE);
        batch.add(CREATE_USER_ROLE_TABLE);
//        batch.add(CREATE_USER_REGISTRATION_TABLE);
//        batch.add(CREATE_PASSWORD_RECOVERY_TABLE);
//        batch.add(CREATE_RESERVATIONS_TABLE);
//        batch.add(INSERT_ROLE_ADMINISTRATOR);
//        batch.add(INSERT_ROLE_PRIVILEGED);
//        batch.add(INSERT_ROLE_AUTHENTICATED);
//        batch.add(INSERT_USER_ADMINISTRATOR);
//        batch.add(INSERT_USER_PRIVILEGED);
//        batch.add(INSERT_USER_AUTHENTICATED);
//        batch.add(INSERT_USER_ROLE_ADMINISTRATOR);
//        batch.add(INSERT_USER_ROLE_PRIVILEGED);
//        batch.add(INSERT_USER_ROLE_AUTHENTICATED);

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

    public void getUserByUsername(String username, Handler<AsyncResult<User>> handler) {
        String query = "SELECT * FROM user WHERE username = ?";
        JsonArray params = new JsonArray().add(username);

        databaseStorage.queryWithParameters(query, params, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() != 0) {
                    handler.handle(Future.succeededFuture(new User(result.result())));
                } else {
                    handler.handle(Future.succeededFuture(null));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUserAuthorities(String username, Handler<AsyncResult<List<Authority>>> handler) {
        String query = "SELECT * FROM authority a " +
                "JOIN user_authority ua on a.id = ua.authority_id JOIN user u on ua.user_id = u.id " +
                "WHERE u.username = ?";
        JsonArray params = new JsonArray().add(username);

        databaseStorage.queryWithParameters(query, params, result -> {
            if (result.succeeded()) {
                List<Authority> authorities = new ArrayList<>();
                for (JsonObject object : result.result().getRows()) {
                    authorities.add(new Authority(object));
                }

                handler.handle(Future.succeededFuture(authorities));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void insertUser(User user, Handler<AsyncResult<Integer>> handler) {
        String query = "INSERT INTO user(username, email, password, enabled) VALUES (?, ?, ?, 0);";
        JsonArray params = new JsonArray().add(user.getUsername()).add(user.getEmail()).add(user.getPassword());

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(result.result().getKeys().getInteger(0)));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void addUserRole(int userId, String roleName, Handler<AsyncResult> handler) {
        String query = "INSERT INTO user_authority SELECT ?, id FROM authority WHERE name = ?";
        JsonArray params = new JsonArray().add(userId).add(roleName);

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void activateUser(int id, Handler<AsyncResult> handler) {
        String query = "UPDATE user SET enabled = 1 WHERE id = ?";
        JsonArray params = new JsonArray().add(id);

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
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

    public void insertUserToken(String name, String password, String token, String tokenExpirationDateTime, Handler<AsyncResult<SqlStringResult>> handler) {
        databaseStorage.startSimpleConnection(connectionResult -> {
            if (connectionResult.succeeded()) {
                Integer connection = connectionResult.result();
                JsonArray queryParameters = new JsonArray().add(name);
                databaseStorage.queryWithParameters(connection, GET_USER_HASH, queryParameters, queryResult -> {
                    if (queryResult.succeeded() && queryResult.result().getNumRows() > 0 &&
                            arePasswordsIdentical(password, queryResult.result().getResults().get(0).getString(0))) {
                        JsonArray parameters = new JsonArray().add(token).add(tokenExpirationDateTime).add(name);
                        databaseStorage.updateWithParameters(connection, ADD_USER_TOKEN, parameters, update -> {
                            if (update.succeeded()) {
                                databaseStorage.stopSimpleConnection(connection, stopResult -> {
                                });
                                handler.handle(Future.succeededFuture(new SqlStringResult("", update.result().getUpdated())));
                            } else {
                                handler.handle(Future.failedFuture(update.cause()));
                            }
                        });

                    } else {
                        handler.handle(Future.failedFuture(queryResult.cause()));
                    }
                });
            } else {
                handler.handle(Future.failedFuture(connectionResult.cause()));
            }
        });
    }

    public void deleteUserToken(String name, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.updateWithParameters(REVOKE_USER_TOKEN, parameters, update -> {
            if (update.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", update.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(update.cause()));
            }
        });
    }

    public void getUserPermissions(String name, Handler<AsyncResult<SqlObjectResult>> handler) {
        JsonArray parameters = new JsonArray().add(name).add(name).add(name);
        databaseStorage.queryWithParameters(GET_USER_PERMISSIONS, parameters, query -> {
            if (query.succeeded()) {
                JsonObject permissionsObject = parseUserPermissions(new JsonObject(query.result().getResults().get(0).getString(0)));
                handler.handle(Future.succeededFuture(new SqlObjectResult(permissionsObject, query.result().getNumRows())));
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void getAllReservationsByDate(String date, Handler<AsyncResult<SqlArrayResult>> handler) {
        JsonArray parameters = new JsonArray().add(date);
        databaseStorage.queryWithParameters(GET_ALL_RESERVATIONS_BY_DATE, parameters, query -> {
            if (query.succeeded()) {
                if (query.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(query.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void getAllReservations(Handler<AsyncResult<SqlArrayResult>> handler) {
        databaseStorage.query(GET_ALL_RESERVATIONS, query -> {
            if (query.succeeded()) {
                if (query.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(query.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void getReservationsByDateAndUser(String name, String date, Handler<AsyncResult<SqlArrayResult>> handler) {
        JsonArray parameters = new JsonArray().add(date).add(name);
        databaseStorage.queryWithParameters(GET_ALL_RESERVATIONS_BY_DATE_AND_USER, parameters, query -> {
            if (query.succeeded()) {
                if (query.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(query.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void getAllUserReservations(String name, Handler<AsyncResult<SqlArrayResult>> handler) {
        JsonArray parameters = new JsonArray().add(name);
        databaseStorage.queryWithParameters(GET_ALL_RESERVATIONS_BY_USER, parameters, query -> {
            if (query.succeeded()) {
                if (query.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlArrayResult(new JsonArray(), 0)));
                } else {
                    JsonArray resultArray = new JsonArray(query.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlArrayResult(resultArray, resultArray.size())));
                }
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void addUserReservation(String name, String startDate, String endDate, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(startDate).add(endDate).add(name);
        databaseStorage.updateWithParameters(ADD_RESERVATION, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void deleteUserReservation(String name, String startDate, Handler<AsyncResult<SqlStringResult>> handler) {
        JsonArray parameters = new JsonArray().add(startDate).add(name);
        databaseStorage.updateWithParameters(DELETE_RESERVATION_BY_DATE, parameters, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", result.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getOngoingReservation(Handler<AsyncResult<SqlObjectResult>> handler) {
        databaseStorage.query(GET_ACTIVE_RESERVATION, query -> {
            if (query.succeeded()) {
                if (query.result().getNumRows() == 0) {
                    handler.handle(Future.succeededFuture(new SqlObjectResult(new JsonObject(), 0)));
                } else {
                    JsonObject resultObject = new JsonObject(query.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(new SqlObjectResult(resultObject, query.result().getNumRows())));
                }
            } else {
                handler.handle(Future.failedFuture(query.cause()));
            }
        });
    }

    public void deleteExpiredUserRegistrations(Handler<AsyncResult<SqlStringResult>> handler) {
        databaseStorage.update(DELETE_EXPIRED_USER_REGISTRATIONS, update -> {
            if (update.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", update.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(update.cause()));
            }
        });
    }

    public void deleteExpiredPasswordRecoveries(Handler<AsyncResult<SqlStringResult>> handler) {
        databaseStorage.update(DELETE_EXPIRED_HASH_RECOVERIES, update -> {
            if (update.succeeded()) {
                handler.handle(Future.succeededFuture(new SqlStringResult("", update.result().getUpdated())));
            } else {
                handler.handle(Future.failedFuture(update.cause()));
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

    private JsonObject parseUserPermissions(JsonObject userPermissions) {
        String onlineKey = "reservationOngoing";
        JsonObject permissions = userPermissions.copy();
        if (permissions.containsKey(onlineKey)) {
            permissions.put(onlineKey, permissions.getInteger(onlineKey) > 0);
        }
        return permissions;
    }

    private boolean arePasswordsIdentical(String password, String hashedPassword) {
        PasswordManager passwordManager = new PasswordManager();

        return passwordManager.authenticate(password.toCharArray(), hashedPassword);
    }
}
