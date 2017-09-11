package org.synyx.urlaubsverwaltung.core.sync.providers.google;

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
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
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
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProviderService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Hammann - hammann@synyx.de
 * @author Marc Sommer - sommer@synyx.de
 */
@Service
public class GoogleCalendarSyncProviderService implements CalendarProviderService {

    public static final String APPLICATION_NAME = "Urlaubsverwaltung";
    private static final Logger LOG = Logger.getLogger(GoogleCalendarSyncProviderService.class);
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Calendar calendarService;
    private final MailService mailService;

    private final GoogleCalendarSettings settings;

    @Autowired
    public GoogleCalendarSyncProviderService(MailService mailService, SettingsService settingsService) {

        settings = settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();

        this.mailService = mailService;
    }

    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        String calendarId = googleCalendarSettings.getCalendarId();

        if (calendarService == null) {
            calendarService = getCalendarService();
        }

        try {
            Event eventToCommit = new Event();
            fillEvent(absence, eventToCommit);


            Event eventInCalendar = calendarService.events().insert(calendarId, eventToCommit).execute();

            LOG.info(String.format("Event %s for '%s' added to google calendar '%s'.", eventInCalendar.getId(),
                    absence.getPerson().getNiceName(), eventInCalendar.getSummary()));

            return Optional.of(eventInCalendar.getId());
        } catch (IOException ex) {
            LOG.warn("An error occurred while trying to add appointment to Exchange calendar", ex);
            mailService.sendCalendarSyncErrorNotification(calendarId, absence, ex.toString());
        }

        return Optional.empty();
    }


    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     */
    private com.google.api.services.calendar.Calendar getCalendarService() {

        String refreshToken = settings.getRefreshToken();

        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setRefreshToken(refreshToken);

            Credential credential = createCredentialWithRefreshToken(httpTransport, JSON_FACTORY, tokenResponse);

            return new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Credential createCredentialWithRefreshToken(
            HttpTransport transport,
            JsonFactory jsonFactory,
            TokenResponse tokenResponse) {

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                transport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(
                        new GenericUrl("https://www.googleapis.com/oauth2/v4/token"))
                .setClientAuthentication(new BasicAuthentication(
                        settings.getClientId(),
                        settings.getClientSecret()))
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    private void debugCalendar(com.google.api.services.calendar.Calendar calendar) {

        try {
            CalendarList calendarList = calendar.calendarList().list().execute();
            List<CalendarListEntry> calendarListItems = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : calendarListItems) {
                LOG.debug("calendarListEntry: " + calendarListEntry.toPrettyString());
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }


    private void fillEvent(Absence absence, Event event) {

        event.setSummary(absence.getEventSubject());

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (absence.isAllDay()) {
            // To create an all-day event, you must use setDate() having created DateTime objects using a String
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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


    @Override
    public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {

        GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        String calendarId = googleCalendarSettings.getCalendarId();

        try {
            // gather exiting event
            Event event = calendarService.events().get(calendarId, eventId).execute();

            // update event with absence
            fillEvent(absence, event);

            // sync event to calendar
            calendarService.events().patch(calendarId, eventId, event).execute();

            LOG.info(String.format("Event %s has been updated in google calendar '%s'.", eventId, calendarId));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not update event %s in google calendar '%s'.", eventId, calendarId));
            mailService.sendCalendarUpdateErrorNotification(calendarId, absence, eventId, ex.getMessage());
        }
    }


    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {

        GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        String calendarId = googleCalendarSettings.getCalendarId();

        try {
            calendarService.events().delete(calendarId, eventId).execute();

            LOG.info(String.format("Event %s has been deleted in google calendar '%s'.", eventId, calendarId));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not delete event %s in google calendar '%s'", eventId, calendarId));
            mailService.sendCalendarDeleteErrorNotification(calendarId, eventId, ex.getMessage());
        }
    }


    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
        //TODO: Implement me!
    }

}