package info.ciclope.wotgate;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.injector.DependenceFactory;
import info.ciclope.wotgate.injector.MainModule;
import info.ciclope.wotgate.injector.ProductionDependenceFactory;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperInfo;
import info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperThing;
import info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationInfo;
import info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationThing;
import info.ciclope.wotgate.thingmanager.ThingManager;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
import io.vertx.core.*;

import java.util.Arrays;


public class MainVerticle extends AbstractVerticle {
    private DependenceFactory dependenceFactory;

    @Inject
    private ThingManagerConfiguration thingManagerConfiguration;

    @Inject
    private HttpServer httpServer;
    private Injector injector;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        injector = Guice.createInjector(new MainModule(this));
        injector.injectMembers(this);
    }


    @Override
    public void start(Future<Void> future) {
        dependenceFactory = new ProductionDependenceFactory(vertx);
        httpServer.startHttpServer(event -> {
            if (event.succeeded()) {
                Future<Void> gatekeeperFuture = Future.future();
                Future<Void> weatherStationFuture = Future.future();

                insertGatekeeperThing(dependenceFactory.getThingManager(), gatekeeperFuture);
                insertWeatherStationThing(dependenceFactory.getThingManager(), weatherStationFuture);

                CompositeFuture.all(Arrays.asList(weatherStationFuture, gatekeeperFuture)).setHandler(allCompleted -> {
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
//				httpServer.routesManager(dependenceFactory.getThingManager());
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

    private void insertGatekeeperThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration(GateKeeperInfo.NAME,
                "info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperThing");

        Verticle verticle = injector.getInstance(GateKeeperThing.class);
        insertThing(thingManager, verticle, thingConfiguration, handler);
    }

    private void insertWeatherStationThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration(WeatherStationInfo.NAME,
                "info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationThing");

        Verticle verticle = injector.getInstance(WeatherStationThing.class);
        insertThing(thingManager, verticle, thingConfiguration, handler);
    }

//    private void insertDomeThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("dome",
//                "info.ciclope.wotgate.thing.driver.dome.DomeThing",
//                injector);
//        insertThing(thingManager, thingConfiguration, handler);
//    }
//
//    private void insertMountThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("mount",
//                "info.ciclope.wotgate.thing.driver.mount.MountThing",
//                injector);
//        insertThing(thingManager, thingConfiguration, handler);
//    }
//
//    private void insertCameraThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("camera",
//                "info.ciclope.wotgate.thing.driver.camera.CameraThing",
//                injector);
//        insertThing(thingManager, thingConfiguration, handler);
//    }

    private void insertThing(ThingManager thingManager, Verticle verticle, ThingConfiguration thingConfiguration, Handler<AsyncResult<Void>> handler) {
        thingManager.insertThing(thingConfiguration, verticle, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
