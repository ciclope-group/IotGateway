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

package info.ciclope.wotgate.thingmanager;

import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.injector.DependenceFactory;
import info.ciclope.wotgate.thing.component.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class ProductionThingManager implements ThingManager {
    private final ThingManagerStorage thingManagerStorage;
    private final Map<String, ThingInformation> thingMap;
    private Vertx vertx;
    private EventBus eventBus;

    public ProductionThingManager(DependenceFactory dependenceFactory) {
        thingManagerStorage = dependenceFactory.createThingManagerStorage();
        thingMap = new HashMap<>();
        this.vertx = dependenceFactory.getVertxInstance();
        this.eventBus = this.vertx.eventBus();
    }

    @Override
    public void insertThing(ThingConfiguration thingConfiguration, Handler<AsyncResult<Void>> result) {
        DeploymentOptions options = new DeploymentOptions().setConfig(thingConfiguration.getThingConfiguration());
        vertx.deployVerticle(thingConfiguration.getThingClassname(), options, deployment -> {
            if (deployment.succeeded()) {
                recoverThingDescription(thingConfiguration.getThingName(), recover -> {
                    if (recover.succeeded()) {
                        thingMap.put(thingConfiguration.getThingName(), new ThingInformation(recover.result(), deployment.result()));
                        result.handle(Future.succeededFuture());
                    } else {
                        vertx.undeploy(deployment.result());
                        result.handle(Future.failedFuture(recover.cause()));
                    }
                });
            } else {
                result.handle(Future.failedFuture(deployment.cause()));
            }
        });
// TODO: insert abstractthing in the abstractthing storage
//            thingManagerStorage.insertThing(thingConfiguration, insertResult -> {
//                if (insertResult.succeeded()) {
//                    thingMap.put(thingConfiguration.getThingName(), abstractthing);
//                    result.handle(Future.succeededFuture());
//                } else {
//                    result.handle(Future.failedFuture(insertResult.cause()));
//                }
//            });
    }

    @Override
    public void deleteThing(String name, Handler<AsyncResult<Void>> result) {
        vertx.undeploy(thingMap.get(name).getThingDeploymentId(), undeployment -> {
            if (undeployment.succeeded()) {
                thingMap.remove(name);
                result.handle(Future.succeededFuture());
            } else {
                result.handle(Future.failedFuture(undeployment.cause()));
            }
        });
// TODO: delete abstractthing from abstractthing storage
//        thingManagerStorage.deleteThing(name, deleteResult -> {
//            if (deleteResult.succeeded()) {
//                thingMap.remove(name);
//                result.handle(Future.succeededFuture());
//            } else {
//                result.handle(Future.failedFuture(deleteResult.cause()));
//            }
//        });
    }

    @Override
    public void getThingManagerThings(RoutingContext routingContext) {

    }

    @Override
    public void getThingDescription(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    eventBus.send(ThingAddress.getGetThingThingDescriptionAddress(thingName), message, sendMessage -> {
                        if (sendMessage.succeeded()) {
                            response(routingContext, (JsonObject) sendMessage.result().body());
                        } else {
                            routingContext.fail(sendMessage.cause());
                        }
                    });
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void putThingDescription(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName) &&
                        thingMap.get(thingName).getThingDescription().isWritableThingDescription()) {
                    if (authorizationResult.result().
                            containsRole(thingMap.get(thingName).getThingDescription().getThingDescriptionRoleBasedWritingAccesControl())) {
                        eventBus.send(ThingAddress.getPutThingThingDescriptionAddress(thingName), message, sendMessage -> {
                            if (sendMessage.succeeded()) {
                                thingMap.get(thingName).setThingDescription(new ThingDescription(routingContext.getBodyAsJson()));
                                routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT);
                                response(routingContext, (JsonObject) sendMessage.result().body());
                            } else {
                                routingContext.fail(sendMessage.cause());
                            }
                        });
                    } else {
                        routingContext.fail(HttpResponseStatus.FORBIDDEN);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void getThingInteraction(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName) &&
                        (thingMap.get(thingName).getThingDescription().containsProperty(interactionName) ||
                                thingMap.get(thingName).getThingDescription().isGetAction(interactionName))) {
                    if (authorizationResult.result().
                            containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                        eventBus.send(ThingAddress.getGetThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                            if (sendMessage.succeeded()) {
                                response(routingContext, (JsonObject) sendMessage.result().body());
                            } else {
                                routingContext.fail(sendMessage.cause());
                            }
                        });
                    } else {
                        routingContext.fail(HttpResponseStatus.FORBIDDEN);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void postThingInteraction(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
                    if (thingDescription.isPostAction(interactionName)) {
                        if (authorizationResult.result().
                                containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                            eventBus.send(ThingAddress.getPostThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                                if (sendMessage.succeeded()) {
                                    response(routingContext, (JsonObject) sendMessage.result().body());
                                } else {
                                    routingContext.fail(sendMessage.cause());
                                }
                            });
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN);
                        }
                    } else {
                        routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void putThingInteraction(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
                    if (thingDescription.containsProperty(interactionName) && thingDescription.isWritableInteraction(interactionName)) {
                        if (authorizationResult.result().
                                containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedWritingAccesControl(interactionName)) ||
                                authorizationResult.result().
                                        containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                            eventBus.send(ThingAddress.getPutThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                                if (sendMessage.succeeded()) {
                                    response(routingContext, (JsonObject) sendMessage.result().body());
                                } else {
                                    routingContext.fail(sendMessage.cause());
                                }
                            });
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN);
                        }
                    } else {
                        routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void getThingActionTask(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
                    if (thingDescription.isObservableAction(interactionName)) {
                        if (authorizationResult.result().
                                containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                            eventBus.send(ThingAddress.getGetThingActionTaskAddress(thingName, interactionName), message, sendMessage -> {
                                if (sendMessage.succeeded()) {
                                    response(routingContext, (JsonObject) sendMessage.result().body());
                                } else {
                                    routingContext.fail(sendMessage.cause());
                                }
                            });
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN);
                        }
                    } else {
                        routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });
    }

    @Override
    public void putThingActionTask(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
                    if (thingDescription.isObservableAction(interactionName) && thingDescription.isWritableInteraction(interactionName)) {
                        if (authorizationResult.result().
                                containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedWritingAccesControl(interactionName)) ||
                                authorizationResult.result().
                                        containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                            eventBus.send(ThingAddress.getPutThingActionTaskAddress(thingName, interactionName), message, sendMessage -> {
                                if (sendMessage.succeeded()) {
                                    response(routingContext, (JsonObject) sendMessage.result().body());
                                } else {
                                    routingContext.fail(sendMessage.cause());
                                }
                            });
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN);
                        }
                    } else {
                        routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });

    }

    @Override
    public void deleteThingActionTask(RoutingContext routingContext) {
        String thingName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(ThingRequestParameter.PARAMETER_INTERACTION);
        String token = routingContext.request().getParam(ThingRequestParameter.PARAMETER_TOKEN);
        getInteractionAuthorization(token, authorizationResult -> {
            if (authorizationResult.succeeded()) {
                JsonObject message = new ThingRequest(routingContext, authorizationResult.result()).getRequest();
                if (thingMap.containsKey(thingName)) {
                    ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
                    if (thingDescription.isObservableAction(interactionName)
                            && thingDescription.isWritableInteraction(interactionName)) {
                        if (authorizationResult.result().
                                containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedWritingAccesControl(interactionName)) ||
                                authorizationResult.result().
                                        containsRole(thingMap.get(thingName).getThingDescription().getInteractionRoleBasedAccesControl(interactionName))) {
                            eventBus.send(ThingAddress.getDeleteThingActionTaskAddress(thingName, interactionName), message, sendMessage -> {
                                if (sendMessage.succeeded()) {
                                    response(routingContext, (JsonObject) sendMessage.result().body());
                                } else {
                                    routingContext.fail(sendMessage.cause());
                                }
                            });
                        } else {
                            routingContext.fail(HttpResponseStatus.FORBIDDEN);
                        }
                    } else {
                        routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                    }
                } else {
                    routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
                }
            } else {
                routingContext.fail(HttpResponseStatus.INTERNAL_ERROR);
            }
        });

    }

    @Override
    public void stopThingManager() {
        thingManagerStorage.closeThingManagerStorage();
    }

    private void recoverThingDescription(String thingName, Handler<AsyncResult<ThingDescription>> handler) {
        eventBus.send(ThingAddress.getProvideThingThingDescriptionAddress(thingName), null, sendMessage -> {
            if (sendMessage.succeeded()) {
                handler.handle(Future.succeededFuture(new ThingDescription((JsonObject) sendMessage.result().body())));
            } else {
                handler.handle(Future.failedFuture(sendMessage.cause()));
            }
        });
    }

    private void response(RoutingContext routingContext, JsonObject response) {
        ThingResponse thingResponse = new ThingResponse(response);
        JsonObject headers = thingResponse.getHeaders();
        HttpServerResponse httpServerResponse = routingContext.response();
        for (Map.Entry<String, Object> header : headers) {
            httpServerResponse = httpServerResponse.putHeader(header.getKey(), (String) header.getValue());
        }
        httpServerResponse = httpServerResponse.setStatusCode(thingResponse.getStatus());
        if (thingResponse.isJsonObjectBody()) {
            httpServerResponse.end(Json.encodePrettily(thingResponse.getJsonObjectBody()));
        } else if (thingResponse.isJsonArrayBody()) {
            httpServerResponse.end(Json.encodePrettily(thingResponse.getJsonArrayBody()));
        } else {
            httpServerResponse.end(thingResponse.getStringBody());
        }
    }

    private void getInteractionAuthorization(String token, Handler<AsyncResult<InteractionAuthorization>> handler) {
        eventBus.send(ThingAddress.getThingInteractionAuthenticationAddress(), token, sendMessage -> {
            if (sendMessage.succeeded()) {
                handler.handle(Future.succeededFuture(new InteractionAuthorization((JsonObject) sendMessage.result().body())));
            } else {
                handler.handle(Future.failedFuture(sendMessage.cause()));
            }
        });
    }

}
