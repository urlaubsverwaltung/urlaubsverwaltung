package org.synyx.urlaubsverwaltung.calendar;

public class PersonCalendarDto {

    private int personId;
    private String calendarUrl;
    private int fetchSince;

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

    public int getFetchSince() {
        return fetchSince;
    }

    public void setFetchSince(int fetchSince) {
        this.fetchSince = fetchSince;
    }
}
