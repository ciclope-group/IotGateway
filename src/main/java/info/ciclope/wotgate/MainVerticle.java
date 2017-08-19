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

import info.ciclope.wotgate.HttpServer.HttpServer;
import info.ciclope.wotgate.HttpServer.ProductionHttpServer;
import info.ciclope.wotgate.dependencefactory.DependenceFactory;
import info.ciclope.wotgate.dependencefactory.ProductionDependenceFactory;
import info.ciclope.wotgate.thingmanager.ThingManager;
import info.ciclope.wotgate.things.thing.ThingConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;


public class MainVerticle extends AbstractVerticle {
    private DependenceFactory dependenceFactory;

    @Override
    public void start(Future<Void> future) {
        Integer port = config().getInteger("http.port", 8080);
        HttpServerOptions options = new HttpServerOptions().setPort(port);
        dependenceFactory = new ProductionDependenceFactory(vertx);
        HttpServer httpServer = new ProductionHttpServer(vertx);
        httpServer.startHttpServer(options, dependenceFactory.getRouterInstance(), result -> {
            if (result.succeeded()) {
                httpServer.setHttpServerThingManagerRoutes(dependenceFactory.getThingManager());
                insertWeatherStationThing(dependenceFactory.getThingManager(), insertThing -> {
                    if (insertThing.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(insertThing.cause());
                    }
                });
            } else {
                future.fail(result.cause());
            }
        });
    }

    private void insertWeatherStationThing(ThingManager thingManager, Handler<AsyncResult<Void>> handler) {
        JsonObject configuration = new JsonObject();
        configuration.put("name", "weatherstation");
        configuration.put("classname", "info.ciclope.wotgate.things.weatherstation.WeatherStationThing");
        ThingConfiguration thingConfiguration = new ThingConfiguration(configuration);
        thingManager.insertThing(thingConfiguration, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
