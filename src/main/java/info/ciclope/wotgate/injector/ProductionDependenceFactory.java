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

package info.ciclope.wotgate.injector;

import info.ciclope.wotgate.storage.SqliteStorage;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thingmanager.ProductionThingManager;
import info.ciclope.wotgate.thingmanager.ThingManager;
import info.ciclope.wotgate.thingmanager.ThingManagerSqliteStorage;
import info.ciclope.wotgate.thingmanager.ThingManagerStorage;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ProductionDependenceFactory implements DependenceFactory {
    private final Router router;
    private Vertx vertx;
    private final ThingManager thingManager;

    public ProductionDependenceFactory(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        this.thingManager = new ProductionThingManager(this);
    }

    @Override
    public Vertx getVertxInstance() {
        return vertx;
    }

    @Override
    public Router getRouterInstance() {
        return router;
    }

    @Override
    public ThingManager getThingManager() {
     return thingManager;
    }

    @Override
    public ThingManagerStorage createThingManagerStorage() {
        return new ThingManagerSqliteStorage(createDatabaseStorage());
    }

    @Override
    public DatabaseStorage createDatabaseStorage() {
        return new SqliteStorage(vertx);
    }
}
