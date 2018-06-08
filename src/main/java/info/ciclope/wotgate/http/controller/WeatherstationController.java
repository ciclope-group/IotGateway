package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.thing.driver.weatherstation.WeatherStationInfo;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class WeatherstationController {

    private EventBus eventBus;

    @Inject
    public WeatherstationController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void getState(RoutingContext routingContext) {
        eventBus.send(WeatherStationInfo.NAME + WeatherStationInfo.STATUS, null, response -> {
            if (response.succeeded()) {
                HttpServerResponse httpServerResponse = routingContext.response();
                httpServerResponse.putHeader("content-type", "application/json; charset=utf-8");
                httpServerResponse.end(response.result().body().toString());
            } else {
                response.cause().printStackTrace();
                routingContext.fail(400);
            }
        });
    }

}
