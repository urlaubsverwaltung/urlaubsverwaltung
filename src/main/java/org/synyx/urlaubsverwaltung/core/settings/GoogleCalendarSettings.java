package org.synyx.urlaubsverwaltung.core.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a Google calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class GoogleCalendarSettings {

    @Column(name = "calendar_google_active")
    private boolean active = false;

    @Column(name = "calendar_google_calendarId")
    private String calendarId;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }
}