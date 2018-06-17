package info.ciclope.wotgate.thing.dome.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.InstantSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Status {
    private double azimuth;

    private double supply_voltage;

    private String current_action;

    private String shutter;

    private boolean active;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    public Status() {
        this.active = false;
        this.current_action = "";
        this.shutter = "";
        this.timestamp = Instant.now();
    }

    public Status(JsonObject object) {
        this.azimuth = object.getDouble("azimuth");
        this.supply_voltage = object.getDouble("supply_voltage");
        this.current_action = object.getString("current_action");
        this.shutter = object.getString("shutter");
        this.active = true;
        this.timestamp = Instant.now();
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getSupply_voltage() {
        return supply_voltage;
    }

    public void setSupply_voltage(double supply_voltage) {
        this.supply_voltage = supply_voltage;
    }

    public String getCurrent_action() {
        return current_action;
    }

    public void setCurrent_action(String current_action) {
        this.current_action = current_action;
    }

    public String getShutter() {
        return shutter;
    }

    public void setShutter(String shutter) {
        this.shutter = shutter;
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
