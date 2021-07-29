package org.synyx.urlaubsverwaltung.absence.settings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "time_settings")
public class TimeSettingsEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "timezone_id")
    private String timeZoneId = "Europe/Berlin";

    @Column(name = "work_day_begin_hour")
    private Integer workDayBeginHour = 8;

    @Column(name = "work_day_end_hour")
    private Integer workDayEndHour = 16;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getWorkDayEndHour() {
        return workDayEndHour;
    }

    public void setWorkDayEndHour(Integer workDayEndHour) {
        this.workDayEndHour = workDayEndHour;
    }
}
