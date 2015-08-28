package org.synyx.urlaubsverwaltung.core.settings;

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
}
