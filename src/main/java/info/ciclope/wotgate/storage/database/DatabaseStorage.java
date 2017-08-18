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

package info.ciclope.wotgate.storage.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

public interface DatabaseStorage {

    void startStorageManager(String storageName);

    void stopStorageManager();

    /* These methods start the connection, execute the operation, and finish the connection */
    void query(String query, Handler<AsyncResult<ResultSet>> result);

    void queryWithParameters(String query, JsonArray parameters, Handler<AsyncResult<ResultSet>> result);

    void update(String update, Handler<AsyncResult<UpdateResult>> result);

    void updateWithParameters(String update, JsonArray parameters, Handler<AsyncResult<UpdateResult>> result);

    void executeBatch(List<String> batch, Handler<AsyncResult<Void>> result);

    /* These methods need first the connection to be open, then execute the operation(s), and then finish the connection */
    void startSimpleConnection(Handler<AsyncResult<Integer>> result);

    void query(Integer connection, String query, Handler<AsyncResult<ResultSet>> result);

    void queryWithParameters(Integer connection, String query, JsonArray parameters, Handler<AsyncResult<ResultSet>> result);

    void update(Integer connection, String update, Handler<AsyncResult<UpdateResult>> result);

    void updateWithParameters(Integer connection, String update, JsonArray parameters, Handler<AsyncResult<UpdateResult>> result);

    void executeBatch(Integer connection, List<String> batch, Handler<AsyncResult<Void>> result);

    void stopSimpleConnection(Integer connection, Handler<AsyncResult<Void>> result);

    void startTransactionConnection(Handler<AsyncResult<Integer>> result);

    void commitTransaction(Integer connection, Handler<AsyncResult<Void>> result);

    void rollbackTransaction(Integer connection, Handler<AsyncResult<Void>> result);

    void stopTransactionConnection(Integer connection, Handler<AsyncResult<Void>> result);
}
