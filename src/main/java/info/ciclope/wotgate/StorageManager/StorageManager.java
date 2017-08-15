/*
 * Copyright (c) 2017, Javier Martínez Villacampa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.ciclope.wotgate.StorageManager;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.HashMap;
import java.util.List;

public class StorageManager implements StorageManagerInterface {
    private static final String NO_SQL_CONNECTION_ERROR = "No SQL connection available.";

    private final Vertx vertx;
    private JDBCClient jdbcClient;
    private HashMap<Integer, SQLConnection> SqlConnectionMap;

    public StorageManager(Vertx vertx) {
        this.vertx = vertx;
        SqlConnectionMap = new HashMap<>();
    }

    @Override
    public void startStorageManager(String storageName) {
        if (jdbcClient != null) {
            return;
        }

        JsonObject configuration = new JsonObject()
                .put("url", "jdbc:sqlite:wotgate.db")
                .put("driver_class", "org.sqlite.JDBC");
        jdbcClient = JDBCClient.createShared(this.vertx, configuration, storageName);
    }

    @Override
    public void stopStorageManager() {
        if (jdbcClient != null) {
            jdbcClient.close();
            jdbcClient = null;
        }
    }

    @Override
    public void startSimpleConnection(Handler<AsyncResult<Integer>> result) {
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnectionMap.put(connection.result().hashCode(), connection.result());
                result.handle(Future.succeededFuture(connection.result().hashCode()));
            } else {
                result.handle(Future.failedFuture(connection.cause()));
            }
        });
    }

    @Override
    public void query(Integer connection, String query, Handler<AsyncResult<ResultSet>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.query(query, next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture(next.result()));
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void queryWithParameters(Integer connection, String query, JsonArray parameters, Handler<AsyncResult<ResultSet>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.queryWithParams(query, parameters, next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture(next.result()));
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void update(Integer connection, String update, Handler<AsyncResult<UpdateResult>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.update(update, next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture(next.result()));
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void updateWithParameters(Integer connection, String update, JsonArray parameters, Handler<AsyncResult<UpdateResult>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.updateWithParams(update, parameters, next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture(next.result()));
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void executeBatch(Integer connection, List<String> batch, Handler<AsyncResult<Void>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.batch(batch, next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture());
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void stopSimpleConnection(Integer connection, Handler<AsyncResult<Void>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.close(next -> {
            if (next.succeeded()) {
                result.handle(Future.succeededFuture());
            } else {
                result.handle(Future.failedFuture(next.cause()));
            }
        });
    }

    @Override
    public void startTransactionConnection(Handler<AsyncResult<Integer>> result) {
        startSimpleConnection(connection -> {
            if (connection.failed()) {
                result.handle(Future.failedFuture(connection.cause()));
                return;
            }
            SQLConnection sqlConnection = SqlConnectionMap.get(connection.result());
            sqlConnection.setAutoCommit(false, setAutoCommit -> {
                if (setAutoCommit.succeeded()) {
                    result.handle(Future.succeededFuture(connection.result()));
                } else {
                    result.handle(Future.failedFuture(setAutoCommit.cause()));
                }
            });
        });
    }

    @Override
    public void commitTransaction(Integer connection, Handler<AsyncResult<Void>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.commit(commit -> {
            if (commit.succeeded()) {
                result.handle(Future.succeededFuture(commit.result()));
            } else {
                result.handle(Future.failedFuture(commit.cause()));
            }
        });
    }

    @Override
    public void rollbackTransaction(Integer connection, Handler<AsyncResult<Void>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.rollback(rollback -> {
            if (rollback.succeeded()) {
                result.handle(Future.succeededFuture(rollback.result()));
            } else {
                result.handle(Future.failedFuture(rollback.cause()));
            }
        });
    }

    @Override
    public void stopTransactionConnection(Integer connection, Handler<AsyncResult<Void>> result) {
        SQLConnection sqlConnection = SqlConnectionMap.get(connection);
        if (sqlConnection == null) {
            result.handle(Future.failedFuture(new Throwable(NO_SQL_CONNECTION_ERROR)));
            return;
        }

        sqlConnection.commit(commit -> {
            if (commit.succeeded()) {
                sqlConnection.setAutoCommit(true, setAutoCommit -> {
                    if (setAutoCommit.succeeded()) {
                        stopSimpleConnection(connection, stopResult -> {
                            if (stopResult.succeeded()) {
                                result.handle(Future.succeededFuture(setAutoCommit.result()));
                            } else {
                                result.handle(Future.failedFuture(stopResult.cause()));
                            }
                        });
                    } else {
                        stopSimpleConnection(connection, stopResult -> {
                            result.handle(Future.failedFuture(setAutoCommit.cause()));
                        });
                    }
                });
            } else {
                sqlConnection.rollback(rollback -> {
                    if (rollback.succeeded()) {
                        sqlConnection.setAutoCommit(true, setAutoCommit -> {
                            if (setAutoCommit.succeeded()) {
                                stopSimpleConnection(connection, stopResult -> {
                                    if (stopResult.succeeded()) {
                                        result.handle(Future.succeededFuture(setAutoCommit.result()));
                                    } else {
                                        result.handle(Future.failedFuture(stopResult.cause()));
                                    }
                                });
                            } else {
                                stopSimpleConnection(connection, stopResult -> {
                                    result.handle(Future.failedFuture(setAutoCommit.cause()));
                                });
                            }
                        });
                    } else {
                        sqlConnection.setAutoCommit(true, setAutoCommit -> {
                            stopSimpleConnection(connection, stopResult -> {
                                result.handle(Future.failedFuture(commit.cause()));
                            });
                        });
                    }
                });
            }
        });
    }

}
