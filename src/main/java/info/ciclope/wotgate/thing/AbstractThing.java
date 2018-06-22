package info.ciclope.wotgate.thing;

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

        startThing(startResult -> {
            if (startResult.succeeded()) {
                handlerRegister = new HandlerRegister(vertx.eventBus(), thingConfiguration.getThingName());
                addHandlers(handlerRegister);
                handlerRegister.register();

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
                stopFuture.fail(stopResult.cause());
            }
        });
        super.stop(stopFuture);
    }

    public abstract void addHandlers(HandlerRegister register);

    public abstract void startThing(Handler<AsyncResult<Void>> handler);

    public abstract void stopThing(Handler<AsyncResult<Void>> handler);

}
