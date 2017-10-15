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

package info.ciclope.wotgate;

import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.http.ProductionHttpServer;
import info.ciclope.wotgate.injector.DependenceFactory;
import info.ciclope.wotgate.injector.ProductionDependenceFactory;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thingmanager.ThingManager;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;


public class MainVerticle extends AbstractVerticle {
    private DependenceFactory dependenceFactory;
    private HttpServer httpServer;
    private ThingManagerConfiguration thingManagerConfiguration;

    @Override
    public void start(Future<Void> future) {
        thingManagerConfiguration = new ThingManagerConfiguration(config());
        HttpServerOptions options = new HttpServerOptions().setPort(thingManagerConfiguration.getHttpServerPort());
        dependenceFactory = new ProductionDependenceFactory(vertx);
        httpServer = new ProductionHttpServer(vertx);
        httpServer.startHttpServer(options, dependenceFactory.getRouterInstance(), result -> {
            if (result.succeeded()) {
                httpServer.setHttpServerThingManagerRoutes(dependenceFactory.getThingManager());
                insertGatekeeperThing(dependenceFactory.getThingManager(), insertGateKeeper -> {
                    if (insertGateKeeper.succeeded()) {
                        insertMountThing(dependenceFactory.getThingManager(), insertMount -> {
                            if (insertMount.succeeded()) {
                                insertDomeThing(dependenceFactory.getThingManager(), insertDome -> {
                                    if (insertDome.succeeded()) {
                                        insertCameraThing(dependenceFactory.getThingManager(), insertCamera -> {
                                            if (insertCamera.succeeded()) {
                                                insertWeatherStationThing(dependenceFactory.getThingManager(), insertWeatherStation -> {
                                                    if (insertWeatherStation.succeeded()) {
                                                        future.complete();
                                                    } else {
                                                        future.fail(insertWeatherStation.cause());
                                                    }
                                                });
                                            } else {
                                                future.fail(insertCamera.cause());
                                            }
                                        });
                                    } else {
                                        future.fail(insertDome.cause());
                                    }
                                });
                            } else {
                                future.fail(insertMount.cause());
                            }
                        });
                    } else {
                        future.fail(insertGateKeeper.cause());
                    }
                });
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        if (httpServer != null) {
            httpServer.stopHttpServer(stopResult -> {
            });
        }
        dependenceFactory.getThingManager().stopThingManager();
        super.stop(stopFuture);
    }

    private void insertWeatherStationThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("weatherstation",
                "info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationThing",
                new JsonObject());
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void insertGatekeeperThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("gatekeeper",
                "info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperThing",
                new JsonObject());
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void insertDomeThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("dome",
                "info.ciclope.wotgate.thing.driver.dome.DomeThing",
                new JsonObject());
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void insertMountThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("mount",
                "info.ciclope.wotgate.thing.driver.mount.MountThing",
                new JsonObject());
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void insertCameraThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("camera",
                "info.ciclope.wotgate.thing.driver.camera.CameraThing",
                new JsonObject());
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
