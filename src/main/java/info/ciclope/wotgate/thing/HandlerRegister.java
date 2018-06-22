package info.ciclope.wotgate.thing;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class HandlerRegister {
    private String thingName;
    private Map<String, Handler<Message<JsonObject>>> handlerMap;
    private EventBus eventBus;

    public HandlerRegister(EventBus eventBus, String thingName) {
        this.handlerMap = new HashMap<>();
        this.thingName = thingName;
        this.eventBus = eventBus;
    }

    public void addHandler(String action, Handler<Message<JsonObject>> handler) {
        handlerMap.put(action, handler);
    }

    public void removeHandler(String action) {
        handlerMap.remove(action);
    }

    public Handler<Message<JsonObject>> getHandler(String action) {
        return handlerMap.get(action);
    }

    public boolean containsAction(String action) {
        return handlerMap.containsKey(action);
    }

    public void register() {
        handlerMap.forEach((action, handler) -> eventBus.consumer(thingName + action, handler));
    }
}
