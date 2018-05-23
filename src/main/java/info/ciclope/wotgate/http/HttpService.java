package info.ciclope.wotgate.http;

import com.google.inject.Singleton;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class HttpService {

    public String getBaseUrl(HttpServerRequest request) {
        String scheme = request.scheme() + "://";
        String serverName = request.host();

        return scheme + serverName;
    }

    public String getUsernameFromToken(RoutingContext context) {
        return context.user().principal().getString("sub");
    }

    public void simpleHttpResponse(RoutingContext routingContext, AsyncResult<Message<Object>> response) {
        if (response.succeeded()) {
            HttpServerResponse httpServerResponse = routingContext.response();
            httpServerResponse.putHeader(HttpHeader.CONTENT_TYPE, HttpHeader.CONTENT_TYPE_JSON);
            httpServerResponse.end(response.result().body().toString());
        } else {
            routingContext.fail(((ReplyException) response.cause()).failureCode());
        }
    }
}
