package org.synyx.urlaubsverwaltung.calendarintegration.providers.google;

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
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarMailService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.http.HttpStatus.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class GoogleCalendarSyncProvider implements CalendarProvider {

    private static final String DATE_PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final String GOOGLEAPIS_OAUTH2_V4_TOKEN = "https://www.googleapis.com/oauth2/v4/token";

    private Calendar googleCalendarClient;
    private int refreshTokenHashCode;

    private final CalendarMailService calendarMailService;
    private final SettingsService settingsService;

    @Autowired
    public GoogleCalendarSyncProvider(CalendarMailService calendarMailService, SettingsService settingsService) {
        this.calendarMailService = calendarMailService;

        this.settingsService = settingsService;
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
            LOG.error("Something went wrong!", e);
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

                LOG.info("Event {} for '{}' added to google calendar '{}'.", eventInCalendar.getId(),
                    absence.getPerson().getId(), calendarId);

                return Optional.of(eventInCalendar.getId());

            } catch (IOException ex) {
                LOG.warn("An error occurred while trying to add appointment to calendar {}", calendarId, ex);
                calendarMailService.sendCalendarSyncErrorNotification(calendarId, absence, ex.toString());
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

                LOG.info("Event {} has been updated in calendar '{}'.", eventId, calendarId);
            } catch (IOException ex) {
                LOG.warn("Could not update event {} in calendar '{}'.", eventId, calendarId, ex);
                calendarMailService.sendCalendarUpdateErrorNotification(calendarId, absence, eventId, ex.getMessage());
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

                LOG.info("Event {} has been deleted in calendar '{}'.", eventId, calendarId);
            } catch (IOException ex) {
                LOG.warn("Could not delete event {} in calendar '{}'", eventId, calendarId, ex);
                calendarMailService.sendCalendarDeleteErrorNotification(calendarId, eventId, ex.getMessage());
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
                LOG.warn("Could not connect to calendar with calendar id '{}'", calendarId, e);
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

        EventAttendee eventAttendee = new EventAttendee();
        eventAttendee.setEmail(absence.getPerson().getEmail());
        eventAttendee.setDisplayName(absence.getPerson().getNiceName());
        event.setAttendees(Collections.singletonList(eventAttendee));

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (absence.isAllDay()) {
            // To create an all-day event, you must use setDate() having created DateTime objects using a String
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN_YYYY_MM_DD);
            String startDateStr = dateTimeFormatter.format(absence.getStartDate());
            String endDateStr = dateTimeFormatter.format(absence.getEndDate());

            DateTime startDateTime = new DateTime(startDateStr);
            DateTime endDateTime = new DateTime(endDateStr);

            startEventDateTime = new EventDateTime().setDate(startDateTime);
            endEventDateTime = new EventDateTime().setDate(endDateTime);
        } else {
            DateTime dateTimeStart = new DateTime(Date.from(absence.getStartDate().toInstant()));
            DateTime dateTimeEnd = new DateTime(Date.from(absence.getEndDate().toInstant()));

            startEventDateTime = new EventDateTime().setDateTime(dateTimeStart);
            endEventDateTime = new EventDateTime().setDateTime(dateTimeEnd);
        }

        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);
    }

}
