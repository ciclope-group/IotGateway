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
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public interface HttpServer {
    public static final String PARAMETER_THING = "thing";
    public static final String PARAMETER_INTERACTION = "interaction";
    public static final String PARAMETER_EXTRA_DATA = "argument";

    abstract void startHttpServer(HttpServerOptions httpServerOptions, Router router, Handler<AsyncResult<HttpServer>> handler);

    abstract void setHttpServerThingManagerRoutes(ThingManager thingManager);

    abstract void stopHttpServer(Handler<AsyncResult<Void>> handler);

}
