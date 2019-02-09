package org.synyx.urlaubsverwaltung.core.ical;

import net.fortuna.ical4j.model.Calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.Period;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.EventType;

import java.util.List;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.DateTimeFormat.forPattern;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.synyx.urlaubsverwaltung.core.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.core.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.core.sync.absence.EventType.ALLOWED_APPLICATION;
import static org.synyx.urlaubsverwaltung.core.sync.absence.EventType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.core.sync.absence.EventType.WAITING_APPLICATION;

import static java.util.Arrays.asList;


public class ICalServiceImplTest {

    private ICalServiceImpl sut;
    private AbsenceService absenceServiceMock;
    private DateTimeFormatter dateTimeFormatter = forPattern("yyyyMMdd'T'HHmmss'Z'");
    private String currentDTSTAMP;
    private String currentDTSTAMPPlusOneSecond;

    @Before
    public void setUp() {

        absenceServiceMock = mock(AbsenceService.class);
        sut = new ICalServiceImpl(absenceServiceMock);

        DateTime dateTime = new DateTime(UTC);
        currentDTSTAMP = dateTimeFormatter.print(dateTime); // This is not very nice. But there is no way to manipulate the ical lib to print a prepared stamp
        currentDTSTAMPPlusOneSecond = dateTimeFormatter.print(dateTime.plusSeconds(1)); // This is not very nice. But there is no way to manipulate the ical lib to print a prepared stamp
    }


    @Test
    public void getICal() {

        Absence absenceA = absence("APPLICATION_1", "Peter", "Sagan", toDateTime("2019-03-26"),
                toDateTime("2019-03-27"), FULL, ALLOWED_APPLICATION);
        Absence absenceB = absence("APPLICATION_2", "Foo", "Bar", toDateTime("2019-04-26"), toDateTime("2019-04-26"),
                MORNING, WAITING_APPLICATION);
        Absence absenceC = absence("APPLICATION_2", "Sick", "Paul", toDateTime("2019-05-26"), toDateTime("2019-05-30"),
                FULL, SICKNOTE);
        List<Absence> absences = asList(absenceA, absenceB, absenceC);
        when(absenceServiceMock.getOpenAbsences()).thenReturn(absences);

        String result = sut.getICal();

        try {
            assertEquals(expectedICalString(currentDTSTAMP), result);
        } catch (AssertionError e) {
            assertEquals(expectedICalString(currentDTSTAMPPlusOneSecond), result);
        }
    }


    private String expectedICalString(String currentDTSTAMP) {

        return "BEGIN:VCALENDAR\r\n"
            + "PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE\r\n"
            + "VERSION:2.0\r\n"
            + "BEGIN:VEVENT\r\n"
            + "DTSTAMP:" + currentDTSTAMP + "\r\n"
            + "DTSTART;VALUE=DATE:20190326\r\n"
            + "SUMMARY:Urlaub Sagan Peter\r\n"
            + "UID:APPLICATION_1\r\n"
            + "END:VEVENT\r\n"
            + "BEGIN:VEVENT\r\n"
            + "DTSTAMP:" + currentDTSTAMP + "\r\n"
            + "DTSTART:20190426T080000\r\n"
            + "DTEND:20190426T120000\r\n"
            + "SUMMARY:Antrag auf Urlaub Bar Foo\r\n"
            + "UID:APPLICATION_2\r\n"
            + "END:VEVENT\r\n"
            + "BEGIN:VEVENT\r\n"
            + "DTSTAMP:" + currentDTSTAMP + "\r\n"
            + "DTSTART;VALUE=DATE:20190526\r\n"
            + "SUMMARY:Paul Sick krank\r\n"
            + "UID:APPLICATION_2\r\n"
            + "END:VEVENT\r\n"
            + "END:VCALENDAR\r\n";
    }


    @Test
    public void validateICal() {

        Absence absence = absence("APPLICATION_1", "Peter", "Sagan", toDateTime("2019-03-26"),
                toDateTime("2019-03-27"), FULL, ALLOWED_APPLICATION);
        List<Absence> absences = asList(absence);
        when(absenceServiceMock.getOpenAbsences()).thenReturn(absences);

        Calendar iCalObject = sut.getICalObject();
        iCalObject.validate();
    }


    private Absence absence(String id, String firstName, String lastName, DateTime startTime, DateTime endTime,
        DayLength length, EventType eventType) {

        Person person = new Person("", firstName, lastName, "doesntmatter");
        DateMidnight start = startTime.toDateMidnight();
        DateMidnight end = endTime.toDateMidnight();
        Period period = new Period(start, end, length);
        AbsenceTimeConfiguration timConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(id, person, period, eventType, timConfig);
    }


    private static DateTime toDateTime(String input) {

        String pattern = "yyyy-MM-dddd";

        return DateTime.parse(input, forPattern(pattern));
    }
}
