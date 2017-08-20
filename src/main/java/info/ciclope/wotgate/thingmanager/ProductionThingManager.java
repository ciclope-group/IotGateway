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

import info.ciclope.wotgate.http.HttpHeader;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.http.HttpServer;
import info.ciclope.wotgate.injector.DependenceFactory;
import info.ciclope.wotgate.thing.component.ThingAddress;
import info.ciclope.wotgate.thing.component.ThingConfiguration;
import info.ciclope.wotgate.thing.component.ThingDescription;
import info.ciclope.wotgate.thing.component.ThingRequest;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
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
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        if (thingMap.containsKey(thingName)) {
            eventBus.send(ThingAddress.getGetThingThingDescriptionAddress(thingName), message, sendMessage -> {
                if (sendMessage.succeeded()) {
                    routingContext.response()
                            .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(sendMessage.result().body()));
                } else {
                    routingContext.fail(sendMessage.cause());
                }
            });
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void putThingDescription(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        if (thingMap.containsKey(thingName)) {
            eventBus.send(ThingAddress.getPutThingThingDescriptionAddress(thingName), message, sendMessage -> {
                if (sendMessage.succeeded()) {
                    thingMap.get(thingName).setThingDescription(new ThingDescription(routingContext.getBodyAsJson()));
                    routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT);
                } else {
                    routingContext.fail(sendMessage.cause());
                }
            });
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void getThingInteraction(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName) &&
                (thingMap.get(thingName).getThingDescription().containsProperty(interactionName) ||
                        thingMap.get(thingName).getThingDescription().isGetAction(interactionName))) {
            eventBus.send(ThingAddress.getGetThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                if (sendMessage.succeeded()) {
                    routingContext.response()
                            .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                } else {
                    routingContext.fail(sendMessage.cause());
                }
            });
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void postThingInteraction(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName) &&
                thingMap.get(thingName).getThingDescription().containsInteraction(interactionName)) {
            ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
            if ((thingDescription.isThingArrayProperty(interactionName) &&
                    thingDescription.isWritableProperty(interactionName)) ||
                    (thingDescription.isPostAction(interactionName))) {
                eventBus.send(ThingAddress.getPostThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                    if (sendMessage.succeeded()) {
                        routingContext.response()
                                .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                    } else {
                        routingContext.fail(sendMessage.cause());
                    }
                });
            } else {
                routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
            }
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void putThingInteraction(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName)) {
            ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
            if (thingDescription.isWritableProperty(interactionName)) {
                eventBus.send(ThingAddress.getPutThingInteractionAddress(thingName, interactionName), message, sendMessage -> {
                    if (sendMessage.succeeded()) {
                        routingContext.response()
                                .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                    } else {
                        routingContext.fail(sendMessage.cause());
                    }
                });
            } else {
                routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
            }
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void getThingInteractionExtraData(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName)) {
            ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
            if (thingDescription.isThingArrayProperty(interactionName) ||
                    thingDescription.isObservableAction(interactionName)) {
                eventBus.send(ThingAddress.getGetThingInteractionExtraDataAddress(thingName, interactionName), message, sendMessage -> {
                    if (sendMessage.succeeded()) {
                        routingContext.response()
                                .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                    } else {
                        routingContext.fail(sendMessage.cause());
                    }
                });
            } else {
                routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
            }
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }

    }

    @Override
    public void putThingInteractionExtraData(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName)) {
            ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
            if ((thingDescription.isThingArrayProperty(interactionName)
                    && thingDescription.isWritableProperty(interactionName)) ||
                    thingDescription.isObservableAction(interactionName)) {
                eventBus.send(ThingAddress.getPutThingInteractionExtraDataAddress(thingName, interactionName), message, sendMessage -> {
                    if (sendMessage.succeeded()) {
                        routingContext.response()
                                .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                    } else {
                        routingContext.fail(sendMessage.cause());
                    }
                });
            } else {
                routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
            }
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }

    }

    @Override
    public void deleteThingInteractionExtraData(RoutingContext routingContext) {
        JsonObject message = new ThingRequest(routingContext, new InteractionAuthorization()).getRequest();
        String thingName = routingContext.request().getParam(HttpServer.PARAMETER_THING);
        String interactionName = routingContext.request().getParam(HttpServer.PARAMETER_INTERACTION);
        if (thingMap.containsKey(thingName)) {
            ThingDescription thingDescription = thingMap.get(thingName).getThingDescription();
            if ((thingDescription.isThingArrayProperty(interactionName)
                    && thingDescription.isWritableProperty(interactionName)) ||
                    thingDescription.isObservableAction(interactionName)) {
                eventBus.send(ThingAddress.getPutThingInteractionExtraDataAddress(thingName, interactionName), message, sendMessage -> {
                    if (sendMessage.succeeded()) {
                        routingContext.response()
                                .putHeader(HttpHeader.HEADER_CONTENT_TYPE, HttpHeader.HEADER_CONTENT_TYPE_JSON)
                                .end(Json.encodePrettily((JsonObject) sendMessage.result().body()));
                    } else {
                        routingContext.fail(sendMessage.cause());
                    }
                });
            } else {
                routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
            }
        } else {
            routingContext.fail(HttpResponseStatus.RESOURCE_NOT_FOUND);
        }

    }

    private void recoverThingDescription(String thingName, Handler<AsyncResult<ThingDescription>> handler) {
        eventBus.send(ThingAddress.getGetThingThingDescriptionAddress(thingName), null, sendMessage -> {
            if (sendMessage.succeeded()) {
                handler.handle(Future.succeededFuture(new ThingDescription((JsonObject) sendMessage.result().body())));
            } else {
                handler.handle(Future.failedFuture(sendMessage.cause()));
            }
        });
    }

}
