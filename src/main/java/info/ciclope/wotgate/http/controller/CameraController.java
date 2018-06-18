package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.camera.CameraInfo;
import io.vertx.core.eventbus.EventBus;
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
}
