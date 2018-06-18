package info.ciclope.wotgate.thing;

import info.ciclope.wotgate.ErrorCode;
import io.vertx.core.json.JsonObject;

import java.security.InvalidParameterException;

public class ThingConfiguration {
    private static final String THING_NAME = "name";

    private final JsonObject configuration;

    public ThingConfiguration(String thingName) {
        if (thingName == null || thingName.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.ERROR_THING_CONFIGURATION);
        }

        configuration = new JsonObject();
        configuration.put(THING_NAME, thingName);
    }

    public ThingConfiguration(JsonObject configuration) {
        String thingName = configuration.getString(THING_NAME);

        if (thingName == null || thingName.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.ERROR_THING_CONFIGURATION);
        }

        this.configuration = configuration.copy();
    }

    public JsonObject asJsonObject() {
        return configuration;
    }

    public String getThingName() {
        return configuration.getString(THING_NAME);
    }
}
