package org.synyx.urlaubsverwaltung.calendar;

public class PersonCalendarDto {

    private int personId;
    private String calendarUrl;
    private CalendarPeriodViewType calendarPeriod;

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
