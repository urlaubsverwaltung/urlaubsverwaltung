package org.synyx.urlaubsverwaltung.absence.settings;

import java.util.TimeZone;

import static java.util.Optional.ofNullable;

public class TimeSettingsDto {

    public static final String DEFAULT_TIMEZONE = "Europe/Berlin";
    private Long id;
    private String timeZoneId;
    private Integer workDayBeginHour;
    private Integer workDayEndHour;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String[] getAvailableTimeZones() {
        return TimeZone.getAvailableIDs();
    }

    public String getTimeZoneId() {
        return ofNullable(timeZoneId).orElse(DEFAULT_TIMEZONE);
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
