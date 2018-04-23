package info.ciclope.wotgate.injector;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.http.ProductionHttpServer;
import info.ciclope.wotgate.http.SecurityService;
import info.ciclope.wotgate.http.WeatherstationService;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.storage.SqliteStorage;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import javax.inject.Named;

public class MainModule extends AbstractModule {

    private AbstractVerticle mainVerticle;
    private Vertx vertx;

    public MainModule(AbstractVerticle mainVerticle) {
        this.mainVerticle = mainVerticle;
        this.vertx = mainVerticle.getVertx();
    }

    @Override
    protected void configure() {
        bind(EventBus.class).toInstance(vertx.eventBus());
        bind(HttpServer.class).to(ProductionHttpServer.class);
    }

    @Provides
    @Singleton
    public ProductionHttpServer provideProductionHttpServer(ThingManagerConfiguration thingManagerConfiguration, WeatherstationService weatherstationService,
                                                            SecurityService securityService, JWTAuth jwtAuth) {
        return new ProductionHttpServer(vertx, weatherstationService, securityService, thingManagerConfiguration, jwtAuth);
    }

    @Provides
    @Singleton
    public ThingManagerConfiguration provideThingManagerConfiguration() {
        return new ThingManagerConfiguration(mainVerticle.config());
    }

    @Provides
    @Singleton
    @Named("gatekeeper")
    public DatabaseStorage provideDatabaseStorageGatekeeper() {
        SqliteStorage storage = new SqliteStorage(vertx);
        storage.startDatabaseStorage("gatekeeper");
        return storage;
    }

    @Provides
    @Singleton
    public JWTAuth provideJwtAuth() {
        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setPath("keystore.jceks")
                        .setPassword("uOzcvDG4zRdgaItG"));

        return JWTAuth.create(vertx, config);
    }

}
