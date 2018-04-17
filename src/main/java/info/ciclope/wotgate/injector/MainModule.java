package info.ciclope.wotgate.injector;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.http.ProductionHttpServer;
import info.ciclope.wotgate.http.WeatherstationService;
import info.ciclope.wotgate.thingmanager.ThingManagerConfiguration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

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
    public ProductionHttpServer provideProductionHttpServer(WeatherstationService weatherstationService,
                                                            ThingManagerConfiguration thingManagerConfiguration) {
        return new ProductionHttpServer(vertx, weatherstationService, thingManagerConfiguration);
    }

    @Provides
    @Singleton
    public ThingManagerConfiguration provideThingManagerConfiguration() {
        return new ThingManagerConfiguration(mainVerticle.config());
    }

}
