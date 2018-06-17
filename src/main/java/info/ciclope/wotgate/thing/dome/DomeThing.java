package info.ciclope.wotgate.thing.dome;

import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.dome.model.Status;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class DomeThing extends AbstractThing {

    private Status status;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        status = new Status();

        handler.handle(Future.succeededFuture());
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        register.addHandler(DomeInfo.STATUS, this::getStatus);
        register.addHandler(DomeInfo.OPEN, this::openShutter);
        register.addHandler(DomeInfo.CLOSE, this::closeShutter);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void getStatus(Message<JsonObject> message) {
        message.reply(JsonObject.mapFrom(status));
    }

    private void openShutter(Message<JsonObject> message) {

    }

    private void closeShutter(Message<JsonObject> message) {

    }
}
