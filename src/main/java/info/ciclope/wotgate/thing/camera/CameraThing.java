package info.ciclope.wotgate.thing.camera;

import com.google.inject.Inject;
import info.ciclope.wotgate.http.HttpStatus;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.HandlerRegister;
import info.ciclope.wotgate.thing.camera.model.Photo;
import info.ciclope.wotgate.thing.camera.model.Status;
import info.ciclope.wotgate.util.Util;
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
        register.addHandler(CameraInfo.TAKE_PHOTO, this::takePhoto);
        register.addHandler(CameraInfo.GET_PHOTO, this::getPhoto);
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
        try {
            Status status = message.body().getJsonObject("body").mapTo(Status.class);

            Util.checkActualReservation(message.body().getString("username"), eventBus, result -> {
                if (result.succeeded() && result.result()) {
                    // TODO: Llamada API cambiar configuracion
                    message.reply(null);
                } else {
                    message.fail(HttpStatus.UNAUTHORIZED, "Unauthorized");
                }
            });
        } catch (IllegalArgumentException e) {
            message.fail(HttpStatus.BAD_REQUEST, "Bad Request");
        }
    }

    private void takePhoto(Message<JsonObject> message) {
        Util.checkActualReservation(message.body().getString("username"), eventBus, result -> {
            if (result.succeeded() && result.result()) {
                // TODO: LLamada a API tomar imagen
                /// Mock
                JsonObject response = new JsonObject().put("id", 0);
                ///
                message.reply(response);
            } else {
                message.fail(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        });
    }

    private void getPhoto(Message<JsonObject> message) {
        Util.checkActualReservation(message.body().getString("username"), eventBus, result -> {
            if (result.succeeded() && result.result()) {
                int photoId = message.body().getInteger("photoId");
                // TODO: LLamada a API obtener imagen 'photoId'
                /// Mock
                Photo photo = new Photo();
                photo.setId(photoId);
                photo.setStatus("done");
                photo.setUrl("http://ofs.fi.upm.es/photo/0");
                ///
                message.reply(JsonObject.mapFrom(photo));
            } else {
                message.fail(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        });
    }


}
