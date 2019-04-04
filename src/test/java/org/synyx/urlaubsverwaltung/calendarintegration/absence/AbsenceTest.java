package org.synyx.urlaubsverwaltung.calendarintegration.absence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.function.BiConsumer;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * Unit test for {@link Absence}.
 */
public class AbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration timeConfiguration;

    @Before
    public void setUp() {

        person = TestDataCreator.createPerson();

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

        Absence absence = new Absence(person, period, EventType.WAITING_APPLICATION, timeConfiguration);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());
        Assert.assertNotNull("Event type must not be null", absence.getEventType());

        Assert.assertEquals("Wrong start date", start.atStartOfDay(UTC), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.atStartOfDay(UTC).plusDays(1), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
        Assert.assertEquals("Wrong event type", EventType.WAITING_APPLICATION, absence.getEventType());
    }


    @Test
    public void ensureCanBeInstantiatedWithCorrectPropertiesConsideringDaylightSavingTime() {

        // Date where daylight saving time is relevant
        LocalDate start = LocalDate.of(2015, 10, 23);
        LocalDate end = LocalDate.of(2015, 10, 25);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());
        Assert.assertNotNull("Event type must not be null", absence.getEventType());

        Assert.assertEquals("Wrong start date", start.atStartOfDay(UTC), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.atStartOfDay(UTC).plusDays(1), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
        Assert.assertEquals("Wrong event type", EventType.ALLOWED_APPLICATION, absence.getEventType());
    }


    @Test
    public void ensureCorrectTimeForMorningAbsence() {

        LocalDateTime today = LocalDate.now(UTC).atStartOfDay();

        ZonedDateTime start = today.withHour(8).atZone(UTC);
        ZonedDateTime end = today.withHour(12).atZone(UTC);

        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.MORNING);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertEquals("Should start at 8 am", start, absence.getStartDate());
        Assert.assertEquals("Should end at 12 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureCorrectTimeForNoonAbsence() {

        LocalDateTime today = LocalDate.now(UTC).atStartOfDay();

        ZonedDateTime start = today.withHour(12).atZone(UTC);
        ZonedDateTime end = today.withHour(16).atZone(UTC);

        Period period = new Period(today.toLocalDate(), today.toLocalDate(), DayLength.NOON);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertEquals("Should start at 12 pm", start, absence.getStartDate());
        Assert.assertEquals("Should end at 4 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureIsAllDayForFullDayPeriod() {

        LocalDate start = LocalDate.now(UTC);
        LocalDate end = start.plusDays(2);

        Period period = new Period(start, end, DayLength.FULL);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertTrue("Should be all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForMorningPeriod() {

        LocalDate today = LocalDate.now(UTC);

        Period period = new Period(today, today, DayLength.MORNING);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayForNoonPeriod() {

        LocalDate today = LocalDate.now(UTC);

        Period period = new Period(today, today, DayLength.NOON);

        Absence absence = new Absence(person, period, EventType.ALLOWED_APPLICATION, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPeriod() {

        new Absence(person, null, EventType.ALLOWED_APPLICATION, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPerson() {

        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.FULL);

        new Absence(null, period, EventType.ALLOWED_APPLICATION, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEventType() {

        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.FULL);

        new Absence(person, period, null, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullConfiguration() {

        Period period = new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.FULL);

        new Absence(person, period, EventType.ALLOWED_APPLICATION, null);
    }


    @Test
    public void ensureCorrectEventSubject() {

        LocalDate today = LocalDate.now(UTC);
        Period period = new Period(today, today, DayLength.FULL);

        BiConsumer<EventType, String> assertCorrectEventSubject = (type, subject) -> {
            Absence absence = new Absence(person, period, type, timeConfiguration);

            assertThat(absence.getEventType(), is(type));
            assertThat(absence.getEventSubject(), is(subject));
        };

        assertCorrectEventSubject.accept(EventType.WAITING_APPLICATION, "Antrag auf Urlaub Marlene Muster");
        assertCorrectEventSubject.accept(EventType.ALLOWED_APPLICATION, "Urlaub Marlene Muster");
        assertCorrectEventSubject.accept(EventType.SICKNOTE, "Marlene Muster krank");
    }
}
