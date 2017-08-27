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
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class User {
    private final GatekeeperDatabase database;

    public User(GatekeeperDatabase database) {
        this.database = database;
    }

    public void getAllUsers(Message<JsonObject> message) {
        database.getAllUsers(result -> {
            if (result.succeeded()) {
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result().getResult()).getResponse());
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), "").getResponse());
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
        database.getUser(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() == 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result().getResult()).getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), "").getResponse());
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
        database.getUserByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() == 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result().getResult()).getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), "").getResponse());
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
        database.getUserByEmail(email, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() == 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), result.result().getResult()).getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), "").getResponse());
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
        database.deleteUserByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
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
        database.deleteUserByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
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

        database.updateUserPassword(name, hashedPassword, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), "").getResponse());
                } else {
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
                }
            } else {
                message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            }
        });
    }

    private boolean isValidPassword(String password) {
        return password.matches("(?=\\S+$).{8,}");
    }


//    public void registerUser(final JsonObject user, Handler<AsyncResult<Void>> handler) {
//        JsonObject data = new JsonObject();
//        Instant now = Instant.now();
//        now.atZone(ZoneId.of("UTC"));
//        EmailValidator emailValidator = EmailValidator.getInstance();
//        if ((user.containsKey("name") && user.getString("name").matches("(?=\\S+$).{5,}")) &&
//                (user.containsKey("password") && user.getString("password").matches("(?=\\S+$).{8,}")) &&
//                (user.containsKey("email") && emailValidator.isValid(user.getString("email")))) {
//            // (?=.*[0-9]) a digit must occur at least once
//            // (?=.*[a-z]) a lower case letter must occur at least once
//            // (?=.*[A-Z]) an upper case letter must occur at least once
//            // (?=.*[@#$%^&+=]) a special character must occur at least once
//            // (?=\\S+$) no whitespace allowed in the entire string
//            // .{8,} at least 8 characters
//            String currentTimestamp = now.toString();
//            data.put("name", user.getString("name"));
//            data.put("email", user.getString("email"));
//            data.put("online", false);
//            data.put("dateCreated", currentTimestamp);
//            data.put("dateModified", currentTimestamp);
//        } else {
//            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
//            return;
//        }
//        // json: name, email, online, dateCreated, dateModified
//        PasswordManager passwordManager = new PasswordManager();
//        String insertUserSql = "INSERT INTO users (data, name, email, password, validated) VALUES ('" + data.toString() + "','" + user.getString("name") + "','" + user.getString("email") + "','" + passwordManager.hash(user.getString("password").toCharArray()) + "',0);";
//        String insertUserRoleSql = "INSERT INTO users_in_role (user, role) VALUES (last_insert_rowid(), 3);";
//        List<String> sqlBatch = new ArrayList<>();
//        sqlBatch.add(insertUserSql);
//        sqlBatch.add(insertUserRoleSql);
//        databaseStorage.startTransactionConnection(sqlConnection -> {
//            databaseStorage.executeBatch(sqlConnection.result(), sqlBatch, result -> {
//                if (result.failed()) {
//                    databaseStorage.stopTransactionConnection(sqlConnection.result(), stopTransaction -> {
//                        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
//                    });
//                } else {
//                    databaseStorage.stopTransactionConnection(sqlConnection.result(), stopTransaction -> {
//                        if (stopTransaction.succeeded()) {
//                            handler.handle(Future.succeededFuture());
//                        } else {
//                            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), stopTransaction.cause())));
//                        }
//                    });
//                }
//            });
//        });
//    }
//


}
