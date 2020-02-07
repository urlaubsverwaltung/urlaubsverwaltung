package org.synyx.urlaubsverwaltung.calendarintegration.providers.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarMailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Properties;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleCalendarSyncProviderServiceTest {

    private static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final String GOOGLEAPIS_OAUTH2_V4_TOKEN = "https://www.googleapis.com/oauth2/v4/token";

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String CALENDAR_ID;
    private static String REFRESH_TOKEN;

    private SettingsService settingsService;
    private CalendarMailService calendarMailService;
    private GoogleCalendarSyncProvider googleCalendarSyncProvider;

    @BeforeAll
    public static void setUpProperties() throws IOException {
        Properties prop = new Properties();
        File file = new File("src/test/resources/google_oauth.properties");

        Assume.assumeTrue("file should exist (do not store in git): " + file.getAbsolutePath(), file.isFile());

        prop.load(new FileInputStream(file));
        CLIENT_ID = prop.getProperty("clientId");
        CLIENT_SECRET = prop.getProperty("clientSecret");
        CALENDAR_ID = prop.getProperty("calendarId");
        REFRESH_TOKEN = prop.getProperty("refreshToken");

        Assume.assumeTrue("clientId for testing should be defined", CLIENT_ID != null);
        Assume.assumeTrue("clientSecret for testing should be defined", CLIENT_SECRET != null);
        Assume.assumeTrue("calendarId for testing should be defined", CALENDAR_ID != null);
        Assume.assumeTrue("refreshToken for testing should be defined", REFRESH_TOKEN != null);
    }

    @BeforeEach
    void setUp() {
        settingsService = prepareSettingsServiceMock();
        calendarMailService = mock(CalendarMailService.class);
        googleCalendarSyncProvider = new GoogleCalendarSyncProvider(calendarMailService, settingsService);
    }

    private static Credential createCredentialWithRefreshToken(
        HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
            transport)
            .setJsonFactory(jsonFactory)
            .setTokenServerUrl(
                new GenericUrl(GOOGLEAPIS_OAUTH2_V4_TOKEN))
            .setClientAuthentication(new BasicAuthentication(
                CLIENT_ID,
                CLIENT_SECRET))
            .build()
            .setFromTokenResponse(tokenResponse);
    }

    private Integer getCalendarEventCount() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(REFRESH_TOKEN);
        Credential credential = createCredentialWithRefreshToken(httpTransport, jsonFactory, tokenResponse);

        Calendar calendar = new com.google.api.services.calendar.Calendar.Builder(
            httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();

        Calendar.Events.List events = calendar.events().list(CALENDAR_ID);

        return events.execute().getItems().size();
    }

    @Test
    void testCalendarEventCount() throws GeneralSecurityException, IOException {
        Integer eventCount = getCalendarEventCount();
        Assert.assertTrue(eventCount >= 0);
    }


    @Test
    void init() {
        googleCalendarSyncProvider = new GoogleCalendarSyncProvider(calendarMailService, settingsService);
    }

    @Test
    void addUpdateDeleteAbsence() throws GeneralSecurityException, IOException {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();

        Person person = new Person("testUser", "Hans", "Wurst", "testUser@mail.test");
        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.MORNING);

        AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(mock(CalendarSettings.class));
        Absence absence = new Absence(person, period, config, Clock.systemUTC());

        int eventsBeforeAdd = getCalendarEventCount();
        String eventId = googleCalendarSyncProvider.add(absence, calendarSettings).get();
        int eventsAfterAdd = getCalendarEventCount();

        Absence absenceUpdate = new Absence(person, period, config, Clock.systemUTC());
        googleCalendarSyncProvider.update(absenceUpdate, eventId, calendarSettings);
        int eventsAfterUpdate = getCalendarEventCount();

        googleCalendarSyncProvider.delete(eventId, calendarSettings);
        int eventsAfterDelete = getCalendarEventCount();

        assertTrue(!eventId.isEmpty());
        assertEquals(eventsBeforeAdd + 1, eventsAfterAdd);
        assertEquals(eventsAfterAdd, eventsAfterUpdate);
        assertEquals(eventsAfterDelete, eventsBeforeAdd);
    }

    private SettingsService prepareSettingsServiceMock() {
        SettingsService settingsService = mock(SettingsService.class);
        Settings settings = new Settings();
        CalendarSettings calendarSettings = new CalendarSettings();
        GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        googleCalendarSettings.setCalendarId(CALENDAR_ID);
        googleCalendarSettings.setClientId(CLIENT_ID);
        googleCalendarSettings.setClientSecret(CLIENT_SECRET);
        googleCalendarSettings.setRefreshToken(REFRESH_TOKEN);
        calendarSettings.setGoogleCalendarSettings(googleCalendarSettings);
        settings.setCalendarSettings(calendarSettings);
        when(settingsService.getSettings()).thenReturn(settings);
        return settingsService;
    }
}
