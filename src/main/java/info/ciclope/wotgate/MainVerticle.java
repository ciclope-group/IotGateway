package info.ciclope.wotgate;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.di.MainModule;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperThing;
import info.ciclope.wotgate.thing.weatherstation.WeatherStationInfo;
import info.ciclope.wotgate.thing.weatherstation.WeatherStationThing;
import io.vertx.core.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

    private Map<String, String> verticlesDeployed;
    private Injector injector;

    @Inject
    private HttpServer httpServer;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        verticlesDeployed = new HashMap<>();
        injector = Guice.createInjector(new MainModule(this));
        injector.injectMembers(this);
    }

    @Override
    public void start(Future<Void> future) {
        httpServer.startHttpServer(event -> {
            if (event.succeeded()) {
                Future<Void> gatekeeperFuture = Future.future();
                Future<Void> weatherStationFuture = Future.future();

                insertGatekeeperThing(gatekeeperFuture);
                insertWeatherStationThing(weatherStationFuture);

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
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        if (httpServer != null) {
            httpServer.stopHttpServer(stopResult -> {
            });
        }

        verticlesDeployed.forEach((k, v) -> vertx.undeploy(v));
        super.stop(stopFuture);
    }

    private void insertGatekeeperThing(Handler<AsyncResult<Void>> handler) {
        Verticle verticle = injector.getInstance(GateKeeperThing.class);
        insertThing(verticle, GateKeeperInfo.NAME, handler);
    }

    private void insertWeatherStationThing(Handler<AsyncResult<Void>> handler) {
        Verticle verticle = injector.getInstance(WeatherStationThing.class);
        insertThing(verticle, WeatherStationInfo.NAME, handler);
    }

//    private void insertDomeThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("dome",
//                "info.ciclope.wotgate.thing.driver.dome.DomeThing",
//                di);
//        insertThing(thingManager, thingConfiguration, handler);
//    }
//
//    private void insertMountThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("mount",
//                "info.ciclope.wotgate.thing.driver.mount.MountThing",
//                di);
//        insertThing(thingManager, thingConfiguration, handler);
//    }
//
//    private void insertCameraThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
//        ThingConfiguration thingConfiguration = new ThingConfiguration("camera",
//                "info.ciclope.wotgate.thing.driver.camera.CameraThing",
//                di);
//        insertThing(thingManager, thingConfiguration, handler);
//    }

    private void insertThing(Verticle verticle, String verticleName, Handler<AsyncResult<Void>> handler) {
        ThingConfiguration thingConfiguration = new ThingConfiguration(verticleName);
        DeploymentOptions options = new DeploymentOptions().setConfig(thingConfiguration.asJsonObject());
        vertx.deployVerticle(verticle, options, deployment -> {
            if (deployment.succeeded()) {
                verticlesDeployed.put(verticleName, deployment.result());
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(deployment.cause()));
            }
        });
    }
}
