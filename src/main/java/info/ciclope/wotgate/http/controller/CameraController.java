package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.camera.CameraInfo;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class CameraController {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public CameraController(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void getStatus(RoutingContext routingContext) {
        eventBus.send(CameraInfo.NAME + CameraInfo.STATUS, null,
                response -> httpService.simpleHttpResponseWithBody(routingContext, response));
    }

    public void setConfig(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject();
        params.put("username", username);
        params.put("body", routingContext.getBodyAsJson());

        eventBus.send(CameraInfo.NAME + CameraInfo.CONFIG, params,
                response -> httpService.simpleHttpResponse(routingContext, response));
    }

    public void takePhoto(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject().put("username", username);

        eventBus.send(CameraInfo.NAME + CameraInfo.TAKE_PHOTO, params,
                response -> httpService.simpleHttpResponseWithBody(routingContext, response));
    }

    public void getPhoto(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        int photoId = Integer.parseInt(routingContext.pathParam("id"));
        JsonObject params = new JsonObject();
        params.put("username", username);
        params.put("photoId", photoId);

        eventBus.send(CameraInfo.NAME + CameraInfo.GET_PHOTO, params,
                response -> httpService.simpleHttpResponseWithBody(routingContext, response));
    }
}
