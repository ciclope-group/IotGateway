package info.ciclope.wotgate.http;

import com.google.inject.Singleton;
import io.vertx.core.http.HttpServerRequest;

@Singleton
public class HttpService {

    public String getBaseUrl(HttpServerRequest request) {
        String scheme = request.scheme() + "://";
        String serverName = request.host();

        return scheme + serverName;
    }
}
