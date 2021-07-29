package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.TimeZone;

import static java.util.Optional.ofNullable;

public class CalendarSettingsDto {

    public Long id;

    private GoogleCalendarSettingsDto googleCalendarSettings;

    private ExchangeCalendarSettingsDto exchangeCalendarSettings;

    private String provider;

    @JsonIgnore
    private List<String> providers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExchangeCalendarSettingsDto getExchangeCalendarSettings() {
        return exchangeCalendarSettings;
    }

    public void setExchangeCalendarSettings(ExchangeCalendarSettingsDto exchangeCalendarSettings) {
        this.exchangeCalendarSettings = exchangeCalendarSettings;
    }

    public GoogleCalendarSettingsDto getGoogleCalendarSettings() {
        return googleCalendarSettings;
    }

    public void setGoogleCalendarSettings(GoogleCalendarSettingsDto googleCalendarSettings) {
        this.googleCalendarSettings = googleCalendarSettings;
    }


    public String getProvider() {
        return ofNullable(provider).orElse("NoopCalendarSyncProvider");
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @JsonIgnore
    public List<String> getAvailableTimezones() {
        return List.of(TimeZone.getAvailableIDs());
    }
}
