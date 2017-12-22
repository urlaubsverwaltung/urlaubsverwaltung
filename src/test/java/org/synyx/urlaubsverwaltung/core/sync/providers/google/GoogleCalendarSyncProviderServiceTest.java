package org.synyx.urlaubsverwaltung.core.sync.providers.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.EventType;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProvider.APPLICATION_NAME;
import static org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProvider.GOOGLEAPIS_OAUTH2_V4_TOKEN;

public class GoogleCalendarSyncProviderServiceTest {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String CALENDAR_ID;
    private static String REFRESH_TOKEN;

    private SettingsService settingsService;
    private MailService mailService;
    private GoogleCalendarSyncProvider googleCalendarSyncProvider;

    @BeforeClass
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

    @Before
    public void setUp() throws Exception {
        settingsService = prepareSettingsServiceMock();
        mailService = mock(MailService.class);
        googleCalendarSyncProvider = new GoogleCalendarSyncProvider(mailService, settingsService);
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
    public void testCalendarEventCount() throws GeneralSecurityException, IOException {
        Integer eventCount = getCalendarEventCount();
        Assert.assertTrue(eventCount >= 0);
    }


    @Test
    public void init() {
        googleCalendarSyncProvider = new GoogleCalendarSyncProvider(mailService, settingsService);
    }

    @Test
    public void addUpdateDeleteAbsence() throws GeneralSecurityException, IOException {

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();

        Person person = new Person("testUser", "Hans", "Wurst", "testUser@mail.test");
        Period period = new Period(DateMidnight.now(), DateMidnight.now(), DayLength.MORNING);

        AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(Mockito.mock(CalendarSettings.class));
        Absence absence = new Absence(person, period, EventType.WAITING_APPLICATION, config);

        int eventsBeforeAdd = getCalendarEventCount();
        String eventId = googleCalendarSyncProvider.add(absence, calendarSettings).get();
        int eventsAfterAdd = getCalendarEventCount();

        Absence absenceUpdate = new Absence(person, period, EventType.SICKNOTE, config);
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
        SettingsService settingsService = Mockito.mock(SettingsService.class);
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
    }}
