package org.synyx.urlaubsverwaltung.core.settings;

import java.util.Base64;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a Microsoft Exchange Calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class ExchangeCalendarSettings {

    @Column(name = "calendar_ews_active")
    private boolean active = false;

    @Column(name = "calendar_ews_domain")
    private String domain;

    @Column(name = "calendar_ews_username")
    private String username;

    @Column(name = "calendar_ews_password")
    private String password;

    @Column(name = "calendar_ews_calendar")
    private String calendar = "Urlaubsverwaltung";

    @Column(name = "calendar_ews_sendInvitationActive")
    private boolean sendInvitationActive = false;

    public boolean isActive() {

        return active;
    }


    public void setActive(boolean active) {

        this.active = active;
    }


    public String getDomain() {

        return domain;
    }


    public void setDomain(String domain) {

        this.domain = domain;
    }


    public String getUsername() {

        return username;
    }


    public void setUsername(String username) {

        this.username = username;
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
}
