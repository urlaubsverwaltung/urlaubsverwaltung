package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Base64;


/**
 * Settings to sync absences with a Microsoft Exchange calendar.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Embeddable
public class ExchangeCalendarSettings {

    @Column(name = "calendar_ews_email")
    private String email;

    @Column(name = "calendar_ews_password")
    private String password;

    @Column(name = "calendar_ews_calendar")
    private String calendar = "";

    @Column(name = "calendar_ews_sendInvitationActive")
    private boolean sendInvitationActive = false;

    @Column(name = "calendar_ews_url")
    private String ewsUrl;

    @Column(name = "calendar_ews_timezoneid")
    private String timeZoneId;


    public String getEmail() {

        return email;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public String getPassword() {

        if (password == null) {
            return null;
        }

        return new String(Base64.getDecoder().decode(password));
    }


    public void setPassword(String password) {

        if (password == null) {
            this.password = null;
        } else {
            this.password = Base64.getEncoder().encodeToString(password.getBytes());
        }
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

        if (ewsUrl == null || ewsUrl.isEmpty()) {
            return null;
        }

        return ewsUrl;
    }


    public void setEwsUrl(String ewsUrl) {

        if (ewsUrl == null || ewsUrl.isEmpty()) {
            this.ewsUrl = null;
        } else {
            this.ewsUrl = ewsUrl;
        }
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
