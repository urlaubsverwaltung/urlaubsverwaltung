package org.synyx.urlaubsverwaltung.calendar;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALF_YEAR;

@Validated
public class PersonCalendarDto {

    @NotNull
    private long personId;
    @Size(min = 1)
    private String calendarUrl;
    @NotNull
    private CalendarPeriodViewType calendarPeriod = HALF_YEAR;

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
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
