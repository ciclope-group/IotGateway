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

import info.ciclope.wotgate.thing.component.ThingConfiguration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.ext.web.RoutingContext;

public interface ThingManager {

    void insertThing(ThingConfiguration thingConfiguration, Verticle verticle, Handler<AsyncResult<Void>> result);

    void deleteThing(String name, Handler<AsyncResult<Void>> result);

    void getThingManagerThings(RoutingContext routingContext);

    void getThingDescription(RoutingContext routingContext);

    void putThingDescription(RoutingContext routingContext);

    void getThingInteraction(RoutingContext routingContext);

    void postThingInteraction(RoutingContext routingContext);

    void putThingInteraction(RoutingContext routingContext);

    void getThingActionTask(RoutingContext routingContext);

    void putThingActionTask(RoutingContext routingContext);

    void deleteThingActionTask(RoutingContext routingContext);

    void stopThingManager();
}
