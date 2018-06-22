package info.ciclope.wotgate.thing.mount.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.InstantSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Status {
    private String rightAscension;

    private String declination;

    private String currentAction;

    private boolean active;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    public Status() {
        this.active = false;
        this.rightAscension = "";
        this.declination = "";
        this.currentAction = "";
        this.timestamp = Instant.now();
    }

    public Status(JsonObject object) {
        this.rightAscension = object.getString("rightAscension");
        this.declination = object.getString("declination");
        this.currentAction = object.getString("current_action");
        this.active = true;
        this.timestamp = Instant.now();
    }

    public String getRightAscension() {
        return rightAscension;
    }

    public void setRightAscension(String rightAscension) {
        this.rightAscension = rightAscension;
    }

    public String getDeclination() {
        return declination;
    }

    public void setDeclination(String declination) {
        this.declination = declination;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(String currentAction) {
        this.currentAction = currentAction;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
