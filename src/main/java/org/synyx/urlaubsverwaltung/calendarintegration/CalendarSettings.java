package org.synyx.urlaubsverwaltung.calendarintegration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

import static jakarta.persistence.GenerationType.SEQUENCE;


@Entity(name = "calendar_integration_settings")
public class CalendarSettings {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "calendar_settings_generator")
    @SequenceGenerator(name = "calendar_settings_generator", sequenceName = "calendar_settings_id_seq", allocationSize = 1)
    private Long id;

    private GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();

    private ExchangeCalendarSettings exchangeCalendarSettings = new ExchangeCalendarSettings();

    @Column(name = "provider")
    private String provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExchangeCalendarSettings getExchangeCalendarSettings() {
        return exchangeCalendarSettings;
    }

    public void setExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings) {
        this.exchangeCalendarSettings = exchangeCalendarSettings;
    }

    public GoogleCalendarSettings getGoogleCalendarSettings() {
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
