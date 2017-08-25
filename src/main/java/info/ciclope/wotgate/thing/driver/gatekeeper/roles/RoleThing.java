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

package info.ciclope.wotgate.thing.driver.gatekeeper.roles;

import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.ZoneId;

public class RoleThing {
    private final DatabaseStorage databaseStorage;

    public RoleThing(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void getRoles(final Integer page, final Integer perPage, final String name, Handler<AsyncResult<JsonObject>> handler) {
        String query = "SELECT count(*) FROM roles;";
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

                    String sql = "SELECT json_group_array(role) FROM (SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END), 'dateCreated', roles.dateCreated, 'dateModified', roles.dateModified) AS role FROM roles LEFT JOIN users_in_role ON roles.id = users_in_role.role LEFT JOIN users ON users.id = users_in_role.user ";
                    if (name != null) {
                        sql = sql.concat("WHERE roles.name = ? ");
                        parameters.add(name);
                    }
                    sql = sql.concat("GROUP BY roles.id, roles.name LIMIT ? OFFSET ? );");
                    parameters.add(resultsPerPage).add(i);
                    databaseStorage.queryWithParameters(sqlConnection.result(), sql, parameters, resultSet2 -> {
                        databaseStorage.stopSimpleConnection(sqlConnection.result(), resultStop -> {
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

    public void addRole(String name, Integer level, Handler<AsyncResult<Void>> handler) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentDateTime = now.toString();
        String sql = "INSERT INTO roles (name, level, dateCreated, dateModified) VALUES ('" + name + "','" + level.toString() + "','" + currentDateTime + "','" + currentDateTime + "');";

        databaseStorage.update(sql, resultUpdate -> {
            if (resultUpdate.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(resultUpdate.cause()));
            }
        });
    }

    public void addUsersToRole(JsonArray userNames) {

    }

    public void deleteUsersFromRole(JsonArray userNames) {

    }
}
