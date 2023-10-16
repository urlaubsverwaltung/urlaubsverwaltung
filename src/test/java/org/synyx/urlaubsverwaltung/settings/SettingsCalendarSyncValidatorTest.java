package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SettingsCalendarSyncValidatorTest {

    private SettingsCalendarSyncValidator sut;

    @BeforeEach
    void setUp() {
        sut = new SettingsCalendarSyncValidator();
    }

    @Test
    void ensureSettingsClassIsSupported() {
        assertThat(sut.supports(SettingsCalendarSyncDto.class)).isTrue();
    }

    @Test
    void ensureOtherClassThanSettingsIsNotSupported() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    // Exchange calendar settings --------------------------------------------------------------------------------------
    @Test
    void ensureExchangeCalendarSettingsAreNotMandatoryIfDeactivated() {

        final ExchangeCalendarSettings exchangeCalendarSettings = new ExchangeCalendarSettings();
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setExchangeCalendarSettings(exchangeCalendarSettings);

        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setCalendarSettings(calendarSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verifyNoInteractions(mockError);
    }

    @Test
    void ensureExchangeCalendarSettingsAreMandatory() {

        final ExchangeCalendarSettings exchangeCalendarSettings = new ExchangeCalendarSettings();
        exchangeCalendarSettings.setEmail(null);
        exchangeCalendarSettings.setPassword(null);
        exchangeCalendarSettings.setCalendar(null);

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setExchangeCalendarSettings(exchangeCalendarSettings);
        calendarSettings.setProvider(ExchangeCalendarProvider.class.getSimpleName());

        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setCalendarSettings(calendarSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.password", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.exchangeCalendarSettings.calendar", "error.entry.mandatory");
    }

    @Test
    void ensureExchangeCalendarEmailMustHaveValidFormat() {

        final ExchangeCalendarSettings exchangeCalendarSettings = new ExchangeCalendarSettings();
        exchangeCalendarSettings.setEmail("synyx");
        exchangeCalendarSettings.setPassword("top-secret");
        exchangeCalendarSettings.setCalendar("Urlaub");

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setExchangeCalendarSettings(exchangeCalendarSettings);
        calendarSettings.setProvider(ExchangeCalendarProvider.class.getSimpleName());

        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setCalendarSettings(calendarSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError).rejectValue("calendarSettings.exchangeCalendarSettings.email", "error.entry.mail");
    }

    @Test
    void ensureGoogleCalendarSettingsAreMandatory() {

        final GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        googleCalendarSettings.setCalendarId(null);
        googleCalendarSettings.setClientId(null);
        googleCalendarSettings.setClientSecret(null);

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setGoogleCalendarSettings(googleCalendarSettings);
        calendarSettings.setProvider(GoogleCalendarSyncProvider.class.getSimpleName());

        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setCalendarSettings(calendarSettings);

        final Errors mockError = mock(Errors.class);

        sut.validate(dto, mockError);

        verify(mockError)
            .rejectValue("calendarSettings.googleCalendarSettings.calendarId", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.googleCalendarSettings.clientId", "error.entry.mandatory");
        verify(mockError)
            .rejectValue("calendarSettings.googleCalendarSettings.clientSecret", "error.entry.mandatory");
    }
}
