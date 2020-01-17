package org.synyx.urlaubsverwaltung.calendar.web;

public class PrivateCalendarShareDto {

    private Integer personId;
    private String calendarUrl;

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getCalendarUrl() {
        return calendarUrl;
    }

    public void setCalendarUrl(String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }
}
