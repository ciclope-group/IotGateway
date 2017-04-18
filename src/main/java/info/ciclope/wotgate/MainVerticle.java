package info.ciclope.wotgate;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;


public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    }
}
