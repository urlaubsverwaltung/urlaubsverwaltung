package org.synyx.urlaubsverwaltung.core.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a Google calendar.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class GoogleCalendarSettings {

    @Column(name = "calendar_google_active")
    private boolean active = false;

    @Column(name = "calendar_google_client_id")
    private String clientId;

    @Column(name = "calendar_google_client_secret")
    private String clientSecret;

    @Column(name = "calendar_google_calendar_id")
    private String calendarId;

    @Column(name = "calendar_google_refresh_token")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

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