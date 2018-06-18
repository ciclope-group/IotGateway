package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.mount.MountInfo;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class MountController {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public MountController(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void getStatus(RoutingContext routingContext) {
        eventBus.send(MountInfo.NAME + MountInfo.STATUS, null,
                response -> httpService.simpleHttpResponseWithBody(routingContext, response));
    }

    public void move(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject();
        params.put("body", routingContext.getBodyAsJson());
        params.put("username", username);

        eventBus.send(MountInfo.NAME + MountInfo.MOVE, params,
                response -> httpService.simpleHttpResponse(routingContext, response));
    }

    public void step(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject();
        params.put("body", routingContext.getBodyAsJson());
        params.put("username", username);

        eventBus.send(MountInfo.NAME + MountInfo.STEP, params,
                response -> httpService.simpleHttpResponse(routingContext, response));
    }
}
