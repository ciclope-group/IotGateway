package info.ciclope.wotgate.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.storage.SqliteStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

import javax.inject.Named;

public class MainModule extends AbstractModule {

    private Vertx vertx;

    public MainModule(AbstractVerticle mainVerticle) {
        this.vertx = mainVerticle.getVertx();
    }

    @Override
    protected void configure() {
        bind(Vertx.class).toInstance(vertx);
        bind(EventBus.class).toInstance(vertx.eventBus());
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
    @Named("weatherstation")
    public DatabaseStorage provideDatabaseStorageWeatherStation() {
        SqliteStorage storage = new SqliteStorage(vertx);
        storage.startDatabaseStorage("weatherstation");
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

    @Provides
    @Singleton
    @Named("weatherstation")
    public WebClient provideWebClientWeatherStation() {
        // TODO: poner host de la API
        WebClientOptions webClientOptions = new WebClientOptions()
                .setDefaultHost("ofs.fi.upm.es")
                .setDefaultPort(5000);

        return WebClient.create(vertx, webClientOptions);
    }

    @Provides
    @Singleton
    @Named("camera")
    public WebClient provideWebClientCamera() {
        // TODO: poner host de la API
        WebClientOptions webClientOptions = new WebClientOptions()
                .setDefaultHost("ofs.fi.upm.es")
                .setDefaultPort(5000);

        return WebClient.create(vertx, webClientOptions);
    }

    @Provides
    @Singleton
    public RabbitMQClient provideRabbitMQClient() {
        RabbitMQOptions config = new RabbitMQOptions();
        config.setHost("localhost")
                .setPort(5672)
                .setUser("venus")
                .setPassword("venuspass")
                .setAutomaticRecoveryEnabled(true);

        return RabbitMQClient.create(vertx, config);
    }

}
