package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class ICalServiceTest {

    private ICalService sut;

    @Before
    public void setUp() {

        sut = new ICalService();
    }

    @Test(expected = CalendarException.class)
    public void getCalendarForPersonAndNoAbsenceFound() {

        sut.generateCalendar("Abwesenheitskalender", List.of());
    }

    @Test
    public void getCalendarForPersonForOneFullDay() {

        final Absence fullDayAbsence = absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(fullDayAbsence));

        assertThat(calendar).contains("VERSION:2.0");
        assertThat(calendar).contains("CALSCALE:GREGORIAN");
        assertThat(calendar).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(calendar).contains("X-WR-CALNAME:Abwesenheitskalender");

        assertThat(calendar).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(calendar).contains("DTSTART;VALUE=DATE:20190326");
    }

    @Test
    public void getCalendarForPersonForHalfDayMorning() {

        final Absence morningAbsence = absence(createPerson(), toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(morningAbsence));

        assertThat(calendar).contains("VERSION:2.0");
        assertThat(calendar).contains("CALSCALE:GREGORIAN");
        assertThat(calendar).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(calendar).contains("X-WR-CALNAME:Abwesenheitskalender");

        assertThat(calendar).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(calendar).contains("DTSTART;TZID=Etc/UTC:20190426T080000");
        assertThat(calendar).contains("DTEND;TZID=Etc/UTC:20190426T120000");
    }

    @Test
    public void getCalendarForPersonForMultipleFullDays() {

        final Absence manyFullDayAbsence = absence(createPerson(), toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(manyFullDayAbsence));

        assertThat(calendar).contains("VERSION:2.0");
        assertThat(calendar).contains("CALSCALE:GREGORIAN");
        assertThat(calendar).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(calendar).contains("X-WR-CALNAME:Abwesenheitskalender");

        assertThat(calendar).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(calendar).contains("DTSTART;TZID=Etc/UTC:20190326T000000");
        assertThat(calendar).contains("DTEND;TZID=Etc/UTC:20190401T000000");
    }

    @Test
    public void getCalendarForPersonForHalfDayNoon() {

        final Absence noonAbsence = absence(createPerson(), toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);

        final String calendar = sut.generateCalendar("Abwesenheitskalender", List.of(noonAbsence));
        assertThat(calendar).contains("VERSION:2.0");
        assertThat(calendar).contains("CALSCALE:GREGORIAN");
        assertThat(calendar).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(calendar).contains("X-WR-CALNAME:Abwesenheitskalender");

        assertThat(calendar).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(calendar).contains("DTSTART;TZID=Etc/UTC:20190526T120000");
        assertThat(calendar).contains("DTEND;TZID=Etc/UTC:20190526T160000");
    }

    private Absence absence(Person person, LocalDate start, LocalDate end, DayLength length) {
        final Period period = new Period(start, end, length);
        final AbsenceTimeConfiguration timeConfig = new AbsenceTimeConfiguration(new CalendarSettings());

        return new Absence(person, period, timeConfig);
    }

    private static LocalDate toDateTime(String input) {
        return LocalDate.parse(input, ofPattern("yyyy-MM-dd"));
    }
}
