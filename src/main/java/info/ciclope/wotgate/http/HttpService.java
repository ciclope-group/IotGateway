package info.ciclope.wotgate.http;

import com.google.inject.Singleton;
import io.vertx.core.http.HttpServerRequest;
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
}
