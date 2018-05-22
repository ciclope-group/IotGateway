package info.ciclope.wotgate.thing.driver.gatekeeper.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.Authority;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static info.ciclope.wotgate.thing.driver.gatekeeper.database.DatabaseSql.*;

@Singleton
public class GatekeeperDatabase {
    private final DatabaseStorage databaseStorage;

    @Inject
    public GatekeeperDatabase(@Named("gatekeeper") DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void initDatabaseStorage(Handler<AsyncResult<Void>> handler) {
        List<String> batch = new ArrayList<>();
        batch.add(CREATE_USERS_TABLE);
        batch.add(CREATE_ROLES_TABLE);
        batch.add(CREATE_USER_ROLE_TABLE);
        batch.add(CREATE_RESERVATION_STATUS_TABLE);
        batch.add(CREATE_RESERVATIONS_TABLE);

        databaseStorage.executeBatch(batch, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUserByUsername(String username, Handler<AsyncResult<User>> handler) {
        String query = "SELECT * FROM user WHERE username = ?";
        JsonArray params = new JsonArray().add(username);

        databaseStorage.queryWithParameters(query, params, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() != 0) {
                    handler.handle(Future.succeededFuture(new User(result.result().getRows().get(0))));
                } else {
                    handler.handle(Future.succeededFuture(null));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getAllUsers(Handler<AsyncResult<List<User>>> handler) {
        String query = "SELECT * FROM user";

        databaseStorage.query(query, result -> {
            if (result.succeeded()) {
                List<User> userList = result.result().getRows().stream()
                        .map(User::new)
                        .collect(Collectors.toList());

                handler.handle(Future.succeededFuture(userList));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void insertUser(User user, Handler<AsyncResult<Integer>> handler) {
        String query = "INSERT INTO user(username, email, password, enabled) VALUES (?, ?, ?, 0);";
        JsonArray params = new JsonArray().add(user.getUsername()).add(user.getEmail()).add(user.getPassword());

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture(result.result().getKeys().getInteger(0)));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void activateUser(int id, Handler<AsyncResult> handler) {
        String query = "UPDATE user SET enabled = 1 WHERE id = ?";
        JsonArray params = new JsonArray().add(id);

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void getUserAuthorities(String username, Handler<AsyncResult<List<Authority>>> handler) {
        String query = "SELECT * FROM authority a " +
                "JOIN user_authority ua on a.id = ua.authority_id JOIN user u on ua.user_id = u.id " +
                "WHERE u.username = ?";
        JsonArray params = new JsonArray().add(username);

        databaseStorage.queryWithParameters(query, params, result -> {
            if (result.succeeded()) {
                List<Authority> authorities = new ArrayList<>();
                for (JsonObject object : result.result().getRows()) {
                    authorities.add(new Authority(object));
                }

                handler.handle(Future.succeededFuture(authorities));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    public void addUserRole(int userId, String roleName, Handler<AsyncResult> handler) {
        String query = "INSERT INTO user_authority SELECT ?, id FROM authority WHERE name = ?";
        JsonArray params = new JsonArray().add(userId).add(roleName);

        databaseStorage.updateWithParameters(query, params, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
