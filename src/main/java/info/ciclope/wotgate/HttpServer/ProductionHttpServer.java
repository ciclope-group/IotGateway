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

package info.ciclope.wotgate.HttpServer;

import info.ciclope.wotgate.thingmanager.ThingManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class ProductionHttpServer implements HttpServer {
    private static final String WOTGATE_THINGDESCRIPTION = "/";
    private static final String WOTGATE_THINGS = WOTGATE_THINGDESCRIPTION + "things";
    private static final String WOTGATE_THING_THINGDESCRIPTION = WOTGATE_THINGS + "/:" + HttpServer.PARAMETER_THING;
    private static final String WOTGATE_THING_INTERACTION = WOTGATE_THING_THINGDESCRIPTION + "/:" + HttpServer.PARAMETER_INTERACTION;
    private static final String WOTGATE_THING_INTERACTION_EXTRA_DATA = WOTGATE_THING_INTERACTION + "/:" + HttpServer.PARAMETER_EXTRA_DATA;

    private Vertx vertx;
    private io.vertx.core.http.HttpServer httpServer;
    private Router router;

    public ProductionHttpServer(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void startHttpServer(HttpServerOptions httpServerOptions, Router router, Handler<AsyncResult<HttpServer>> handler) {
        this.router = router;
        if (httpServer != null) {
            handler.handle(Future.succeededFuture());
            return;
        }

        httpServer = vertx.createHttpServer(httpServerOptions).requestHandler(router::accept).listen(result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    @Override
    public void setHttpServerThingManagerRoutes(ThingManager thingManager) {
        router.get(WOTGATE_THINGS).handler(thingManager::getThingManagerThings);
        router.get(WOTGATE_THINGS + "/").handler(thingManager::getThingManagerThings);

        router.get(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::getThingDescription);
        router.get(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::getThingDescription);
        router.put(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::putThingDescription);
        router.put(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::putThingDescription);
        router.put(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::putThingDescription);
        router.put(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::putThingDescription);

        router.get(WOTGATE_THING_INTERACTION).handler(thingManager::getThingInteraction);
        router.get(WOTGATE_THING_INTERACTION + "/").handler(thingManager::getThingInteraction);
        router.post(WOTGATE_THING_INTERACTION).handler(thingManager::postThingInteraction);
        router.post(WOTGATE_THING_INTERACTION + "/").handler(thingManager::postThingInteraction);
        router.put(WOTGATE_THING_INTERACTION).handler(thingManager::putThingInteraction);
        router.put(WOTGATE_THING_INTERACTION + "/").handler(thingManager::putThingInteraction);

        router.get(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::getThingInteractionExtraData);
        router.get(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::getThingInteractionExtraData);
        router.put(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::putThingInteractionExtraData);
        router.put(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::putThingInteractionExtraData);
        router.delete(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::deleteThingInteractionExtraData);
        router.delete(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::deleteThingInteractionExtraData);
    }

    @Override
    public void stopHttpServer(Handler<AsyncResult<Void>> handler) {
        if (httpServer == null) {
            handler.handle(Future.succeededFuture());
            return;
        }

        httpServer.close(result -> {
            httpServer = null;
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

}
