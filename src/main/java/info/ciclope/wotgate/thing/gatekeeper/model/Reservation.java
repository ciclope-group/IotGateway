package info.ciclope.wotgate.thing.gatekeeper.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.ciclope.wotgate.util.LocalDateTimeDeserializer;
import info.ciclope.wotgate.util.LocalDateTimeSerializer;
import io.vertx.core.json.JsonObject;

import java.time.Duration;
import java.time.LocalDateTime;

public class Reservation {

    private long id;
    private long userId;
    private int status;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime dateCreated;

    public Reservation() {
    }

    public Reservation(JsonObject object) {
        this.id = object.getLong("id");
        this.userId = object.getLong("user_id");
        this.status = object.getInteger("status_id");

        this.startDate = LocalDateTime.parse(object.getString("startDate"));
        this.endDate = LocalDateTime.parse(object.getString("endDate"));
        this.dateCreated = LocalDateTime.parse(object.getString("dateCreated"));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Validate start date and end date. Start date must be before end date, and both need to be a quarter of hour.
     * Minutes valid are 00, 15, 30, 45. Maximun reservation time is 3 hours.
     */
    public boolean validate() {
        if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
            if (startDate.getMinute() % 15 == 0 && endDate.getMinute() % 15 == 0) {
                if (Duration.between(startDate, endDate).toMinutes() <= 3 * 60) {
                    return true;
                }
            }
        }

        return false;
    }
}
