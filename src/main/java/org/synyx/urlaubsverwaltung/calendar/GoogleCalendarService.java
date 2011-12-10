/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.io.IOException;

import java.net.URL;


/**
 * @author  Aljona Murygina (urspruenglich: @author otto allmendinger)
 *
 *          <p>dies ist ein vorläufiger versuch den google calendar einzubinden die methoden, die wir fuer das
 *          urlaubsverwaltungstool benoetigen, wurden dem googlecalendarserviceimpl des ressourcenplanungstools von Otto
 *          Allmendinger - allmendinger@synyx.de entnommen für das urlaubsverwaltungstool</p>
 */
public class GoogleCalendarService {

    private static final DateTimeZone calendarTimeZone = DateTimeZone.forID("Europe/Berlin"); // als property!!!

    private static final String GOOGLE_BASE_URL = "http://www.google.com/calendar/feeds/";

    private final String username;
    private final String password;
    private com.google.gdata.client.calendar.CalendarService googleCalendarService = null;

    public GoogleCalendarService(String username, String password) {

        this.username = username;
        this.password = password;
    }

//    private static String getApplicationName() {
//        return "synyx.de-resourceplanning-$version"; // TODO: get version from somewhere
//    }

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
    public void addVacation(LocalDate start, LocalDate end, Person person, String calendarId)
        throws AuthenticationException, IOException, ServiceException {

        // Strings in properties setzen

        // URl mit String format zusammenbauen
        // pattern angeben mit parameter hintendran
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
        googleCalendarService.setUserCredentials(username, password);
        googleCalendarService.insert(postUrl, entry);
        // besser: uri.toUrl
    }


    private static com.google.gdata.data.DateTime toGoogleDateTime(DateTime input) {

        com.google.gdata.data.DateTime dateTime = new com.google.gdata.data.DateTime();
        dateTime.setTzShift(new Period(calendarTimeZone.getOffset(input)).toStandardMinutes().getMinutes());
        dateTime.setValue(input.getMillis());

        return dateTime;
    }

//    private com.google.gdata.client.calendar.CalendarService getGoogleCalendarService() throws AuthenticationException {
//
//        if (googleCalendarService == null) {
//            if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
//                throw new IllegalStateException("username or password not set");
//            }
//
////            googleCalendarService = new com.google.gdata.client.calendar.CalendarService(getApplicationName());
//            googleCalendarService.setUserCredentials(username, password);
//        }
//
//        return googleCalendarService;
//    }

//    private static String calendarEventToString(CalendarEventEntry entry) {
//
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("Calendar Event \"").append(entry.getTitle().getPlainText()).append("\", times [");
//
//        for (When when : entry.getTimes()) {
//            stringBuilder.append(when.getStartTime()).append(" to ").append(when.getEndTime()).append(" ");
//        }
//
//        stringBuilder.append("], participants [");
//
//        for (EventWho who : entry.getParticipants()) {
//            stringBuilder.append(who.getEmail()).append(" ");
//        }
//
//        stringBuilder.append("]");
//
//        return stringBuilder.toString();
//    }
//
//
//    public CalendarEventFeed getCalendarEventFeed(String calendarId, YearWeek yearWeek) throws AuthenticationException,
//        IOException, ServiceException {
//
//        return getCalendarEventFeed(new URL(GOOGLE_BASE_URL + calendarId + "/private/full"), yearWeek);
//    }
//
//
//    public CalendarEventFeed getCalendarEventFeed(URL calendarFeedUrl, YearWeek yearWeek)
//        throws AuthenticationException, ServiceException, IOException {
//
//        CalendarQuery query = new CalendarQuery(calendarFeedUrl);
//        query.setMinimumStartTime(toGoogleDateTime(yearWeek.getFirstDay().toDateTimeAtStartOfDay(calendarTimeZone)));
//        query.setMaximumStartTime(toGoogleDateTime(
//                yearWeek.getNextWeek().getFirstDay().toDateTimeAtStartOfDay(calendarTimeZone)));
//
//        query.setStringCustomParameter("singleevents", "true"); // expand recurring events into single events
//        query.setStringCustomParameter("orderby", "starttime");
//        query.setStringCustomParameter("sortorder", "ascending");
//
//        CalendarEventFeed calendarEventFeed = getGoogleCalendarService().query(query, CalendarEventFeed.class);
//
//        if (!calendarEventFeed.getTimeZone().getValue().equals(calendarTimeZone.getID())) {
//            throw new GoogleCalendarSynchronisationException(String.format(
//                    "Google Calendar time zone %s doesn't match expected time zone %s",
//                    calendarEventFeed.getTimeZone().getValue(), calendarTimeZone.getID()));
//        }
//
//        return calendarEventFeed;
//    }

}
