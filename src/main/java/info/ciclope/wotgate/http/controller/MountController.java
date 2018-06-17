package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.mount.MountInfo;
import io.vertx.core.eventbus.EventBus;
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
}
