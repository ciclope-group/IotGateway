package info.ciclope.wotgate.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class ReservationController {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public ReservationController(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void getAllReservations(RoutingContext routingContext) {
        MultiMap queryParams = routingContext.queryParams();

        String start = queryParams.get("start");
        String end = queryParams.get("end");

        if (start != null && end != null) {
            JsonObject params = new JsonObject().put("start", start).put("end", end);

            eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.GET_RESERVATIONS_RANGE, params,
                    response -> httpService.simpleHttpResponse(routingContext, response));
        } else {
            routingContext.fail(HttpResponseStatus.BAD_REQUEST);
        }
    }

    public void createReservation(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject();
        params.put("body", routingContext.getBodyAsJson());
        params.put("username", username);

        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.CREATE_RESERVATION, params,
                response -> {
                    if (response.succeeded()) {
                        String contentLocation = httpService.getBaseUrl(routingContext.request()) +
                                "/reservations/" + String.valueOf(response.result().body());

                        HttpServerResponse httpServerResponse = routingContext.response();
                        httpServerResponse.putHeader(HttpHeader.CONTENT_TYPE, HttpHeader.CONTENT_TYPE_JSON);
                        httpServerResponse.putHeader(HttpHeader.CONTENT_LOCATION, contentLocation);
                        httpServerResponse.setStatusCode(HttpResponseStatus.CREATED);
                        httpServerResponse.end();
                    } else {
                        routingContext.fail(((ReplyException) response.cause()).failureCode());
                    }
                });
    }

    public void cancelReservation(RoutingContext routingContext) {

    }

    public void completeReservation(RoutingContext routingContext) {

    }
}
