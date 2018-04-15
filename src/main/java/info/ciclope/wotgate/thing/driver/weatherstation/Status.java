package info.ciclope.wotgate.thing.driver.weatherstation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Status {

    @JsonProperty("Estacion")
    private Estacion estacion;


    public Status() {
        this.estacion = new Estacion();
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public void setEstacion(Estacion estacion) {
        this.estacion = estacion;
    }


    public class Estacion {

        @JsonProperty("Temperatura")
        private double temperature;

        @JsonProperty("Presion")
        private double pressure;

        @JsonProperty("Humedad")
        private double humidity;

        @JsonProperty("Precipitacion")
        private double rainFall;

        @JsonProperty("Velocidad viento")
        private double windSpeed;

        @JsonProperty("Direccion viento")
        private String windDirection;

        @JsonProperty("Estado")
        private String status;

        @JsonProperty("Hora")
        private String time;

        @JsonProperty("Fecha")
        private String date;

        private Instant timeStamp;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getTimeStamp() {
            // Convert date and time to local instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

            if (date != null && time != null) {
                timeStamp = LocalDateTime.parse(date + " " + time, formatter).atZone(ZoneId.of("Europe/Madrid")).toInstant();
            } else {
                timeStamp = Instant.now();
            }

            return timeStamp;
        }

        public void setTimeStamp(Instant timeStamp) {
            this.timeStamp = timeStamp;
        }

        @JsonIgnore
        public String getTime() {
            return time;
        }

        @JsonSetter
        public void setTime(String time) {
            this.time = time;
        }

        @JsonIgnore
        public String getDate() {
            return date;
        }

        @JsonSetter
        public void setDate(String date) {
            this.date = date;
        }

        public JsonObject asJson() {
            return JsonObject.mapFrom(this);
        }
    }

}
