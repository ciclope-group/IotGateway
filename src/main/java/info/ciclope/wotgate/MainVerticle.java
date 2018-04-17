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

import com.google.inject.Guice;
import com.google.inject.Inject;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.injector.DependenceFactory;
import info.ciclope.wotgate.injector.MainModule;
import info.ciclope.wotgate.injector.ProductionDependenceFactory;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationInfo;
import info.ciclope.wotgate.thingmanager.ThingManager;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;


public class MainVerticle extends AbstractVerticle {
    private DependenceFactory dependenceFactory;

    @Inject
    private ThingManagerConfiguration thingManagerConfiguration;

    @Inject
    private HttpServer httpServer;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        Guice.createInjector(new MainModule(this)).injectMembers(this);
    }


    @Override
    public void start(Future<Void> future) {
        dependenceFactory = new ProductionDependenceFactory(vertx);
        httpServer.startHttpServer(event -> {
            if (event.succeeded()) {
                httpServer.setHttpServerThingManagerRoutes();
                Future<Void> weatherStationFuture = Future.future();
                insertWeatherStationThing(dependenceFactory.getThingManager(), weatherStationFuture);

                CompositeFuture.all(Arrays.asList(weatherStationFuture)).setHandler(allCompleted -> {
                    if (allCompleted.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(allCompleted.cause());
                    }
                });
            } else {
                future.fail(event.cause());
            }
        });


//		httpServer = new ProductionHttpServer(vertx);
//		httpServer.startHttpServer(options, dependenceFactory.getRouterInstance(), result -> {
//			if (result.succeeded()) {
//				httpServer.setHttpServerThingManagerRoutes(dependenceFactory.getThingManager());
//				insertGatekeeperThing(dependenceFactory.getThingManager(), insertGateKeeper -> {
//					if (insertGateKeeper.succeeded()) {
//						insertMountThing(dependenceFactory.getThingManager(), insertMount -> {
//							if (insertMount.succeeded()) {
//								insertDomeThing(dependenceFactory.getThingManager(), insertDome -> {
//									if (insertDome.succeeded()) {
//										insertCameraThing(dependenceFactory.getThingManager(), insertCamera -> {
//											if (insertCamera.succeeded()) {
//												insertWeatherStationThing(dependenceFactory.getThingManager(), insertWeatherStation -> {
//													if (insertWeatherStation.succeeded()) {
//														future.complete();
//													} else {
//														future.fail(insertWeatherStation.cause());
//													}
//												});
//											} else {
//												future.fail(insertCamera.cause());
//											}
//										});
//									} else {
//										future.fail(insertDome.cause());
//									}
//								});
//							} else {
//								future.fail(insertMount.cause());
//							}
//						});
//					} else {
//						future.fail(insertGateKeeper.cause());
//					}
//				});
//			} else {
//				future.fail(result.cause());
//			}
//		});
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
        ThingConfiguration thingConfiguration = new ThingConfiguration(WeatherStationInfo.NAME,
                "info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationThing",
                new JsonObject());

        insertThing(thingManager, thingConfiguration, handler);
    }

    private void insertGatekeeperThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("gatekeeper",
                "info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperThing",
                new JsonObject());
        insertThing(thingManager, thingConfiguration, handler);
    }

    private void insertDomeThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("dome",
                "info.ciclope.wotgate.thing.driver.dome.DomeThing",
                new JsonObject());
        insertThing(thingManager, thingConfiguration, handler);
    }

    private void insertMountThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("mount",
                "info.ciclope.wotgate.thing.driver.mount.MountThing",
                new JsonObject());
        insertThing(thingManager, thingConfiguration, handler);
    }

    private void insertCameraThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration("camera",
                "info.ciclope.wotgate.thing.driver.camera.CameraThing",
                new JsonObject());
        insertThing(thingManager, thingConfiguration, handler);
    }

    private void insertThing(ThingManager thingManager, ThingConfiguration thingConfiguration, Handler<AsyncResult<Void>> handler) {
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
