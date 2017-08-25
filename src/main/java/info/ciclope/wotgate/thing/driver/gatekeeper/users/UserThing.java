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

package info.ciclope.wotgate.thing.driver.gatekeeper.users;

import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.driver.gatekeeper.authorizer.PasswordManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class UserThing {
    private final DatabaseStorage databaseStorage;

    public UserThing(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void registerUser(final JsonObject user, Handler<AsyncResult<Void>> handler) {
        JsonObject data = new JsonObject();
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        EmailValidator emailValidator = EmailValidator.getInstance();
        if ((user.containsKey("name") && user.getString("name").matches("(?=\\S+$).{5,}")) &&
                (user.containsKey("password") && user.getString("password").matches("(?=\\S+$).{8,}")) &&
                (user.containsKey("email") && emailValidator.isValid(user.getString("email")))) {
            // (?=.*[0-9]) a digit must occur at least once
            // (?=.*[a-z]) a lower case letter must occur at least once
            // (?=.*[A-Z]) an upper case letter must occur at least once
            // (?=.*[@#$%^&+=]) a special character must occur at least once
            // (?=\\S+$) no whitespace allowed in the entire string
            // .{8,} at least 8 characters
            String currentTimestamp = now.toString();
            data.put("name", user.getString("name"));
            data.put("email", user.getString("email"));
            data.put("online", false);
            data.put("dateCreated", currentTimestamp);
            data.put("dateModified", currentTimestamp);
        } else {
            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
            return;
        }
        // json: name, email, online, dateCreated, dateModified
        PasswordManager passwordManager = new PasswordManager();
        String insertUserSql = "INSERT INTO users (data, name, email, password, validated) VALUES ('" + data.toString() + "','" + user.getString("name") + "','" + user.getString("email") + "','" + passwordManager.hash(user.getString("password").toCharArray()) + "',0);";
        String insertUserRoleSql = "INSERT INTO users_in_role (user, role) VALUES (last_insert_rowid(), 3);";
        List<String> sqlBatch = new ArrayList<>();
        sqlBatch.add(insertUserSql);
        sqlBatch.add(insertUserRoleSql);
        databaseStorage.startTransactionConnection(sqlConnection -> {
            databaseStorage.executeBatch(sqlConnection.result(), sqlBatch, result -> {
                if (result.failed()) {
                    databaseStorage.stopTransactionConnection(sqlConnection.result(), stopTransaction -> {
                        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                    });
                } else {
                    databaseStorage.stopTransactionConnection(sqlConnection.result(), stopTransaction -> {
                        if (stopTransaction.succeeded()) {
                            handler.handle(Future.succeededFuture());
                        } else {
                            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), stopTransaction.cause())));
                        }
                    });
                }
            });
        });
    }

    public void getUsers(final Integer page, final Integer perPage, final String name, Handler<AsyncResult<JsonObject>> handler) {
        String query = "SELECT count(data) FROM users;";
        databaseStorage.startSimpleConnection(sqlConnection -> {
            databaseStorage.query(sqlConnection.result(), query, resultSet -> {
                if (resultSet.succeeded()) {
                    Integer parameterPage = page;
                    if (parameterPage == null) {
                        parameterPage = 0;
                    }
                    Integer parameterPerPage = perPage;
                    Integer lastIndex = resultSet.result().getRows().get(0).getInteger("count(data)");
                    if (parameterPerPage <= 0 || parameterPage < 0 || parameterPage * parameterPerPage >= lastIndex) {
                        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.RESOURCE_NOT_FOUND.toString(), resultSet.cause())));
                        return;
                    }
                    if (parameterPage > 100) {
                        parameterPerPage = 100;
                    }

                    Integer i = parameterPage * parameterPerPage;
                    Integer resultsPerPage = parameterPerPage;
                    JsonArray parameters = new JsonArray();
                    String sql = "SELECT json_group_array(user) FROM (SELECT json_insert(users.data,'$.roleNames',CASE WHEN (json_group_array(roles.name)='[null]') THEN json_array() ELSE json_group_array(roles.name) END) AS user FROM users LEFT JOIN users_in_role ON users.id = users_in_role.user LEFT JOIN roles ON roles.id = users_in_role.role ";
                    if (name != null) {
                        sql = sql.concat("WHERE users.name=? ");
                        parameters.add(name);
                    }
                    sql = sql.concat("GROUP BY users.id, users.name LIMIT ? OFFSET ? );");
                    parameters.add(resultsPerPage).add(i);
                    databaseStorage.queryWithParameters(sqlConnection.result(), sql, parameters, resultSet2 -> {
                        databaseStorage.stopSimpleConnection(sqlConnection.result(), stopResult -> {
                        });
                        if (resultSet2.succeeded()) {
                            if (resultSet2.result().getRows().isEmpty() || resultSet2.result().getResults().get(0).getString(0).contentEquals("[]")) {
                                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.RESOURCE_NOT_FOUND.toString(), resultSet2.cause())));
                            } else {
                                JsonArray jsonArray = new JsonArray(resultSet2.result().getResults().get(0).getString(0));
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.put("results", jsonArray);
                                jsonObject.put("total", lastIndex);
                                jsonObject.put("perPage", resultsPerPage);
                                handler.handle(Future.succeededFuture(jsonObject));
                            }
                        } else {
                            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet2.cause())));
                        }
                    });
                } else {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
                }
            });
        });
    }


}
