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

package info.ciclope.wotgate.thingmanager;

import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class ThingManagerSqliteStorage implements ThingManagerStorage {
    private static final String THINGMANAGER_DATABASE = "WoTGate";
    private static final String THINGMANAGER_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS thing (id INTEGER PRIMARY KEY ASC, name TEXT, classname TEXT, extraconfiguration TEXT, UNIQUE (name));";
    private static final String THINGMANAGER_DATABASE_INSERT_THING = "INSERT INTO thing (name, classname, extraconfiguration) VALUES (?, ?, json(?));";
    private static final String THINGMANAGER_DATABASE_DELETE_THING = "DELETE FROM thing WHERE name = ? ;";

    private final DatabaseStorage databaseStorage;

    public ThingManagerSqliteStorage(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
        initDatabase();
    }

    @Override
    public void insertThing(ThingConfiguration thingConfiguration, Handler<AsyncResult<Void>> result) {
        JsonArray parameters = new JsonArray();
        parameters.add(thingConfiguration.getThingName());
        parameters.add(thingConfiguration.getThingClassname());
        parameters.add(thingConfiguration.getExtraConfiguration());
        databaseStorage.updateWithParameters(THINGMANAGER_DATABASE_INSERT_THING, parameters, update-> {
            if (update.succeeded()) {
                result.handle(Future.succeededFuture());
            } else {
                result.handle(Future.failedFuture(update.cause()));
            }
        });

    }

    @Override
    public void deleteThing(String name, Handler<AsyncResult<Void>> result) {
        JsonArray parameters = new JsonArray();
        parameters.add(name);
        databaseStorage.updateWithParameters(THINGMANAGER_DATABASE_DELETE_THING, parameters, update-> {
            if (update.succeeded()) {
                result.handle(Future.succeededFuture());
            } else {
                result.handle(Future.failedFuture(update.cause()));
            }
        });
    }

    private void initDatabase() {
        this.databaseStorage.startDatabaseStorage(THINGMANAGER_DATABASE);
        databaseStorage.startSimpleConnection(connection -> {
            if (connection.succeeded()) {
                List<String> batch = new ArrayList<>();
                batch.add(THINGMANAGER_DATABASE_CREATE);
                databaseStorage.executeBatch(connection.result(), batch, next-> {
                    databaseStorage.stopSimpleConnection(connection.result(), stopResult-> {
                        // TODO: Add async management
                    });
                });
            }
        });

    }

}
