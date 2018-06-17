package info.ciclope.wotgate.thing.mount;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import info.ciclope.wotgate.thing.mount.model.Status;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

public class MountThing extends AbstractThing {
    private static final String QUEUE_MOUNT = "queueMontura";
    private static final String EXCHANGE_MOUNT = "montura";
    private static final String ROUTING_KEY_COMAND = "comand";
    private static final String ROUTING_KEY_INFO = "info";

    private static final int INACTIVE_TIME = 120000; // 2 minutes

    @Inject
    private RabbitMQClient rabbitMQClient;

    @Inject
    private EventBus eventBus;

    private long timerId;
    private Status status;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        status = new Status();
        connectRabbit(handler);
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        register.addHandler(MountInfo.STATUS, this::getStatus);
        register.addHandler(MountInfo.COMAND, this::sendComand);
        register.addHandler(MountInfo.RABBIT_STATUS, this::updateStatus);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void getStatus(Message<JsonObject> message) {
        message.reply(JsonObject.mapFrom(status));
    }

    private void sendComand(Message<JsonObject> message) {

    }

    @SuppressWarnings("Duplicates")
    private void connectRabbit(Handler<AsyncResult<Void>> handler) {
        Future<Void> startFuture = Future.future();

        rabbitMQClient.start(startFuture);
        Future<Void> completedFuture = startFuture.compose(s -> {
            Future<Void> exchangeFuture = Future.future();
            rabbitMQClient.exchangeDeclare(EXCHANGE_MOUNT, "direct", false, false, exchangeFuture);
            return exchangeFuture;
        }).compose(s -> {
            Future<JsonObject> queueFuture = Future.future();
            rabbitMQClient.queueDeclare(QUEUE_MOUNT, false, true, false, queueFuture);
            return queueFuture;
        }).compose(s -> {
            Future<Void> bindFuture = Future.future();
            rabbitMQClient.queueBind(QUEUE_MOUNT, EXCHANGE_MOUNT, ROUTING_KEY_INFO, bindFuture);
            return bindFuture;
        }).compose(s -> {
            Future<Void> consumeFuture = Future.future();
            rabbitMQClient.basicConsume(QUEUE_MOUNT, MountInfo.NAME + MountInfo.RABBIT_STATUS, consumeFuture);
            return consumeFuture;
        });

        completedFuture.setHandler(handler);
    }

    private void updateStatus(Message<JsonObject> message) {
        status = new Status(new JsonObject(message.body().getString("body")));
        // Message received, stop last timer
        vertx.cancelTimer(timerId);

        // New timer to set the status inactive if no more messages received
        timerId = vertx.setTimer(INACTIVE_TIME, event -> status = new Status());
    }

    @SuppressWarnings("Duplicates")
    private void checkActualReservation(String username, Handler<AsyncResult<Boolean>> handler) {
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
