package info.ciclope.wotgate.http.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class SecurityCameraController {
    private static final String EXTERNAL_CAMERA = "cameras/external.jpg";
    private static final String INTERNAL_CAMERA = "cameras/internal%s.jpg";

    private FileSystem fs;

    @Inject
    public SecurityCameraController(Vertx vertx) {
        this.fs = vertx.fileSystem();
    }

    public void externalCamera(RoutingContext routingContext) {
        getImage(routingContext, EXTERNAL_CAMERA);
    }

    public void internalCamera(RoutingContext routingContext) {
        String imgPath = String.format(INTERNAL_CAMERA, routingContext.pathParam("id"));
        getImage(routingContext, imgPath);
    }

    private void getImage(RoutingContext routingContext, String imgPath) {
        // Check if image exists
        if (fs.existsBlocking(imgPath)) {
            Buffer buffer = fs.readFileBlocking(imgPath);
            HttpServerResponse response = routingContext.response();
            response.putHeader(HttpHeaders.CONTENT_TYPE, HttpHeader.CONTENT_TYPE_IMAGE);
            response.end(buffer);
        } else {
            routingContext.fail(HttpStatus.RESOURCE_NOT_FOUND);
        }
    }

}
