package info.ciclope.wotgate.thing.gatekeeper.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.gatekeeper.database.GatekeeperDatabase;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Collectors;

@Singleton
public class UserService {
    private GatekeeperDatabase database;

    @Inject
    public UserService(GatekeeperDatabase database) {
        this.database = database;
    }

    public void getUserByUsername(Message<JsonObject> message) {
        String username = message.body().getString("username");

        database.getUserByUsername(username, result -> {
            if (result.succeeded()) {
                message.reply(JsonObject.mapFrom(result.result()));
            } else {
                message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
            }
        });
    }

    public void getAllUsers(Message<JsonObject> message) {
        database.getAllUsers(result -> {
            if (result.succeeded()) {
                JsonArray jsonArray = result.result().stream()
                        .map(JsonObject::mapFrom)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), JsonArray::new));
                message.reply(jsonArray);
            } else {
                message.fail(HttpResponseStatus.INTERNAL_ERROR, "Error");
            }
        });
    }

    public void activateUser(Message<JsonObject> message) {
        int id = message.body().getInteger("id");

        database.activateUser(id, result -> {
            if (result.succeeded()) {
                message.reply(null);
            } else {
                message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
            }
        });
    }
}
