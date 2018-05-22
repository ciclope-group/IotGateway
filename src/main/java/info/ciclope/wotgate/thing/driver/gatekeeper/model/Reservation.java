package info.ciclope.wotgate.thing.driver.gatekeeper.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class Reservation {

    private long id;
    private long userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime dateCreated;
    private int status;

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

    public boolean validate() {
        return startDate != null && endDate != null;
    }
}
