package info.ciclope.wotgate.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperInfo;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.AuthorityName;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class SecurityService {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public SecurityService(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void login(RoutingContext routingContext) {
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.LOGIN, routingContext.getBodyAsJson(),
                (AsyncResult<Message<JsonObject>> response) -> {
                    if (response.succeeded()) {
                        HttpServerResponse httpServerResponse = routingContext.response();
                        httpServerResponse.putHeader("content-type", "application/json; charset=utf-8");
                        httpServerResponse.end(response.result().body().toString());
                    } else {
                        routingContext.fail(((ReplyException) response.cause()).failureCode());
                    }
                });
    }

    public void register(RoutingContext routingContext) {
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.REGISTER, routingContext.getBodyAsJson(),
                (AsyncResult<Message<Integer>> response) -> {
                    if (response.succeeded()) {
                        String contentLocation = httpService.getBaseUrl(routingContext.request()) +
                                "/users/" + String.valueOf(response.result().body());

                        HttpServerResponse httpServerResponse = routingContext.response();
                        httpServerResponse.putHeader("content-type", "application/json; charset=utf-8");
                        httpServerResponse.putHeader("Content-Location", contentLocation);
                        httpServerResponse.setStatusCode(HttpResponseStatus.CREATED);
                        httpServerResponse.end();
                    } else {
                        routingContext.fail(((ReplyException) response.cause()).failureCode());
                    }
                });
    }

    public void activateUser(RoutingContext routingContext) {
        User user = routingContext.user();
        user.isAuthorized(AuthorityName.ROLE_ADMIN, result -> {
            if (result.succeeded() && result.result()) {
                JsonObject params = new JsonObject().put("id", Integer.parseInt(routingContext.pathParam("id")));

                eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.ACTIVATE_USER, params,
                        response -> {
                            if (response.succeeded()) {
                                HttpServerResponse httpServerResponse = routingContext.response();
                                httpServerResponse.putHeader("content-type", "application/json; charset=utf-8");
                                httpServerResponse.end();
                            } else {
                                routingContext.fail(((ReplyException) response.cause()).failureCode());
                            }
                        });
            } else {
                routingContext.fail(HttpResponseStatus.FORBIDDEN);
            }
        });
    }
}
