package info.ciclope.wotgate.thing.weatherstation.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.InstantSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

public class Status {
    private double temperature;

    private double pressure;

    private double humidity;

    private double rainFall;

    private double windSpeed;

    private String windDirection;

    private boolean active;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    public Status() {
        this.active = false;
        this.windDirection = "";
        this.timestamp = Instant.now();
    }

    public Status(JsonObject parentObject) {
        JsonObject object = parentObject.getJsonObject("Estacion");

        this.temperature = object.getDouble("Temperatura");
        this.pressure = object.getDouble("Presion");
        this.humidity = object.getDouble("Humedad");
        this.rainFall = object.getDouble("Precipitacion");
        this.windSpeed = object.getDouble("Velocidad viento");
        this.windDirection = object.getString("Direccion viento");
        this.active = true;
        this.timestamp = Instant.now();
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getRainFall() {
        return rainFall;
    }

    public void setRainFall(double rainFall) {
        this.rainFall = rainFall;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
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
