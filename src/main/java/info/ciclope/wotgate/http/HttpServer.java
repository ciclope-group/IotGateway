package info.ciclope.wotgate.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Singleton
public class HttpServer {
    private static final int PORT = 8080;

    private Vertx vertx;
    private Router router;
    private io.vertx.core.http.HttpServer httpServer;
    private WeatherstationController weatherstationController;
    private SecurityController securityController;
    private ReservationController reservationController;
    private JWTAuth jwtAuth;

    @Inject
    public HttpServer(Vertx vertx, JWTAuth jwtAuth, WeatherstationController weatherstationController,
                      SecurityController securityController, ReservationController reservationController) {
        this.vertx = vertx;
        this.jwtAuth = jwtAuth;
        this.router = Router.router(vertx);

        this.weatherstationController = weatherstationController;
        this.securityController = securityController;
        this.reservationController = reservationController;
    }

    public void startHttpServer(Handler<AsyncResult<HttpServer>> handler) {
        HttpServerOptions options = new HttpServerOptions().setPort(PORT);
        httpServer = vertx.createHttpServer(options).requestHandler(router::accept).listen(result -> {
            if (result.succeeded()) {
                configSecurity();
                routesManager();
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void configSecurity() {
        // Allow CORS
        HttpMethod[] httpMethods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS};
        String[] headers = {"Content-Type", "Authorization", "X-Requested-With"};

        router.route().handler(CorsHandler.create("*")
                .allowedMethods(new HashSet<>(Arrays.asList(httpMethods)))
                .allowedHeaders(new HashSet<>(Arrays.asList(headers)))
                .maxAgeSeconds(3600));


        // JWT
        JWTAuthHandler authHandler = JWTAuthHandler.create(jwtAuth);

        // Routes that require authentication
        List<String> authRoutes = Arrays.asList("/weatherstation/state",
                "/users/:id/activate",
                "/users/logged",
                "/users",
                "/reservations",
                "/reservations/:id/cancel",
                "/reservations/:id/complete");
        authRoutes.forEach(r -> router.route(r).handler(authHandler));
    }

    private void routesManager() {
        // Security
        router.post("/login").handler(BodyHandler.create()).handler(securityController::login);
        router.post("/register").handler(BodyHandler.create()).handler(securityController::register);

        // Users
        router.post("/users/:id/activate").handler(securityController::activateUser);
        router.get("/users/logged").handler(securityController::getUser);
        router.get("/users").handler(securityController::getAllUsers);

        // Reservation
        router.get("/reservations").handler(reservationController::getAllReservations);
        router.post("/reservations").handler(BodyHandler.create()).handler(reservationController::createReservation);
        router.put("/reservations/:id/cancel").handler(reservationController::cancelReservation);
        router.put("/reservations/:id/complete").handler(reservationController::completeReservation);

        // Weather station
        router.get("/weatherstation/state").handler(weatherstationController::getState);
    }

    public void stopHttpServer(Handler<AsyncResult<Void>> handler) {
        if (httpServer == null) {
            handler.handle(Future.succeededFuture());
            return;
        }

        httpServer.close(result -> {
            httpServer = null;
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

}
