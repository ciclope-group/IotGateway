package info.ciclope.wotgate.http;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.component.ThingRequestParameter;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
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

public class ProductionHttpServer implements HttpServer {
    private static final String WOTGATE_THINGDESCRIPTION = "/";
    private static final String WOTGATE_THINGS = WOTGATE_THINGDESCRIPTION + "things";
    private static final String WOTGATE_THING_THINGDESCRIPTION = WOTGATE_THINGS + "/:" + ThingRequestParameter.PARAMETER_THING;
    private static final String WOTGATE_THING_INTERACTION = WOTGATE_THING_THINGDESCRIPTION + "/:" + ThingRequestParameter.PARAMETER_INTERACTION;
    private static final String WOTGATE_THING_INTERACTION_EXTRA_DATA = WOTGATE_THING_INTERACTION + "/:" + ThingRequestParameter.PARAMETER_EXTRA_DATA;

    private Vertx vertx;
    private Router router;
    private io.vertx.core.http.HttpServer httpServer;
    private WeatherstationService weatherstationService;
    private SecurityService securityService;
    private ThingManagerConfiguration thingManagerConfiguration;
    private JWTAuth jwtAuth;

    @Inject
    public ProductionHttpServer(Vertx vertx, WeatherstationService weatherstationService, SecurityService securityService,
                                ThingManagerConfiguration thingManagerConfiguration, JWTAuth jwtAuth) {
        this.vertx = vertx;
        this.weatherstationService = weatherstationService;
        this.securityService = securityService;
        this.thingManagerConfiguration = thingManagerConfiguration;
        this.jwtAuth = jwtAuth;

        this.router = Router.router(vertx);
    }

    @Override
    public void startHttpServer(Handler<AsyncResult<HttpServer>> handler) {
        HttpServerOptions options = new HttpServerOptions().setPort(thingManagerConfiguration.getHttpServerPort());
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
        List<String> authRoutes = Arrays.asList("/weatherstation/state");
        authRoutes.forEach(r -> router.route(r).handler(authHandler));
    }

    private void routesManager() {
        router.post("/login").handler(BodyHandler.create()).handler(securityService::login);

        router.get("/weatherstation/state").handler(weatherstationService::getState);

//        router.get(WOTGATE_THINGDESCRIPTION).handler(thingManager::getThingManagerThings);
//        router.get(WOTGATE_THINGS).handler(thingManager::getThingManagerThings);
//        router.get(WOTGATE_THINGS + "/").handler(thingManager::getThingManagerThings);
//
//        router.get(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::getThingDescription);
//        router.get(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::getThingDescription);
//        router.put(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::putThingDescription);
//        router.put(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::putThingDescription);
//        router.put(WOTGATE_THING_THINGDESCRIPTION).handler(thingManager::putThingDescription);
//        router.put(WOTGATE_THING_THINGDESCRIPTION + "/").handler(thingManager::putThingDescription);
//
//        router.get(WOTGATE_THING_INTERACTION).handler(thingManager::getThingInteraction);
//        router.get(WOTGATE_THING_INTERACTION + "/").handler(thingManager::getThingInteraction);
//        router.post(WOTGATE_THING_INTERACTION).handler(thingManager::postThingInteraction);
//        router.post(WOTGATE_THING_INTERACTION + "/").handler(thingManager::postThingInteraction);
//        router.put(WOTGATE_THING_INTERACTION).handler(thingManager::putThingInteraction);
//        router.put(WOTGATE_THING_INTERACTION + "/").handler(thingManager::putThingInteraction);
//
//        router.get(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::getThingActionTask);
//        router.get(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::getThingActionTask);
//        router.put(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::putThingActionTask);
//        router.put(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::putThingActionTask);
//        router.delete(WOTGATE_THING_INTERACTION_EXTRA_DATA).handler(thingManager::deleteThingActionTask);
//        router.delete(WOTGATE_THING_INTERACTION_EXTRA_DATA + "/").handler(thingManager::deleteThingActionTask);
    }

    @Override
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
