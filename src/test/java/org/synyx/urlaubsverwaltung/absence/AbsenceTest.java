package org.synyx.urlaubsverwaltung.absence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Unit test for {@link Absence}.
 */
public class AbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration timeConfiguration;
    private Clock clock;

    @Before
    public void setUp() {

        person = TestDataCreator.createPerson();
        clock = Clock.systemUTC();

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(16);

        timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
    }


    @Test
    public void ensureCanBeInstantiatedWithCorrectProperties() {

        Instant start = Instant.from(LocalDate.of(2015, 9, 21));
        Instant end = Instant.from(LocalDate.of(2015, 9, 23));

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", LocalDate.from(start).atStartOfDay(clock.getZone()).toInstant(), absence.getStartDate());
        Assert.assertEquals("Wrong end date", LocalDate.from(end.plus(1, DAYS)).atStartOfDay(clock.getZone()).toInstant(), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
    }


    @Test
    public void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        Instant start = Instant.from(LocalDate.of(2015, 10, 23));
        Instant end = Instant.from(LocalDate.of(2015, 10, 25));

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", LocalDate.from(start).atStartOfDay(clock.getZone()).toInstant(), absence.getStartDate());
        Assert.assertEquals("Wrong end date", LocalDate.from(end.plus(1, DAYS)).atStartOfDay(clock.getZone()).toInstant(), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
    }


    @Test
    public void ensureCorrectTimeForMorningAbsence() {

        Instant today = LocalDate.from(Instant.now(clock)).atStartOfDay(clock.getZone()).toInstant();

        ZonedDateTime start = today.with(HOUR_OF_DAY, 8).atZone(clock.getZone());
        ZonedDateTime end = today.with(HOUR_OF_DAY, 12).atZone(clock.getZone());

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertEquals("Should start at 8 am", start, absence.getStartDate());
        Assert.assertEquals("Should end at 12 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureCorrectTimeForNoonAbsence() {

        Instant today = LocalDate.from(Instant.now(clock)).atStartOfDay(clock.getZone()).toInstant();

        ZonedDateTime start = today.with(HOUR_OF_DAY, 12).atZone(clock.getZone());
        ZonedDateTime end = today.with(HOUR_OF_DAY,16).atZone(clock.getZone());

        Period period = new Period(today, today, DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertEquals("Should start at 12 pm", start, absence.getStartDate());
        Assert.assertEquals("Should end at 4 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureIsAllDayForFullDayPeriod() {

        Instant start = Instant.now(clock);
        Instant end = start.plus(2, DAYS);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertTrue("Should be all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForMorningPeriod() {

        Instant today = Instant.now(clock);

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForNoonPeriod() {

        Instant today = Instant.now(clock);

        Period period = new Period(today, today, DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPeriod() {

        new Absence(person, null, timeConfiguration, clock);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        Period period = new Period(Instant.now(clock), Instant.now(clock), DayLength.FULL);

        new Absence(null, period, timeConfiguration, clock);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullConfiguration() {

        Period period = new Period(Instant.now(clock), Instant.now(clock), DayLength.FULL);

        new Absence(person, period, null, clock);
    }


    @Test
    public void ensureCorrectEventSubject() {

        Instant today = Instant.now(clock);
        Period period = new Period(today, today, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration, clock);

        Assert.assertNotNull("Event subject must not be null", absence.getEventSubject());
        Assert.assertEquals("Wrong event subject", "Marlene Muster abwesend", absence.getEventSubject());
    }

    @Test
    public void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));
        final Absence absence = new Absence(person, new Period(Instant.MIN, Instant.MAX.with(YEAR, 10), DayLength.FULL), new AbsenceTimeConfiguration(new CalendarSettings()), clock);

        final String absenceToString = absence.toString();
        assertThat(absenceToString).isEqualTo("Absence{startDate=-999999999-01-01T00:00Z," +
            " endDate=0011-01-01T00:00Z, person=Person{id='10'}, isAllDay=true}");
    }
}
