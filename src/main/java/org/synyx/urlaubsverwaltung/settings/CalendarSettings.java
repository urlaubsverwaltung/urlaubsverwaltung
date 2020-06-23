package org.synyx.urlaubsverwaltung.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a calendar provider.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Embeddable
public class CalendarSettings {

    private GoogleCalendarSettings googleCalendarSettings;

    private ExchangeCalendarSettings exchangeCalendarSettings;

    @Column(name = "calendar_workDayBeginHour")
    private Integer workDayBeginHour = 8; // NOSONAR

    @Column(name = "calendar_workDayEndHour")
    private Integer workDayEndHour = 16; // NOSONAR

    @Column(name = "calendar_provider")
    private String provider = "NoopCalendarSyncProvider";

    public ExchangeCalendarSettings getExchangeCalendarSettings() {

        if (exchangeCalendarSettings == null) {
            exchangeCalendarSettings = new ExchangeCalendarSettings();
        }

        return exchangeCalendarSettings;
    }


    public void setExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings) {

        this.exchangeCalendarSettings = exchangeCalendarSettings;
    }


    public GoogleCalendarSettings getGoogleCalendarSettings() {

        if (googleCalendarSettings == null) {
            googleCalendarSettings = new GoogleCalendarSettings();
        }

        return googleCalendarSettings;
    }


    public void setGoogleCalendarSettings(GoogleCalendarSettings googleCalendarSettings) {

        this.googleCalendarSettings = googleCalendarSettings;
    }


    public Integer getWorkDayBeginHour() {

        return workDayBeginHour;
    }


    public void setWorkDayBeginHour(Integer workDayBeginHour) {

        this.workDayBeginHour = workDayBeginHour;
    }


    public Integer getWorkDayEndHour() {

        return workDayEndHour;
    }


    public void setWorkDayEndHour(Integer workDayEndHour) {

        this.workDayEndHour = workDayEndHour;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
