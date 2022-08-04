package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

@Validated
public class PersonCalendarDto {

    @NotNull
    private int personId;
    @Size(min = 1)
    private String calendarUrl;
    @NotNull
    private CalendarPeriodViewType calendarPeriod = HALF_YEAR;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getCalendarUrl() {
        return calendarUrl;
    }

    public void setCalendarUrl(String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }

    public CalendarPeriodViewType getCalendarPeriod() {
        return calendarPeriod;
    }

    public void setCalendarPeriod(CalendarPeriodViewType calendarPeriod) {
        this.calendarPeriod = calendarPeriod;
    }
}
