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

package info.ciclope.wotgate.storage;

import com.sun.istack.internal.NotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

public interface DatabaseStorage {

    void startDatabaseStorage(@NotNull String databaseName);

    void stopDatabaseStorage();

    /* These methods start the connection, execute the operation, and finish the connection */
    void query(@NotNull String query, Handler<AsyncResult<ResultSet>> result);

    void queryWithParameters(@NotNull String query, @NotNull JsonArray parameters, Handler<AsyncResult<ResultSet>> result);

    void update(@NotNull String update, Handler<AsyncResult<UpdateResult>> result);

    void updateWithParameters(@NotNull String update, @NotNull JsonArray parameters, Handler<AsyncResult<UpdateResult>> result);

    void executeBatch(@NotNull List<String> batch, Handler<AsyncResult<Void>> result);

    /* These methods need first the connection to be open, then execute the operation(s), and then finish the connection */
    void startSimpleConnection(Handler<AsyncResult<Integer>> result);

    void query(@NotNull Integer connection, @NotNull String query, Handler<AsyncResult<ResultSet>> result);

    void queryWithParameters(@NotNull Integer connection, @NotNull String query, @NotNull JsonArray parameters, Handler<AsyncResult<ResultSet>> result);

    void update(@NotNull Integer connection, @NotNull String update, Handler<AsyncResult<UpdateResult>> result);

    void updateWithParameters(@NotNull Integer connection, @NotNull String update, @NotNull JsonArray parameters, Handler<AsyncResult<UpdateResult>> result);

    void executeBatch(@NotNull Integer connection, @NotNull List<String> batch, Handler<AsyncResult<Void>> result);

    void stopSimpleConnection(@NotNull Integer connection, Handler<AsyncResult<Void>> result);

    void startTransactionConnection(Handler<AsyncResult<Integer>> result);

    void commitTransaction(@NotNull Integer connection, Handler<AsyncResult<Void>> result);

    void rollbackTransaction(@NotNull Integer connection, Handler<AsyncResult<Void>> result);

    void stopTransactionConnection(@NotNull Integer connection, Handler<AsyncResult<Void>> result);
}
