package info.ciclope.wotgate.thing.camera;

import com.google.inject.Inject;
import info.ciclope.wotgate.http.HttpStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.camera.model.Status;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import javax.inject.Named;

public class CameraThing extends AbstractThing {

    @Inject
    private EventBus eventBus;

    @Inject
    @Named("camera")
    private WebClient webClient;

    private Status status;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        status = new Status();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        register.addHandler(CameraInfo.STATUS, this::getStatus);
        register.addHandler(CameraInfo.CONFIG, this::config);
        register.addHandler(CameraInfo.TAKE_PICTURE, this::takePicture);
        register.addHandler(CameraInfo.GET_PICTURE, this::getPicture);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    private void getStatus(Message<JsonObject> message) {
        // TODO: Poner url correcta
        webClient.get("/api/camera").send(event -> {
            if (event.succeeded() && event.result().statusCode() == HttpStatus.OK) {
                // Obtain response object
                status = new Status(event.result().bodyAsJsonObject());
            } else {
                status = new Status();
            }

            message.reply(JsonObject.mapFrom(status));
        });
    }

    private void config(Message<JsonObject> message) {
    }

    private void takePicture(Message<JsonObject> message) {
    }

    private void getPicture(Message<JsonObject> message) {
    }


}
