package org.synyx.urlaubsverwaltung.calendar;

import static org.synyx.urlaubsverwaltung.calendar.CalendarPeriodViewType.HALVE_YEAR;

public class CompanyCalendarDto {

    private int personId;
    private String calendarUrl;
    private CalendarPeriodViewType calendarPeriod = HALVE_YEAR;

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
