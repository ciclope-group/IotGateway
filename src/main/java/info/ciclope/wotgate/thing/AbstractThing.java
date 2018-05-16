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

package info.ciclope.wotgate.thing;

import info.ciclope.wotgate.ErrorCode;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public abstract class AbstractThing extends AbstractVerticle {
    private ThingConfiguration thingConfiguration;
    private HandlerRegister handlerRegister;

    @Override
    public void start(Future<Void> startFuture) {
        this.thingConfiguration = new ThingConfiguration(this.config());
        if (!loadThingExtraConfiguration()) {
            startFuture.fail(ErrorCode.ERROR_LOAD_THING_EXTRA_CONFIGURATION);
            return;
        }
        startThing(startResult -> {
            if (startResult.succeeded()) {
                handlerRegister = new HandlerRegister(vertx.eventBus(), thingConfiguration.getThingName());
                addHandlers(handlerRegister);
                handlerRegister.register();
//                thingHandlers = new ProductionThingHandlers(this, handlerRegister, databaseStorage);
//                thingHandlersStarter = new ProductionThingHandlersStarter(thingConfiguration.getThingName(), thingHandlers);
//                thingHandlersStarter.startThingHandlers(thingDescription, vertx.eventBus());
                startFuture.complete();
            } else {
                startFuture.fail(startResult.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        stopThing(stopResult -> {
            if (stopResult.succeeded()) {
                stopFuture.succeeded();
            } else {
                {
                    stopFuture.fail(stopResult.cause());
                }
            }
        });
        super.stop(stopFuture);
    }

    public abstract String getThingDescriptionPath();

    public abstract boolean loadThingExtraConfiguration();

    public abstract void addHandlers(HandlerRegister register);

    public abstract void startThing(Handler<AsyncResult<Void>> handler);

    public abstract void stopThing(Handler<AsyncResult<Void>> handler);

}
