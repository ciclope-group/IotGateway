/*
 *  Copyright (c) 2017, Javier Martínez Villacampa
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.ciclope.wotgate.thing.driver.gatekeeper.interaction;

import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.component.ThingActionTask;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public class User {
    private static final long TOKEN_SECONDS_LIFE_WINDOW = 3600;
    private final GatekeeperDatabase database;
    private Vertx vertx;

    public User(GatekeeperDatabase database, Vertx vertx) {
        this.database = database;
        this.vertx = vertx;
    }

    public void getAllUsers(Message<JsonObject> message) {
        ThingActionTask task = new ThingActionTask();
        database.getAllUsers(result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void getUser(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getUser(name, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void getUserByName(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getBody().getString("name");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getUserByName(name, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void getUserByEmail(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String email;
        try {
            email = request.getBody().getString("email");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (email == null) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getUserByEmail(email, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void deleteUser(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void deleteUserByName(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getBody().getString("name");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void changeUserPassword(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, password;
        try {
            name = request.getInteractionAuthorization().getUsername();
            password = request.getBody().getString("password");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || password == null || !isValidPassword(password)) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        // HASH PASSWORD
        String hashedPassword = new PasswordManager().hash(password.toCharArray());

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.updateUserPassword(name, hashedPassword, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void registerUser(Message<JsonObject> message) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        PasswordManager passwordManager = new PasswordManager();
        ThingRequest request = new ThingRequest(message.body());
        String name, password, email;
        try {
            name = request.getBody().getString("name");
            password = request.getBody().getString("password");
            email = request.getBody().getString("email");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || password == null || email == null
                || !isValidPassword(password) || !emailValidator.isValid(email)) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        // HASH PASSWORD
        String hashedPassword = passwordManager.hash(password.toCharArray());

        // Generate Token
        String token = generateToken();

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.registerUser(name, email, hashedPassword, token, generateExpirationDateTime(TOKEN_SECONDS_LIFE_WINDOW), result -> {
            if (result.succeeded()) {
                // TODO: send email with user confirmation link
                task.setOutputData(new JsonObject().put("token", token));
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
            }
            message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
        });
    }

    public void confirmUserRegistration(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, email, token;
        try {
            name = request.getBody().getString("name");
            email = request.getBody().getString("email");
            token = request.getBody().getString("token");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || email == null || token == null || name.isEmpty() || email.isEmpty() || token.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.validateUser(name, email, token, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void recoverUserPassword(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, email;
        try {
            name = request.getBody().getString("name");
            email = request.getBody().getString("email");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || email == null || name.isEmpty() || email.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        // Generate Token
        String token = generateToken();

        // Generate new password
        String newPassword = UUID.randomUUID().toString().substring(0, 12);
        String hashedPassword = new PasswordManager().hash(newPassword.toCharArray());

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.recoverUserPassword(token, name, email, hashedPassword, generateExpirationDateTime(TOKEN_SECONDS_LIFE_WINDOW), result -> {
            if (result.succeeded()) {
                // TODO: send email with user confirmation link
                task.setOutputData(new JsonObject().put("token", token).put("newPassword", newPassword));
            }
            task.setStatus(ThingActionTask.TASK_STATUS_OK);
            message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
        });
    }

    public void confirmPasswordRecovery(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, email, token;
        try {
            name = request.getBody().getString("name");
            email = request.getBody().getString("email");
            token = request.getBody().getString("token");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || email == null || token == null || name.isEmpty() || email.isEmpty() || token.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }

        ThingActionTask task = new ThingActionTask(request.getBody());
        database.validatePasswordRecovery(name, email, token, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void deleteExpiredUserRegistrations(long id) {
        database.deleteExpiredUserRegistrations(result -> {
        });
    }

    public void deleteExpiredPasswordRecoveries(long id) {
        database.deleteExpiredPasswordRecoveries(result -> {
        });
    }

    private boolean isValidPassword(String password) {
        // (?=.*[0-9]) a digit must occur at least once
        // (?=.*[a-z]) a lower case letter must occur at least once
        // (?=.*[A-Z]) an upper case letter must occur at least once
        // (?=.*[@#$%^&+=]) a special character must occur at least once
        // (?=\\S+$) no whitespace allowed in the entire string
        // .{8,} at least 8 characters

        return password.matches("(?=\\S+$).{8,}");
    }

    private String generateToken() {
        PasswordManager passwordManager = new PasswordManager();
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentDateTime = now.toString();
        return passwordManager.hash(currentDateTime.toCharArray());
    }

    private String generateExpirationDateTime(long seconds) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        return now.plusSeconds(seconds).toString();
    }

    private void sendMail(String to, String body) {
        MailConfig config = new MailConfig();
        config.setHostname("smtp.gmail.com");
        config.setPort(465);
        config.setSsl(true);
        config.setLogin(LoginOption.REQUIRED);
        config.setUsername("");
        config.setPassword("");
        MailClient mailClient = MailClient.createNonShared(vertx, config);

        MailMessage messager = new MailMessage();
        messager.setFrom("");
        messager.setTo(to);
        messager.setText("Recover user password from web things");
        messager.setHtml("this is html text <a href=\"http://vertx.io\">vertx.io</a>");
        mailClient.sendMail(messager, results -> {
            if (results.succeeded()) {
                System.out.println(results.result());
            } else {
                results.cause().printStackTrace();
            }
        });
    }

}
