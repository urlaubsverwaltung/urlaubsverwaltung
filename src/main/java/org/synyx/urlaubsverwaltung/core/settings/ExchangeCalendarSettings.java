package org.synyx.urlaubsverwaltung.core.settings;

import java.util.Base64;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a Microsoft Exchange calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class ExchangeCalendarSettings {

    @Column(name = "calendar_ews_active")
    private boolean active = false;

    @Column(name = "calendar_ews_email")
    private String email;

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
}
