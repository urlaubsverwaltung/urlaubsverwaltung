package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
