package info.ciclope.wotgate.thing.weatherstation;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.weatherstation.model.Status;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class WeatherStationThing extends AbstractThing {
        private static final int UPDATE_INTERVAL = 300000; // 5 minutes

    @Inject
    private WebClient webClient;

    private long timerId;
    private Status status;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        status = new Status();

        startUpdatingProcess();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void addHandlers(HandlerRegister handlerRegister) {
        handlerRegister.addHandler(WeatherStationInfo.STATUS, this::getStateProperty);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        stopUpdatingProcess();
        handler.handle(Future.succeededFuture());
    }

    private void startUpdatingProcess() {
        updateMeasurements();
        timerId = vertx.setPeriodic(UPDATE_INTERVAL, event -> updateMeasurements());
    }

    private void stopUpdatingProcess() {
        vertx.cancelTimer(timerId);
    }

    private void getStateProperty(Message<JsonObject> message) {
        message.reply(JsonObject.mapFrom(status));
    }

    private void updateMeasurements() {
        // Make request to physical device
        webClient.get("/api/estacion/montegancedo").send(event -> {
            if (event.succeeded()) {
                // Obtain response object
                status = new Status(event.result().bodyAsJsonObject());
            } else {
                status = new Status();
            }
        });
    }
}
