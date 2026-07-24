package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.services.calendar.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarClientProviderTest {

    @Mock
    private CalendarSettingsService calendarSettingsService;

    private GoogleCalendarClientProvider sut;

    @BeforeEach
    void setUp() {
        sut = new GoogleCalendarClientProvider(calendarSettingsService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void ensureEmptyClientWithoutRefreshToken(String refreshToken) {

        final GoogleCalendarSettings googleCalendarSettings = someGoogleCalendarSettings();
        googleCalendarSettings.setRefreshToken(refreshToken);

        final Optional<Calendar> maybeCalendarClient = sut.getCalendarClient(googleCalendarSettings);

        assertThat(maybeCalendarClient).isEmpty();
        verifyNoInteractions(calendarSettingsService);
    }

    @Test
    void ensureClientWithRefreshToken() {

        final GoogleCalendarSettings googleCalendarSettings = someGoogleCalendarSettings();
        googleCalendarSettings.setRefreshToken("REFRESH_TOKEN");

        final Optional<Calendar> maybeCalendarClient = sut.getCalendarClient(googleCalendarSettings);

        assertThat(maybeCalendarClient).isPresent();
    }

    @Test
    void ensureClientWithRefreshTokenAndPersistedAccessToken() {

        final GoogleCalendarSettings googleCalendarSettings = someGoogleCalendarSettings();
        googleCalendarSettings.setRefreshToken("REFRESH_TOKEN");
        googleCalendarSettings.setAccessToken("ACCESS_TOKEN");
        googleCalendarSettings.setAccessTokenExpirationMillis(4102444800000L);

        final Optional<Calendar> maybeCalendarClient = sut.getCalendarClient(googleCalendarSettings);

        assertThat(maybeCalendarClient).isPresent();
    }

    private static GoogleCalendarSettings someGoogleCalendarSettings() {
        final GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        googleCalendarSettings.setCalendarId("CALENDAR_ID");
        googleCalendarSettings.setClientId("CLIENT_ID");
        googleCalendarSettings.setClientSecret("CLIENT_SECRET");
        return googleCalendarSettings;
    }
}
