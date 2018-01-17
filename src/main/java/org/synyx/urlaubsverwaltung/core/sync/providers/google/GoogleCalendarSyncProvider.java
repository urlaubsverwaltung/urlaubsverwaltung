package org.synyx.urlaubsverwaltung.core.sync.providers.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author Daniel Hammann - hammann@synyx.de
 * @author Marc Sommer - sommer@synyx.de
 */
@Service
public class GoogleCalendarSyncProvider implements CalendarProvider {

    public static final String DATE_PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger LOG = Logger.getLogger(GoogleCalendarSyncProvider.class);

    public static final String APPLICATION_NAME = "Urlaubsverwaltung";
    protected static final String GOOGLEAPIS_OAUTH2_V4_TOKEN = "https://www.googleapis.com/oauth2/v4/token";

    private Calendar googleCalendarClient;
    private int refreshTokenHashCode;
    private final MailService mailService;
    private final SettingsService settingsService;

    @Autowired
    public GoogleCalendarSyncProvider(MailService mailService, SettingsService settingsService) {

        this.settingsService = settingsService;
        this.mailService = mailService;
    }

    /**
     * Build and return an authorized google calendar client.
     *
     * @return an authorized calendar client service
     */
    private com.google.api.services.calendar.Calendar getOrCreateGoogleCalendarClient() {

        String refreshToken =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getRefreshToken();

        if (googleCalendarClient != null &&
                refreshToken != null &&
                refreshTokenHashCode == refreshToken.hashCode()) {
            LOG.debug("use cached googleCalendarClient");
            return googleCalendarClient;
        }
        try {
            LOG.info("create new googleCalendarClient");

            if (refreshToken != null) {
                refreshTokenHashCode = refreshToken.hashCode();
            }

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setRefreshToken(refreshToken);

            Credential credential = createCredentialWithRefreshToken(httpTransport, JSON_FACTORY, tokenResponse);

            return new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        } catch (GeneralSecurityException | IOException e) {
            LOG.error(e);
        }

        return null;
    }


    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        googleCalendarClient = getOrCreateGoogleCalendarClient();

        if (googleCalendarClient != null) {
            GoogleCalendarSettings googleCalendarSettings =
                    settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();
            String calendarId = googleCalendarSettings.getCalendarId();

            try {
                Event eventToCommit = new Event();
                fillEvent(absence, eventToCommit);

                Event eventInCalendar = googleCalendarClient.events().insert(calendarId, eventToCommit).execute();

                LOG.info(String.format("Event %s for '%s' added to calendar '%s'.", eventInCalendar.getId(),
                        absence.getPerson().getNiceName(), eventInCalendar.getSummary()));
                return Optional.of(eventInCalendar.getId());

            } catch (IOException ex) {
                LOG.warn(String.format("An error occurred while trying to add appointment to calendar %s", calendarId), ex);
                mailService.sendCalendarSyncErrorNotification(calendarId, absence, ex.toString());
            }
        }
        return Optional.empty();
    }


    @Override
    public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {

        googleCalendarClient = getOrCreateGoogleCalendarClient();

        if (googleCalendarClient != null) {

            String calendarId =
                    settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getCalendarId();

            try {
                // gather exiting event
                Event event = googleCalendarClient.events().get(calendarId, eventId).execute();

                // update event with absence
                fillEvent(absence, event);

                // sync event to calendar
                googleCalendarClient.events().patch(calendarId, eventId, event).execute();

                LOG.info(String.format("Event %s has been updated in calendar '%s'.", eventId, calendarId));
            } catch (IOException ex) {
                LOG.warn(String.format("Could not update event %s in calendar '%s'.", eventId, calendarId), ex);
                mailService.sendCalendarUpdateErrorNotification(calendarId, absence, eventId, ex.getMessage());
            }
        }
    }


    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {

        googleCalendarClient = getOrCreateGoogleCalendarClient();

        if (googleCalendarClient != null) {

            String calendarId =
                    settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getCalendarId();

            try {
                googleCalendarClient.events().delete(calendarId, eventId).execute();

                LOG.info(String.format("Event %s has been deleted in calendar '%s'.", eventId, calendarId));
            } catch (IOException ex) {
                LOG.warn(String.format("Could not delete event %s in calendar '%s'", eventId, calendarId), ex);
                mailService.sendCalendarDeleteErrorNotification(calendarId, eventId, ex.getMessage());
            }
        }
    }


    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {

        googleCalendarClient = getOrCreateGoogleCalendarClient();

        if (googleCalendarClient != null) {
            String calendarId =
                    settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getCalendarId();
            try {
                HttpResponse httpResponse = googleCalendarClient.calendarList().get(calendarId).executeUsingHead();
                if (httpResponse.getStatusCode() == SC_OK) {
                    LOG.info("Calendar sync successfully activated!");
                } else {
                    throw new IOException(httpResponse.getStatusMessage());
                }
            } catch (IOException e) {
                LOG.warn(String.format("Could not connect to calendar with calendar id '%s'", calendarId), e);
            }
        }
    }

    private Credential createCredentialWithRefreshToken(
            HttpTransport transport,
            JsonFactory jsonFactory,
            TokenResponse tokenResponse) {

        String clientId =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getClientId();
        String clientSecret =
                settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings().getClientSecret();

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                transport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(
                        new GenericUrl(GOOGLEAPIS_OAUTH2_V4_TOKEN))
                .setClientAuthentication(new BasicAuthentication(
                        clientId,
                        clientSecret))
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    private static void fillEvent(Absence absence, Event event) {

        event.setSummary(absence.getEventSubject());

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (absence.isAllDay()) {
            // To create an all-day event, you must use setDate() having created DateTime objects using a String
            DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_YYYY_MM_DD);
            String startDateStr = dateFormat.format(absence.getStartDate());
            String endDateStr = dateFormat.format(absence.getEndDate());

            DateTime startDateTime = new DateTime(startDateStr);
            DateTime endDateTime = new DateTime(endDateStr);

            startEventDateTime = new EventDateTime().setDate(startDateTime);
            endEventDateTime = new EventDateTime().setDate(endDateTime);
        } else {
            DateTime dateTimeStart = new DateTime(absence.getStartDate());
            DateTime dateTimeEnd = new DateTime(absence.getEndDate());

            startEventDateTime = new EventDateTime().setDateTime(dateTimeStart);
            endEventDateTime = new EventDateTime().setDateTime(dateTimeEnd);
        }

        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);
    }

}
