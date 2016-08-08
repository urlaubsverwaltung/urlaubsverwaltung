package org.synyx.urlaubsverwaltung.core.sync.providers.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
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
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProviderService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Daniel Hammann - hammann@synyx.de
 * @author Marc Sommer - sommer@synyx.de
 */
@Service
public class GoogleCalendarSyncProviderService implements CalendarProviderService {

    private static final Logger LOG = Logger.getLogger(GoogleCalendarSyncProviderService.class);

    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), "urlaubsverwaltung");
    private static final Set<String> CALENDAR_SCOPES = CalendarScopes.all();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private final MailService mailService;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            LOG.warn(t.getMessage(), t);
        }
    }

    private String calendarName;

    // something@resource.calendar.google.com
    private String calendarId;

    // something@applicationname.iam.gserviceaccount.com
    private String serviceAccount;

    // the filename of the PKCS#12 (.p12) archive in user.home/urlaubsverwaltung
    private String pkcs12File;


    @Autowired
    public GoogleCalendarSyncProviderService(MailService mailService, SettingsService settingsService) {

        GoogleCalendarSettings settings = settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();
        this.calendarName = settings.getCalendar();
        this.calendarId = settings.getCalendarId();
        this.serviceAccount = settings.getServiceAccount();
        this.pkcs12File = settings.getPkcs12KeyFile();
        this.mailService = mailService;
    }


    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();

        try {
            Event eventToCommit = new Event();
            fillEvent(absence, eventToCommit);

            Event eventInCalendar = getCalendarService().events().insert(this.calendarId, eventToCommit).execute();

            LOG.info(String.format("Event %s for '%s' added to google calendar '%s'.", eventInCalendar.getId(),
                    absence.getPerson().getNiceName(), eventInCalendar.getSummary()));

            return Optional.of(eventInCalendar.getId());
        } catch (IOException ex) {
            LOG.warn("An error occurred while trying to add appointment to Exchange calendar", ex);
            mailService.sendCalendarSyncErrorNotification(googleCalendarSettings.getCalendar(), absence, ex.toString());
        }

        return Optional.empty();
    }


    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     *
     * @throws IOException
     */
    public com.google.api.services.calendar.Calendar getCalendarService() throws IOException {

        GoogleCredential credential = null;
        try {

            File p12_key = new File(DATA_STORE_DIR.getPath() + "/" + this.pkcs12File);

            credential = new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountPrivateKeyFromP12File(p12_key)
                    .setServiceAccountScopes(CALENDAR_SCOPES)
                    .setServiceAccountId(this.serviceAccount)
                    .build();

        } catch (GeneralSecurityException e) {
            LOG.error(e.getMessage(), e);
        }

        com.google.api.services.calendar.Calendar calendar = new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).build();

        if (LOG.isDebugEnabled()) {
            debugCalendar(calendar);
        }

        return calendar;
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

        try {
            // gather exiting event
            Event event = getCalendarService().events().get(this.calendarId, eventId).execute();

            // update event with absence
            fillEvent(absence, event);

            // sync event to calendar
            getCalendarService().events().patch(this.calendarId, eventId, event).execute();

            LOG.info(String.format("Event %s has been updated in google calendar '%s'.", eventId, this.calendarName));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not update event %s in google calendar '%s'.", eventId, this.calendarName));
            mailService.sendCalendarUpdateErrorNotification(this.calendarName, absence, eventId, ex.getMessage());
        }
    }


    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {

        try {
            getCalendarService().events().delete(this.calendarId, eventId).execute();

            LOG.info(String.format("Event %s has been deleted in google calendar '%s'.", eventId, this.calendarName));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not delete event %s in google calendar '%s'", eventId, this.calendarName));
            mailService.sendCalendarDeleteErrorNotification(this.calendarName, eventId, ex.getMessage());
        }
    }


    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {

    }

}