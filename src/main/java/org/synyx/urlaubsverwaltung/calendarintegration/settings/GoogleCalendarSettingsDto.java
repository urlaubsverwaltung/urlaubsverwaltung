package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GoogleCalendarSettingsDto {

    private String clientId;

    private String clientSecret;

    private String calendarId;

    private String refreshToken;

    @JsonIgnore
    private String authorizedRedirectUrl;

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

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAuthorizedRedirectUrl() {
        return authorizedRedirectUrl;
    }

    public void setAuthorizedRedirectUrl(String authorizedRedirectUrl) {
        this.authorizedRedirectUrl = authorizedRedirectUrl;
    }
}
