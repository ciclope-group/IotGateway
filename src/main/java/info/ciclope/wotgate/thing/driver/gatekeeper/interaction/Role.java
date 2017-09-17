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
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class Role {
    private final GatekeeperDatabase database;

    public Role(GatekeeperDatabase database) {
        this.database = database;
    }

    public void getAllRoles(Message<JsonObject> message) {
        ThingActionTask task = new ThingActionTask();
        database.getAllRoles(result -> {
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

    public void getRoleByName(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getBody().getString("name");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getRoleByName(name, result -> {
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

    public void getRolesByLevel(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        Integer level;
        try {
            level = request.getBody().getInteger("level");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (level == null || level < 0) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getRolesByLevel(level, result -> {
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

    public void addRole(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        Integer level;
        try {
            name = request.getBody().getString("name");
            level = request.getBody().getInteger("level");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || level == null || name.isEmpty() || level < 0) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.insertRole(name, level, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void deleteRoleByName(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getBody().getString("name");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if ("Administrator".equals(name) || "Privileged".equals(name) || "Authenticated".equals(name)) {
            message.reply(new ThingResponse(HttpResponseStatus.FORBIDDEN, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteRoleByName(name, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
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

    public void addUserToRole(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName, roleName;
        try {
            userName = request.getBody().getString("userName");
            roleName = request.getBody().getString("roleName");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (userName == null || roleName == null || userName.isEmpty() || roleName.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.addUserToRole(userName, roleName, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void deleteUserFromRole(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String userName, roleName;
        try {
            userName = request.getBody().getString("userName");
            roleName = request.getBody().getString("roleName");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (userName == null || roleName == null || userName.isEmpty() || roleName.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserFromRole(userName, roleName, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }
}
