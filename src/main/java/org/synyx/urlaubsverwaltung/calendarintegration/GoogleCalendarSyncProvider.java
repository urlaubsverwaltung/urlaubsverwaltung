package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.http.HttpStatus.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class GoogleCalendarSyncProvider implements CalendarProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String DATE_PATTERN_YYYY_MM_DD = "yyyy-MM-dd";

    private final GoogleCalendarClientProvider googleCalendarClientProvider;

    @Autowired
    GoogleCalendarSyncProvider(GoogleCalendarClientProvider googleCalendarClientProvider) {
        this.googleCalendarClientProvider = googleCalendarClientProvider;
    }

    @Override
    public Optional<String> add(CalendarAbsence absence, CalendarSettings calendarSettings) {

        final GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        final Optional<Calendar> maybeCalendarClient = googleCalendarClientProvider.getCalendarClient(googleCalendarSettings);

        if (maybeCalendarClient.isPresent()) {
            final String calendarId = googleCalendarSettings.getCalendarId();
            try {
                final Event eventToCommit = new Event();
                fillEvent(absence, eventToCommit);

                final Event eventInCalendar = maybeCalendarClient.get().events().insert(calendarId, eventToCommit).execute();

                LOG.info("Event {} for '{}' added to google calendar '{}'.", eventInCalendar.getId(),
                    absence.getPerson().getId(), calendarId);

                return Optional.of(eventInCalendar.getId());

            } catch (IOException ex) {
                LOG.warn("An error occurred while trying to add appointment to calendar {}", calendarId, ex);
            }
        }
        return Optional.empty();
    }

    @Override
    public void update(CalendarAbsence absence, String eventId, CalendarSettings calendarSettings) {

        final GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        final Optional<Calendar> maybeCalendarClient = googleCalendarClientProvider.getCalendarClient(googleCalendarSettings);

        if (maybeCalendarClient.isPresent()) {
            final String calendarId = googleCalendarSettings.getCalendarId();
            try {
                final Calendar calendarClient = maybeCalendarClient.get();

                // gather exiting event
                final Event event = calendarClient.events().get(calendarId, eventId).execute();

                // update event with absence
                fillEvent(absence, event);

                // sync event to calendar
                calendarClient.events().patch(calendarId, eventId, event).execute();

                LOG.info("Event {} has been updated in calendar '{}'.", eventId, calendarId);
            } catch (IOException ex) {
                LOG.warn("Could not update event {} in calendar '{}'.", eventId, calendarId, ex);
            }
        }
    }

    @Override
    public Optional<String> delete(String eventId, CalendarSettings calendarSettings) {

        final GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        final Optional<Calendar> maybeCalendarClient = googleCalendarClientProvider.getCalendarClient(googleCalendarSettings);

        if (maybeCalendarClient.isPresent()) {
            final String calendarId = googleCalendarSettings.getCalendarId();
            try {
                maybeCalendarClient.get().events().delete(calendarId, eventId).execute();
                LOG.info("Event {} has been deleted in calendar '{}'.", eventId, calendarId);
                return Optional.of(eventId);
            } catch (IOException ex) {
                LOG.warn("Could not delete event {} in calendar '{}'", eventId, calendarId, ex);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {

        final GoogleCalendarSettings googleCalendarSettings = calendarSettings.getGoogleCalendarSettings();
        final Optional<Calendar> maybeCalendarClient = googleCalendarClientProvider.getCalendarClient(googleCalendarSettings);

        if (maybeCalendarClient.isPresent()) {
            final String calendarId = googleCalendarSettings.getCalendarId();
            try {
                final HttpResponse httpResponse = maybeCalendarClient.get().calendarList().get(calendarId).executeUsingHead();
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

    /**
     * Build and return an authorized google calendar client.
     *
     * @return an authorized calendar client service
     */
    private static void fillEvent(CalendarAbsence absence, Event event) {

        event.setSummary(getEventSubject(absence));

        final EventAttendee eventAttendee = new EventAttendee();
        eventAttendee.setEmail(absence.getPerson().getEmail());
        eventAttendee.setDisplayName(absence.getPerson().getNiceName());
        event.setAttendees(List.of(eventAttendee));

        final EventDateTime startEventDateTime;
        final EventDateTime endEventDateTime;

        if (absence.isAllDay()) {
            // To create an all-day event, you must use setDate() having created DateTime objects using a String
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN_YYYY_MM_DD);
            final String startDateStr = dateTimeFormatter.format(absence.getStartDate());
            final String endDateStr = dateTimeFormatter.format(absence.getEndDate());

            final DateTime startDateTime = new DateTime(startDateStr);
            final DateTime endDateTime = new DateTime(endDateStr);

            startEventDateTime = new EventDateTime().setDate(startDateTime);
            endEventDateTime = new EventDateTime().setDate(endDateTime);
        } else {
            final DateTime dateTimeStart = new DateTime(Date.from(absence.getStartDate().toInstant()));
            final DateTime dateTimeEnd = new DateTime(Date.from(absence.getEndDate().toInstant()));

            startEventDateTime = new EventDateTime().setDateTime(dateTimeStart);
            endEventDateTime = new EventDateTime().setDateTime(dateTimeEnd);
        }

        event.setStart(startEventDateTime);
        event.setEnd(endEventDateTime);
    }

    private static String getEventSubject(CalendarAbsence absence) {
        if (absence.isHolidayReplacement()) {
            return format("Vertretung für %s", absence.getPerson().getNiceName());
        }

        return format("%s abwesend", absence.getPerson().getNiceName());
    }
}
