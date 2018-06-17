package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.dome.DomeInfo;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class DomeController {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public DomeController(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void getStatus(RoutingContext routingContext) {
        eventBus.send(DomeInfo.NAME + DomeInfo.STATUS, null,
                response -> httpService.simpleHttpResponseWithBody(routingContext, response));
    }
}
