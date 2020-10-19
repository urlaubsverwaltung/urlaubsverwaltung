package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static java.util.Optional.ofNullable;

@Embeddable
public class TimeSettings {

    @Column(name = "timezoneid")
    private String timeZoneId;

    @Column(name = "workDayBeginHour")
    private Integer workDayBeginHour = 8; // NOSONAR

    @Column(name = "workDayEndHour")
    private Integer workDayEndHour = 16; // NOSONAR

    public String getTimeZoneId() {

        return ofNullable(timeZoneId).orElse("Europe/Berlin");
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

    public Integer getWorkDayEndHour() {
        return workDayEndHour;
    }

    public void setWorkDayEndHour(Integer workDayEndHour) {
        this.workDayEndHour = workDayEndHour;
    }
}
