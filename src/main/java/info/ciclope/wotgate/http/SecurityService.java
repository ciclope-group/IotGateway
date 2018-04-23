package info.ciclope.wotgate.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.thing.driver.gatekeeper.GateKeeperInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class SecurityService {

    private EventBus eventBus;

    @Inject
    public SecurityService(EventBus eventBus) {
        this.eventBus = eventBus;
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

}
