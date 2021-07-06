package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "calendar_settings")
public class CalendarSettingsEntity {

    @Id
    @GeneratedValue
    public Long id;

    private GoogleCalendarSettings googleCalendarSettings;

    private ExchangeCalendarSettings exchangeCalendarSettings;

    @Column(name = "calendar_provider")
    private String provider = "NoopCalendarSyncProvider";

    public ExchangeCalendarSettings getExchangeCalendarSettings() {

        if (exchangeCalendarSettings == null) {
            exchangeCalendarSettings = new ExchangeCalendarSettings();
        }

        return exchangeCalendarSettings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
