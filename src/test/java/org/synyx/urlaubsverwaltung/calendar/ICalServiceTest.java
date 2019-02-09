package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.model.Calendar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.absence.AbsenceService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class ICalServiceTest {

    private ICalService sut;

    @Mock
    private AbsenceService absenceService;
    @Mock
    private PersonService personService;

    @Before
    public void setUp() {

        sut = new ICalService(absenceService, personService);
    }

    @Test
    public void getICalForOneFullDay() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);
        when(absenceService.getOpenAbsences(person)).thenReturn(List.of(fullDayAbsence));

        final String iCal = sut.getCalendarForPerson(1);

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;VALUE=DATE:20190326");
    }

    @Test
    public void getICalForHalfDayMorning() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Absence morningAbsence = absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);
        when(absenceService.getOpenAbsences(person)).thenReturn(List.of(morningAbsence));

        final String iCal = sut.getCalendarForPerson(1);

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190426T080000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190426T120000");
    }

    @Test
    public void getICalForMultipleFullDays() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Absence manyFullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-04-01"), FULL);
        when(absenceService.getOpenAbsences(person)).thenReturn(List.of(manyFullDayAbsence));

        final String iCal = sut.getCalendarForPerson(1);

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190326T000000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190401T000000");
    }

    @Test
    public void getICalForHalfDayNoon() {

        final Person person = createPerson();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        when(absenceService.getOpenAbsences(person)).thenReturn(List.of(noonAbsence));

        final String iCal = sut.getCalendarForPerson(1);

        assertThat(iCal).contains("VERSION:2.0");
        assertThat(iCal).contains("CALSCALE:GREGORIAN");
        assertThat(iCal).contains("PRODID:-//Urlaubsverwaltung//iCal4j 1.0//DE");
        assertThat(iCal).contains("X-WR-CALNAME:Abwesenheitskalender von Marlene Muster");

        assertThat(iCal).contains("SUMMARY:Marlene Muster abwesend");
        assertThat(iCal).contains("DTSTART;TZID=Etc/UTC:20190526T120000");
        assertThat(iCal).contains("DTEND;TZID=Etc/UTC:20190526T160000");
    }

    @Test
    public void validateICal() {

        final Person person = createPerson();
        final Absence morningAbsence = absence(person, toDateTime("2019-04-26"), toDateTime("2019-04-26"), MORNING);
        final Absence noonAbsence = absence(person, toDateTime("2019-05-26"), toDateTime("2019-05-26"), NOON);
        final Absence fullMultipleDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-28"), FULL);
        final Absence fullDayAbsence = absence(person, toDateTime("2019-03-26"), toDateTime("2019-03-26"), FULL);

        final Calendar calendar = sut.generateCalendar("title", List.of(morningAbsence, noonAbsence, fullDayAbsence, fullMultipleDayAbsence));
        calendar.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getICalButPersonNotFound() {

        when(personService.getPersonByID(1)).thenReturn(Optional.ofNullable(null));

        sut.getCalendarForPerson(1);
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
