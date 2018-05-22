package info.ciclope.wotgate.thing.driver.gatekeeper.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.Authority;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.AuthorityName;
import info.ciclope.wotgate.thing.driver.gatekeeper.model.User;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthorityService {
    private GatekeeperDatabase database;
    private JWTAuth jwtAuth;

    @Inject
    public AuthorityService(GatekeeperDatabase database, JWTAuth jwtAuth) {
        this.database = database;
        this.jwtAuth = jwtAuth;
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
                    int userId = result.result();
                    database.addUserRole(userId, AuthorityName.ROLE_USER, resultRole -> {
                        if (resultRole.succeeded()) {
                            message.reply(userId);
                        } else {
                            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                        }
                    });
                } else {
                    message.fail(HttpResponseStatus.CONFLICT, "Conflict");
                }
            });
        } catch (IllegalArgumentException e) {
            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
        }
    }




}
