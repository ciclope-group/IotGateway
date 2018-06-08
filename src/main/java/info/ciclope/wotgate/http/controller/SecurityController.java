package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.http.HttpService;
import info.ciclope.wotgate.thing.gatekeeper.GateKeeperInfo;
import info.ciclope.wotgate.thing.gatekeeper.model.AuthorityName;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class SecurityController {

    private EventBus eventBus;
    private HttpService httpService;

    @Inject
    public SecurityController(EventBus eventBus, HttpService httpService) {
        this.eventBus = eventBus;
        this.httpService = httpService;
    }

    public void login(RoutingContext routingContext) {
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.LOGIN, routingContext.getBodyAsJson(),
                response -> simpleHttpResponse(routingContext, response));
    }

    public void register(RoutingContext routingContext) {
        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.REGISTER, routingContext.getBodyAsJson(),
                (AsyncResult<Message<Integer>> response) -> {
                    if (response.succeeded()) {
                        String contentLocation = httpService.getBaseUrl(routingContext.request()) +
                                "/users/" + String.valueOf(response.result().body());

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

    public void activateUser(RoutingContext routingContext) {
        User user = routingContext.user();
        user.isAuthorized(AuthorityName.ROLE_ADMIN, result -> {
            if (result.succeeded() && result.result()) {
                JsonObject params = new JsonObject().put("id", Integer.parseInt(routingContext.pathParam("id")));

                eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.ACTIVATE_USER, params,
                        response -> httpService.simpleHttpResponse(routingContext, response));
            } else {
                routingContext.fail(HttpResponseStatus.FORBIDDEN);
            }
        });
    }

    public void getUser(RoutingContext routingContext) {
        String username = httpService.getUsernameFromToken(routingContext);
        JsonObject params = new JsonObject().put("username", username);

        eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.GET_USER, params,
                response -> simpleHttpResponse(routingContext, response));
    }

    public void getAllUsers(RoutingContext routingContext) {
        User user = routingContext.user();
        user.isAuthorized(AuthorityName.ROLE_ADMIN, result -> {
            if (result.succeeded() && result.result()) {
                eventBus.send(GateKeeperInfo.NAME + GateKeeperInfo.GET_ALL_USERS, null,
                        response -> simpleHttpResponse(routingContext, response));
            } else {
                routingContext.fail(HttpResponseStatus.FORBIDDEN);
            }
        });
    }

    private void simpleHttpResponse(RoutingContext routingContext, AsyncResult<Message<Object>> response) {
        if (response.succeeded()) {
            HttpServerResponse httpServerResponse = routingContext.response();
            httpServerResponse.putHeader(HttpHeader.CONTENT_TYPE, HttpHeader.CONTENT_TYPE_JSON);
            httpServerResponse.end(response.result().body().toString());
        } else {
            routingContext.fail(((ReplyException) response.cause()).failureCode());
        }
    }
}
