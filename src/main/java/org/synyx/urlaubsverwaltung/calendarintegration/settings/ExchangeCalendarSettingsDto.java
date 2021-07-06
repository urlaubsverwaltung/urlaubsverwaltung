package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import java.util.Optional;

public class ExchangeCalendarSettingsDto {

    private static final String DEFAULT_TIMEZONE = "UTC";
    private String email;
    private String password;
    private String calendar;
    private boolean sendInvitationActive;
    private String ewsUrl;
    private String timeZoneId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public boolean isSendInvitationActive() {
        return sendInvitationActive;
    }

    public void setSendInvitationActive(boolean sendInvitationActive) {
        this.sendInvitationActive = sendInvitationActive;
    }

    public String getEwsUrl() {
        return ewsUrl;
    }

    public void setEwsUrl(String ewsUrl) {
        this.ewsUrl = ewsUrl;
    }

    public String getTimeZoneId() {
        return Optional.ofNullable(timeZoneId).orElse(DEFAULT_TIMEZONE);
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
