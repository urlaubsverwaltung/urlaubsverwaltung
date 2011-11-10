/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.EventWho;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.io.IOException;

import java.net.URL;

import java.util.List;


/**
 * @author  aljona (not really.... eigtl: @author otto allmendinger)
 *
 *          <p>dies ist ein vorläufiger versuch den google calendar einzubinden die methoden wurden dem
 *          googlecalendarserviceimpl des ressourcenplanungstools von Otto Allmendinger - allmendinger@synyx.de
 *          entnommen für das urlaubsverwaltungstool nicht nötige methoden und attribute wurden auskommentiert</p>
 */
public class GoogleCalendarServiceImpl implements CalendarService {

    private static final DateTimeZone calendarTimeZone = DateTimeZone.forID("Europe/Berlin");

//    private static final Log LOG = LogFactory.getLog(GoogleCalendarServiceImpl.class);

    private static final String GOOGLE_BASE_URL = "http://www.google.com/calendar/feeds/";

    private final String username;
    private final String password;
    private com.google.gdata.client.calendar.CalendarService googleCalendarService = null;

    public GoogleCalendarServiceImpl(String username, String password) {

        this.username = username;
        this.password = password;
    }

//    private static String getApplicationName() {
//        return "synyx.de-resourceplanning-$version"; // TODO: get version from somewhere
//    }

    @Override
    public int getWorkDays(LocalDate start, LocalDate end) throws AuthenticationException, IOException,
        ServiceException {

        int nettoUrlaubstage = 0;

        // Kalendar Deutsche Feiertage

        URL feedUrl = new URL(
                "http://www.google.com/calendar/feeds/de.german%23holiday%40group.v.calendar.google.com/public/basic");

        CalendarQuery query = new CalendarQuery(feedUrl);

        // aus LocalDate joda time DateTime erzeugen
        DateTime startTime = start.toDateTimeAtStartOfDay();
        DateTime endTime = end.toDateTimeAtStartOfDay();

        // aus joda time DateTime wird GoogleDateTime erzeugt und dann ins When als Start und Ende gesetzt
        query.setMinimumStartTime(toGoogleDateTime(startTime));
        query.setMaximumStartTime(toGoogleDateTime(endTime));

        getGoogleCalendarService().setUserCredentials(username, password);

        CalendarEventFeed resultFeed = getGoogleCalendarService().query(query, CalendarEventFeed.class);

        List<CalendarEventEntry> entries = resultFeed.getEntries();

        for (CalendarEventEntry entry : entries) {
            List<When> when = entry.getTimes();

            for (When w : when) {
            }
        }

        return 5;
    }


    /**
     * Einen Eintrag, wer wann Urlaub hat im Google Kalender setzen
     *
     * @param  start
     * @param  end
     * @param  person
     * @param  calendarId
     *
     * @throws  AuthenticationException
     * @throws  IOException
     * @throws  ServiceException
     *
     * @author  aljona
     */
    @Override
    public void addVacation(LocalDate start, LocalDate end, Person person, String calendarId)
        throws AuthenticationException, IOException, ServiceException {

        // Url des Kalenders
        URL postUrl = new URL(GOOGLE_BASE_URL + calendarId + "/private/full");

        // Entry erzeugen
        CalendarEventEntry entry = new CalendarEventEntry();

        // Attribute des Eintrags setzen
        entry.setTitle(new PlainTextConstruct("Urlaub"));
        entry.setContent(new PlainTextConstruct(person.getFirstName() + " " + person.getLastName() + " hat Urlaub."));

        // aus LocalDate joda time DateTime erzeugen
        DateTime startTime = start.toDateTimeAtStartOfDay();
        DateTime endTime = end.toDateTimeAtStartOfDay();

        // When erzeugen
        When eventTimes = new When();

        // aus joda time DateTime wird GoogleDateTime erzeugt und dann ins When als Start und Ende gesetzt
        eventTimes.setStartTime(toGoogleDateTime(startTime));
        eventTimes.setEndTime(toGoogleDateTime(endTime));

        // das When ins Entry setzen
        entry.addTime(eventTimes);

        // den google calendar service aufrufen und Url und Entry setzen
        getGoogleCalendarService().insert(postUrl, entry);
    }


    private static String calendarEventToString(CalendarEventEntry entry) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Calendar Event \"").append(entry.getTitle().getPlainText()).append("\", times [");

        for (When when : entry.getTimes()) {
            stringBuilder.append(when.getStartTime()).append(" to ").append(when.getEndTime()).append(" ");
        }

        stringBuilder.append("], participants [");

        for (EventWho who : entry.getParticipants()) {
            stringBuilder.append(who.getEmail()).append(" ");
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }


    private static boolean isDateOnly(When when) {

        boolean startDateOnly = when.getStartTime().isDateOnly();
        boolean endDateOnly = when.getEndTime().isDateOnly();

        if (startDateOnly && endDateOnly) {
            return true;
        } else if (!startDateOnly && !endDateOnly) {
            return false;
        } else {
            throw new IllegalArgumentException(String.format("Inconsistency: start.isDateOnly()=%s end.isDateOnly()=%s",
                    startDateOnly, endDateOnly));
        }
    }


    private static com.google.gdata.data.DateTime toGoogleDateTime(DateTime input) {

        com.google.gdata.data.DateTime dateTime = new com.google.gdata.data.DateTime();
        dateTime.setTzShift(new Period(calendarTimeZone.getOffset(input)).toStandardMinutes().getMinutes());
        dateTime.setValue(input.getMillis());

        return dateTime;
    }


    private static LocalDate toJodaLocalDate(com.google.gdata.data.DateTime dateTime) {

        Assert.isTrue(dateTime.isDateOnly());

        return new LocalDate(ISODateTimeFormat.yearMonthDay().parseDateTime(dateTime.toString()));
    }


    private static DateTime toJodaDateTime(com.google.gdata.data.DateTime dateTime) {

        Assert.isTrue(!dateTime.isDateOnly());

        return ISODateTimeFormat.dateTime().parseDateTime(dateTime.toString());
    }


    private static void addGoogleCalendarEvent(TimeTable timeTable, CalendarEventEntry entry) {

        // if assertion below fails, check the singleevents=true parameter and see
        // http://code.google.com/apis/calendar/data/2.0/developers_guide_protocol.html#CreatingRecurring

        Assert.isTrue(entry.getTimes().size() == 1, "invalid number of times associated");

        When when = entry.getTimes().get(0);

        if (isDateOnly(when)) {
            LocalDate firstDay = toJodaLocalDate(when.getStartTime());
            LocalDate lastDay = toJodaLocalDate(when.getEndTime());
            timeTable.addEventSpan(firstDay, lastDay);
        } else {
            DateTime start = toJodaDateTime(when.getStartTime());
            DateTime end = toJodaDateTime(when.getEndTime());
            timeTable.addEventSpan(start, end);
        }
    }


    public CalendarEventFeed getCalendarEventFeed(String calendarId, YearWeek yearWeek) throws AuthenticationException,
        IOException, ServiceException {

        return getCalendarEventFeed(new URL(GOOGLE_BASE_URL + calendarId + "/private/full"), yearWeek);
    }


    public CalendarEventFeed getCalendarEventFeed(URL calendarFeedUrl, YearWeek yearWeek)
        throws AuthenticationException, ServiceException, IOException {

        CalendarQuery query = new CalendarQuery(calendarFeedUrl);
        query.setMinimumStartTime(toGoogleDateTime(yearWeek.getFirstDay().toDateTimeAtStartOfDay(calendarTimeZone)));
        query.setMaximumStartTime(toGoogleDateTime(
                yearWeek.getNextWeek().getFirstDay().toDateTimeAtStartOfDay(calendarTimeZone)));

        query.setStringCustomParameter("singleevents", "true"); // expand recurring events into single events
        query.setStringCustomParameter("orderby", "starttime");
        query.setStringCustomParameter("sortorder", "ascending");

        CalendarEventFeed calendarEventFeed = getGoogleCalendarService().query(query, CalendarEventFeed.class);

        if (!calendarEventFeed.getTimeZone().getValue().equals(calendarTimeZone.getID())) {
            throw new GoogleCalendarSynchronisationException(String.format(
                    "Google Calendar time zone %s doesn't match expected time zone %s",
                    calendarEventFeed.getTimeZone().getValue(), calendarTimeZone.getID()));
        }

        return calendarEventFeed;
    }


    private com.google.gdata.client.calendar.CalendarService getGoogleCalendarService() throws AuthenticationException {

        if (googleCalendarService == null) {
            if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
                throw new IllegalStateException("username or password not set");
            }

//            googleCalendarService = new com.google.gdata.client.calendar.CalendarService(getApplicationName());
            googleCalendarService.setUserCredentials(username, password);
        }

        return googleCalendarService;
    }

//    public int[] getWeekMinutes(Collection<CalendarEventEntry> events, YearWeek yearWeek) {
//
//        TimeTable timeTable = new TimeTable();
//
//        for (CalendarEventEntry event : events) {
//            addGoogleCalendarEvent(timeTable, event);
//        }
//
//        return timeTable.getWeekMinutes(yearWeek);
//    }
//
//
//    public int[] getWeekMinutes(Collection<CalendarEventEntry> events, YearWeek yearWeek, Person person) {
//
//        return getWeekMinutes(Collections2.filter(events, new IsParticipant(person)), yearWeek);
//    }

    private final class IsParticipant implements Predicate<CalendarEventEntry> {

        private final Person person;

        public IsParticipant(Person person) {

            this.person = person;
        }

        public boolean apply(CalendarEventEntry input) {

            for (EventWho who : input.getParticipants()) {
                if (who.getEmail().equals(this.person.getEmail())) {
                    return true;
                }
            }

            return false;
        }
    }
}
