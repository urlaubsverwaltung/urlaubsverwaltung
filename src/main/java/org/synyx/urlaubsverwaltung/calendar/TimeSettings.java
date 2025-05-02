package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TimeSettings implements Serializable {

    @Column(name = "timezoneid")
    private String timeZoneId = "Europe/Berlin";

    @Column(name = "workDayBeginHour")
    private Integer workDayBeginHour = 8;

    @Column(name = "workDayBeginMinute")
    private Integer workDayBeginMinute = 0;

    @Column(name = "workDayEndHour")
    private Integer workDayEndHour = 16;

    @Column(name = "workDayEndMinute")
    private Integer workDayEndMinute = 0;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public Integer getWorkDayBeginHour() {
        return workDayBeginHour;
    }

    public void setWorkDayBeginHour(Integer workDayBeginHour) {
        this.workDayBeginHour = workDayBeginHour;
    }

    public Integer getWorkDayBeginMinute() {
        return workDayBeginMinute;
    }

    public void setWorkDayBeginMinute(Integer workDayBeginMinute) {
        this.workDayBeginMinute = workDayBeginMinute;
    }

    public Integer getWorkDayEndHour() {
        return workDayEndHour;
    }

    public void setWorkDayEndHour(Integer workDayEndHour) {
        this.workDayEndHour = workDayEndHour;
    }

    public Integer getWorkDayEndMinute() {
        return workDayEndMinute;
    }

    public void setWorkDayEndMinute(Integer workDayEndMinute) {
        this.workDayEndMinute = workDayEndMinute;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TimeSettings that = (TimeSettings) o;
        return Objects.equals(timeZoneId, that.timeZoneId)
            && Objects.equals(workDayBeginHour, that.workDayBeginHour)
            && Objects.equals(workDayBeginMinute, that.workDayBeginMinute)
            && Objects.equals(workDayEndHour, that.workDayEndHour)
            && Objects.equals(workDayEndMinute, that.workDayEndMinute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeZoneId, workDayBeginHour, workDayBeginMinute, workDayEndHour, workDayEndMinute);
    }
}
