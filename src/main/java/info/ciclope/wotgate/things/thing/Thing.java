package info.ciclope.wotgate.things.thing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.ciclope.wotgate.StorageManager.StorageManagerInterface;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URL;

public abstract class Thing {
    private final ThingConfiguration thingConfiguration;
    private JsonObject thingDescription;
    private StorageManagerInterface storageManager;

    public Thing(ThingConfiguration thingConfiguration, StorageManagerInterface storageManager) {
        this.thingConfiguration = thingConfiguration;
        loadThingDescription(getThingDescriptionPath());
        this.storageManager = storageManager;
    }

    public ThingConfiguration getThingConfiguration() {
        return thingConfiguration;
    }

    public JsonObject getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(JsonObject thingDescription) {
        this.thingDescription = thingDescription;
    }

    public abstract String getThingDescriptionPath();

    private void loadThingDescription(String thingDescriptionPath) {
        ObjectMapper objectMapper = new ObjectMapper();
        URL thingDescriptionUrl = getClass().getClassLoader().getResource(thingDescriptionPath);
        try {
            thingDescription = new JsonObject((objectMapper.readValue(thingDescriptionUrl, JsonNode.class)).toString());
        } catch (IOException e) {
            thingDescription = new JsonObject();
            e.printStackTrace();
        }
    }
}
