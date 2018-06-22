package info.ciclope.wotgate.thing.dome.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.InstantSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Status {
    private double azimuth;

    private double supplyVoltage;

    private String currentAction;

    private String shutter;

    private boolean active;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    public Status() {
        this.active = false;
        this.currentAction = "";
        this.shutter = "";
        this.timestamp = Instant.now();
    }

    public Status(JsonObject object) {
        this.azimuth = object.getDouble("azimuth");
        this.supplyVoltage = object.getDouble("supply_voltage");
        this.currentAction = object.getString("current_action");
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

    public double getSupplyVoltage() {
        return supplyVoltage;
    }

    public void setSupplyVoltage(double supplyVoltage) {
        this.supplyVoltage = supplyVoltage;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(String currentAction) {
        this.currentAction = currentAction;
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
