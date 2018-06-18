package info.ciclope.wotgate.thing.camera.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.InstantSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Status {
    private double brightness;

    private double gamma;

    private double exposure;

    private boolean active;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    public Status() {
        this.active = false;
        this.timestamp = Instant.now();
    }

    public Status(JsonObject object) {
        this.brightness = object.getDouble("brightness");
        this.gamma = object.getDouble("gamma");
        this.exposure = object.getDouble("exposure");
        this.active = true;
        this.timestamp = Instant.now();
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
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
