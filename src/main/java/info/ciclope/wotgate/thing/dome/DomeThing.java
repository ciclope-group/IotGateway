package info.ciclope.wotgate.thing.dome;

import com.google.inject.Inject;
import info.ciclope.wotgate.http.HttpStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.dome.model.Status;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

public class DomeThing extends AbstractThing {
    private static final String QUEUE_DOME = "queueCupula";
    private static final String EXCHANGE_DOME = "cupula";
    private static final String ROUTING_KEY_ACTION = "action";
    private static final String ROUTING_KEY_INFO = "info";

    private static final int INACTIVE_TIME = 120000; // 2 mintes

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
        register.addHandler(DomeInfo.STATUS, this::getStatus);
        register.addHandler(DomeInfo.OPEN, this::openShutter);
        register.addHandler(DomeInfo.CLOSE, this::closeShutter);
        register.addHandler(DomeInfo.RABBIT_STATUS, this::updateStatus);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void getStatus(Message<JsonObject> message) {
        message.reply(JsonObject.mapFrom(status));
    }

    private void openShutter(Message<JsonObject> message) {
        checkActualReservation(message.body().getString("username"), result -> {
            if (result.succeeded() && result.result()) {
                JsonObject data = new JsonObject().put("action", "open");
                rabbitMQClient.basicPublish(EXCHANGE_DOME, ROUTING_KEY_ACTION, new JsonObject().put("body", data.toString()),
                        publishHandler -> {
                            if (publishHandler.succeeded()) {
                                message.reply(null);
                            } else {
                                message.fail(HttpStatus.INTERNAL_ERROR, "Internal Error");
                            }
                        });
            } else {
                message.fail(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        });
    }

    private void closeShutter(Message<JsonObject> message) {
        checkActualReservation(message.body().getString("username"), result -> {
            if (result.succeeded() && result.result()) {
                JsonObject data = new JsonObject().put("action", "close");
                rabbitMQClient.basicPublish(EXCHANGE_DOME, ROUTING_KEY_ACTION, new JsonObject().put("body", data.toString()),
                        publishHandler -> {
                            if (publishHandler.succeeded()) {
                                message.reply(null);
                            } else {
                                message.fail(HttpStatus.INTERNAL_ERROR, "Internal Error");
                            }
                        });
            } else {
                message.fail(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        });
    }

    private void connectRabbit(Handler<AsyncResult<Void>> handler) {
        Future<Void> startFuture = Future.future();

        rabbitMQClient.start(startFuture);
        Future<Void> completedFuture = startFuture.compose(s -> {
            Future<Void> exchangeFuture = Future.future();
            rabbitMQClient.exchangeDeclare(EXCHANGE_DOME, "direct", false, false, exchangeFuture);
            return exchangeFuture;
        }).compose(s -> {
            Future<JsonObject> queueFuture = Future.future();
            rabbitMQClient.queueDeclare(QUEUE_DOME, false, true, false, queueFuture);
            return queueFuture;
        }).compose(s -> {
            Future<Void> bindFuture = Future.future();
            rabbitMQClient.queueBind(QUEUE_DOME, EXCHANGE_DOME, ROUTING_KEY_INFO, bindFuture);
            return bindFuture;
        }).compose(s -> {
            Future<Void> consumeFuture = Future.future();
            rabbitMQClient.basicConsume(QUEUE_DOME, DomeInfo.NAME + DomeInfo.RABBIT_STATUS, consumeFuture);
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
