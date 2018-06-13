package info.ciclope.wotgate.util;

import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;

import java.time.LocalDateTime;

public class Interval {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Interval(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Interval(Reservation reservation) {
        this.startTime = reservation.getStartDate();
        this.endTime = reservation.getEndDate();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean contains(Interval interval) {
        return interval.startTime.compareTo(this.startTime) >= 0 && interval.endTime.compareTo(this.endTime) <= 0;
    }

    public boolean overlap(Interval interval) {
        if (this.contains(interval) || interval.contains(this)) {
            return true;
        }

        if (interval.startTime.compareTo(this.startTime) < 0
                && interval.endTime.compareTo(this.startTime) > 0
                && interval.endTime.compareTo(this.endTime) < 0) {
            // Upper overlap
            return true;
        } else if (interval.startTime.compareTo(this.startTime) > 0
                && interval.startTime.compareTo(this.endTime) < 0
                && interval.endTime.compareTo(this.endTime) > 0) {
            // Lower overlap
            return true;
        }

        return false;
    }
}
