package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class GoogleCalendarSettings {

    @Column(name = "google_client_id")
    private String clientId = null;

    @Convert(converter = EncryptedSecretConverter.class)
    @Column(name = "google_client_secret")
    private String clientSecret = null;

    @Column(name = "google_calendar_id")
    private String calendarId = null;

    @Convert(converter = EncryptedSecretConverter.class)
    @Column(name = "google_refresh_token")
    private String refreshToken = null;

    @Convert(converter = EncryptedSecretConverter.class)
    @Column(name = "google_access_token")
    private String accessToken = null;

    @Column(name = "google_access_token_expiration")
    private Long accessTokenExpirationMillis = null;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getAccessTokenExpirationMillis() {
        return accessTokenExpirationMillis;
    }

    public void setAccessTokenExpirationMillis(Long accessTokenExpirationMillis) {
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
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

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        // the access token is deliberately not part of equals/hashCode -
        // equality is used to detect configuration changes that invalidate the oauth tokens
        GoogleCalendarSettings that = (GoogleCalendarSettings) o;
        return Objects.equals(getClientId(), that.getClientId()) &&
            Objects.equals(getClientSecret(), that.getClientSecret()) &&
            Objects.equals(getCalendarId(), that.getCalendarId()) &&
            Objects.equals(getRefreshToken(), that.getRefreshToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClientId(), getClientSecret(), getCalendarId(), getRefreshToken());
    }
}
