package info.ciclope.wotgate.thing.driver.gatekeeper.interaction;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.component.ThingActionTask;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.Authority;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Named;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthorityService {
    private static final Integer TOKEN_LIFE_WINDOW = 3600;
    private final DatabaseStorage databaseStorage;
    private final GatekeeperDatabase database;

    private JWTAuth jwtAuth;

    @Inject
    public AuthorityService(@Named("gatekeeper") DatabaseStorage databaseStorage, GatekeeperDatabase database, JWTAuth jwtAuth) {
        this.databaseStorage = databaseStorage;
        this.database = database;
        this.jwtAuth = jwtAuth;
    }

    public void getTokenOwner(String token, Handler<AsyncResult<String>> handler) {
        String query = "SELECT name FROM users WHERE token='" + token + "';";
        databaseStorage.query(query, resultSet -> {
            if (resultSet.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
            } else if (resultSet.result().getRows().isEmpty()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.UNAUTHORIZED.toString())));
            } else {
                handler.handle(Future.succeededFuture(resultSet.result().getResults().get(0).getString(0)));
            }
        });
    }

    public void getTokenOwnerRoles(String token, Handler<AsyncResult<JsonArray>> handler) {
        String query = "SELECT json_group_array(userroles.name) FROM (SELECT roles.name FROM users, roles, user_in_role WHERE users.id = user_in_role.user AND user_in_role.role = roles.id AND users.token ='" + token + "' GROUP BY roles.name UNION SELECT roles.name FROM (SELECT roles.name, roles.level FROM users, roles, user_in_role WHERE users.id = user_in_role.user AND user_in_role.role = roles.id AND users.token ='" + token + "' GROUP BY roles.name) AS userroles, roles WHERE roles.level > userroles.level GROUP BY roles.name) AS userroles";
        databaseStorage.query(query, resultSet -> {
            if (resultSet.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), resultSet.cause())));
            } else if (resultSet.result().getRows().isEmpty()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.UNAUTHORIZED.toString())));
            } else {
                handler.handle(Future.succeededFuture(new JsonArray(resultSet.result().getResults().get(0).getString(0))));
            }
        });
    }

    public void generateUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userPassword = request.getHeader(HttpHeader.HEADER_AUTHORIZATION);
        if (userPassword == null || userPassword.length() < 9 || !userPassword.substring(0, 6).equals("Basic ")) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        byte[] bytes = Base64.getDecoder().decode(userPassword.substring(6));
        if (bytes == null) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        userPassword = new String(bytes);
        String[] authorization = userPassword.split(":", 2);
        if (authorization == null || authorization.length < 2) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        JsonObject tokenObject = generateToken(authorization[0], TOKEN_LIFE_WINDOW);

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.insertUserToken(authorization[0], authorization[1],
                tokenObject.getString("token"), tokenObject.getString("expirationTime"), result -> {
                    if (result.succeeded()) {
                        if (result.result().getTotal() > 0) {
                            task.setStatus(ThingActionTask.TASK_STATUS_OK);
                            task.setOutputData(tokenObject);
                            message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                        } else {
                            task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                        }
                    } else {
                        task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                        message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                    }
                });
    }

    public void revokeUserToken(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserToken(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getUserPermissions(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getUserPermissions(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    task.setOutputData(result.result().getResult());
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    private JsonObject generateToken(String userName, long lifeTimeSeconds) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentTimeStamp = now.plusSeconds(lifeTimeSeconds).toString();
        PasswordManager tokenManager = new PasswordManager();

        String token = tokenManager.hash((userName + currentTimeStamp).toCharArray());
        JsonObject result = new JsonObject();
        result.put("token", token);
        result.put("expirationTime", currentTimeStamp);
        result.put("lifeWindow", lifeTimeSeconds);

        return result;
    }

    public void login(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body == null || !body.containsKey("username") || !body.containsKey("password")) {
            message.fail(400, "Bad Request");
            return;
        }

        String username = body.getString("username");
        database.getUserByUsername(username, result -> {
            if (result.succeeded()) {
                User user = result.result();
                // Check password with Bcrypt
                if (user.isEnabled() && BCrypt.checkpw(body.getString("password"), user.getPassword())) {
                    // Obtain user authorities
                    database.getUserAuthorities(username, resultAuthorities -> {
                        List<String> authorityNames = resultAuthorities.result().stream()
                                .map(Authority::getName)
                                .collect(Collectors.toList());

                        // Generate token and add authorities
                        String token = jwtAuth.generateToken(new JsonObject(),
                                new JWTOptions().setAlgorithm("HS512").setSubject(username).setPermissions(authorityNames));

                        message.reply(new JsonObject().put("token", token));
                    });
                } else {
                    message.fail(HttpResponseStatus.UNAUTHORIZED, "Unauthorized");
                }
            } else {
                message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
            }
        });

    }

    public void register(Message<JsonObject> message) {
        try {
            User user = message.body().mapTo(User.class);
            if (!user.validate()) {
                message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                return;
            }

            // Bcrypt password
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
            database.insertUser(user, result -> {
                if (result.succeeded()) {
                    message.reply(result.result());
                } else {
                    message.fail(HttpResponseStatus.CONFLICT, "Conflict");
                }
            });
        } catch (IllegalArgumentException e) {
            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
        }
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
