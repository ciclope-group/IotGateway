package info.ciclope.wotgate.util;

import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class Util {

    public static void checkActualReservation(String username, EventBus eventBus, Handler<AsyncResult<Boolean>> handler) {
        Future<Message<JsonObject>> userFuture = Future.future();
        Future<Message<JsonObject>> reservationFuture = Future.future();

        JsonObject params = new JsonObject().put("username", username);
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.GET_USER, params, userFuture);
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.GET_ACTUAL_RESERVATION, null, reservationFuture);

        CompositeFuture.all(userFuture, reservationFuture).setHandler(allCompleted -> {
            if (allCompleted.succeeded()) {
                //noinspection unchecked
                long userId = ((Message<JsonObject>) allCompleted.result().resultAt(0)).body().getLong("id");
                //noinspection unchecked
                long reservationUserId = ((Message<JsonObject>) allCompleted.result().resultAt(1)).body().getLong("userId");

                if (userId == reservationUserId) {
                    handler.handle(Future.succeededFuture(true));
                } else {
                    handler.handle(Future.failedFuture(allCompleted.cause()));
                }
            } else {
                handler.handle(Future.failedFuture(allCompleted.cause()));
            }
        });
    }
}
