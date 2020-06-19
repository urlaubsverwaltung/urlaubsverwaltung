package org.synyx.urlaubsverwaltung.absence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Unit test for {@link Absence}.
 */
public class AbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration timeConfiguration;

    @Before
    public void setUp() {

        person = DemoDataCreator.createPerson();

        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setWorkDayBeginHour(8);
        calendarSettings.setWorkDayEndHour(16);

        timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
    }


    @Test
    public void ensureCanBeInstantiatedWithCorrectProperties() {

        LocalDate start = LocalDate.of(2015, 9, 21);
        LocalDate end = LocalDate.of(2015, 9, 23);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", start.atStartOfDay(UTC), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.atStartOfDay(UTC).plusDays(1), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
    }


    @Test
    public void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        LocalDate start = LocalDate.of(2015, 10, 23);
        LocalDate end = LocalDate.of(2015, 10, 25);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", start.atStartOfDay(UTC), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.atStartOfDay(UTC).plusDays(1), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
    }


    @Test
    public void ensureCorrectTimeForMorningAbsence() {

        LocalDateTime today = LocalDate.now(UTC).atStartOfDay();

        ZonedDateTime start = today.withHour(8).atZone(UTC);
        ZonedDateTime end = today.withHour(12).atZone(UTC);

        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertEquals("Should start at 8 am", start, absence.getStartDate());
        Assert.assertEquals("Should end at 12 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureCorrectTimeForNoonAbsence() {

        LocalDateTime today = LocalDate.now(UTC).atStartOfDay();

        ZonedDateTime start = today.withHour(12).atZone(UTC);
        ZonedDateTime end = today.withHour(16).atZone(UTC);

        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertEquals("Should start at 12 pm", start, absence.getStartDate());
        Assert.assertEquals("Should end at 4 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureIsAllDayForFullDayPeriod() {

        LocalDate start = LocalDate.now(UTC);
        LocalDate end = start.plusDays(2);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertTrue("Should be all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForMorningPeriod() {

        LocalDate today = LocalDate.now(UTC);

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForNoonPeriod() {

        LocalDate today = LocalDate.now(UTC);

        Period period = new Period(today, today, DayLength.NOON);

        Absence absence = new Absence(person, period, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPeriod() {

        new Absence(person, null, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.FULL);

        new Absence(null, period, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullConfiguration() {

        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.FULL);

        new Absence(person, period, null);
    }


    @Test
    public void ensureCorrectEventSubject() {

        LocalDate today = LocalDate.now(UTC);
        Period period = new Period(today, today, DayLength.FULL);

        Absence absence = new Absence(person, period, timeConfiguration);

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
        final Absence absence = new Absence(person, new Period(LocalDate.MIN, LocalDate.MAX.withYear(10), DayLength.FULL), new AbsenceTimeConfiguration(new CalendarSettings()));

        final String absenceToString = absence.toString();
        assertThat(absenceToString).isEqualTo("Absence{startDate=-999999999-01-01T00:00Z," +
            " endDate=0011-01-01T00:00Z, person=Person{id='10'}, isAllDay=true}");
    }
}
