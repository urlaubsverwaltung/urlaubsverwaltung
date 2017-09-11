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

public class GoogleCalendarSyncProviderServiceTest {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String CALENDAR_ID;
    private static String REFRESH_TOKEN;

    @BeforeClass
    public static void setUp() throws IOException {
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

    private GoogleCalendarSyncProviderService cut;

    public static HttpResponse executeGet(HttpTransport transport, JsonFactory jsonFactory, String accessToken, GenericUrl url)
            throws IOException {
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
        HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
        return requestFactory.buildGetRequest(url).execute();
    }

    private static Credential createCredentialWithRefreshToken(
            HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                transport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(
                        new GenericUrl("https://www.googleapis.com/oauth2/v4/token"))
                .setClientAuthentication(new BasicAuthentication(
                        CLIENT_ID,
                        CLIENT_SECRET))
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    @Test
    public void createCredentialWithRefreshToken() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v4/token");

        //executeGet(httpTransport, jsonFactory, refreshToken, url);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(REFRESH_TOKEN);
        createCredentialWithRefreshToken(httpTransport, jsonFactory, tokenResponse);
    }

    private int getCalendarEventCount() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(REFRESH_TOKEN);
        Credential credential = createCredentialWithRefreshToken(httpTransport, jsonFactory, tokenResponse);

        String APPLICATION_NAME = "testing";

        Calendar calendar = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();

        Calendar.Events.List events = calendar.events().list(CALENDAR_ID);

        return events.execute().getItems().size();
    }

    @Test
    public void testCalendarEventCount() throws GeneralSecurityException, IOException {
        int events = getCalendarEventCount();

        Assert.assertTrue(events >= 0);
    }


    @Test
    public void init() {
        MailService mailService = Mockito.mock(MailService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Settings set = Mockito.mock(Settings.class);
        CalendarSettings cSet = Mockito.mock(CalendarSettings.class);
        GoogleCalendarSettings gcSet = Mockito.mock(GoogleCalendarSettings.class);
        Mockito.when(settingsService.getSettings()).thenReturn(set);
        Mockito.when(set.getCalendarSettings()).thenReturn(cSet);
        Mockito.when(cSet.getGoogleCalendarSettings()).thenReturn(gcSet);
        Mockito.when(gcSet.getCalendarId()).thenReturn("calenderId");

        cut = new GoogleCalendarSyncProviderService(mailService, settingsService);
    }

    @Test
    public void addUpdateDeleteAbsence() throws GeneralSecurityException, IOException {
        MailService mailService = Mockito.mock(MailService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Settings set = Mockito.mock(Settings.class);
        CalendarSettings cSet = Mockito.mock(CalendarSettings.class);
        GoogleCalendarSettings gcSet = Mockito.mock(GoogleCalendarSettings.class);
        Mockito.when(settingsService.getSettings()).thenReturn(set);
        Mockito.when(set.getCalendarSettings()).thenReturn(cSet);
        Mockito.when(cSet.getGoogleCalendarSettings()).thenReturn(gcSet);

        Mockito.when(gcSet.getClientId()).thenReturn(CLIENT_ID);
        Mockito.when(gcSet.getClientSecret()).thenReturn(CLIENT_SECRET);
        Mockito.when(gcSet.getCalendarId()).thenReturn(CALENDAR_ID);
        Mockito.when(gcSet.getRefreshToken()).thenReturn(REFRESH_TOKEN);

        cut = new GoogleCalendarSyncProviderService(mailService, settingsService);
        Person person = new Person();
        Period period = new Period(new DateMidnight(0), new DateMidnight(1), DayLength.MORNING);

        CalendarSettings cSet2 = Mockito.mock(CalendarSettings.class);
        AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(cSet2);
        Absence absence = new Absence(person, period, EventType.WAITING_APPLICATION, config);

        int eventsBeforeAdd = getCalendarEventCount();
        String eventId = cut.add(absence, cSet).get();
        int eventsAfterAdd = getCalendarEventCount();

        Absence absenceUpdate = new Absence(person, period, EventType.SICKNOTE, config);
        cut.update(absenceUpdate, eventId, cSet);
        int eventsAfterUpdate = getCalendarEventCount();

        cut.delete(eventId, cSet);
        int eventsAfterDelete = getCalendarEventCount();

        assertTrue(!eventId.isEmpty());
        assertEquals(eventsBeforeAdd + 1, eventsAfterAdd);
        assertEquals(eventsAfterAdd, eventsAfterUpdate);
        assertEquals(eventsAfterDelete, eventsBeforeAdd);
    }


}
