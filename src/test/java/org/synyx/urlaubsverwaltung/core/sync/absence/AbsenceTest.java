package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;


/**
 * Unit test for {@link Absence}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AbsenceTest {

    private Person person;
    private AbsenceTimeConfiguration timeConfiguration;

    @Before
    public void setUp() {

        person = new Person("foo", "Muster", "Marlene", "muster@muster.de");
        timeConfiguration = new AbsenceTimeConfiguration(8, 12, 13, 17);
    }


    @Test
    public void ensureCanBeInstantiatedWithAnApplication() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = start.plusDays(2);

        Application application = new Application();
        application.setStartDate(start);
        application.setEndDate(end);
        application.setPerson(person);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", start.toDate(), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.plusDays(1).toDate(), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
    }


    @Test
    public void ensureCorrectTimeForMorningAbsence() {

        DateMidnight today = DateMidnight.now();

        Date start = today.toDateTime().withHourOfDay(8).toDate();
        Date end = today.toDateTime().withHourOfDay(12).toDate();

        Application application = new Application();
        application.setHowLong(DayLength.MORNING);
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertEquals("Should start at 8 am", start, absence.getStartDate());
        Assert.assertEquals("Should end at 12 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureCorrectTimeForNoonAbsence() {

        DateMidnight today = DateMidnight.now();

        Date start = today.toDateTime().withHourOfDay(13).toDate();
        Date end = today.toDateTime().withHourOfDay(17).toDate();

        Application application = new Application();
        application.setHowLong(DayLength.NOON);
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertEquals("Should start at 1 pm", start, absence.getStartDate());
        Assert.assertEquals("Should end at 5 pm", end, absence.getEndDate());
    }


    @Test
    public void ensureIsAllDayIfApplicationIsFullDay() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = start.plusDays(2);

        Application application = new Application();
        application.setStartDate(start);
        application.setEndDate(end);
        application.setHowLong(DayLength.FULL);
        application.setPerson(person);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertTrue("Should be all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayIfApplicationIsInTheMorning() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = start.plusDays(2);

        Application application = new Application();
        application.setStartDate(start);
        application.setEndDate(end);
        application.setHowLong(DayLength.MORNING);
        application.setPerson(person);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test
    public void ensureIsNotAllDayIfApplicationIsAfternoons() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = start.plusDays(2);

        Application application = new Application();
        application.setStartDate(start);
        application.setEndDate(end);
        application.setHowLong(DayLength.NOON);
        application.setPerson(person);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        Assert.assertFalse("Should be not all day", absence.isAllDay());
    }


    @Test
    public void ensureCanBeInstantiatedWithASickNote() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = start.plusDays(2);

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(start);
        sickNote.setEndDate(end);
        sickNote.setPerson(person);

        Absence absence = new Absence(sickNote);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", start.toDate(), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.plusDays(1).toDate(), absence.getEndDate());
        Assert.assertEquals("Wrong person", person, absence.getPerson());
        Assert.assertTrue("Should be all day", absence.isAllDay());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonSetHowLong() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setStatus(ApplicationStatus.ALLOWED);

        new Absence(application, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnZeroDayLength() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setStatus(ApplicationStatus.ALLOWED);

        application.setHowLong(DayLength.ZERO);

        new Absence(application, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonSetApplicationStartDate() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setEndDate(today);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.ALLOWED);

        new Absence(application, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonSetApplicationEndDate() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.ALLOWED);

        new Absence(application, timeConfiguration);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonSetSickNoteStartDate() {

        DateMidnight today = DateMidnight.now();

        SickNote sickNote = new SickNote();
        sickNote.setEndDate(today);
        sickNote.setPerson(person);

        new Absence(sickNote);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonSetSickNoteEndDate() {

        DateMidnight today = DateMidnight.now();

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(today);
        sickNote.setPerson(person);

        new Absence(sickNote);
    }


    @Test
    public void ensureThatEventTypeIsSetForWaitingApplication() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.WAITING);

        Absence absence = new Absence(application, timeConfiguration);

        assertThat(absence.getEventType(), is(EventType.WAITING_APPLICATION));
        assertThat(absence.getEventSubject(), is("Antrag auf Urlaub Marlene Muster"));
    }


    @Test
    public void ensureThatEventTypeIsSetForAllowedApplication() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, timeConfiguration);

        assertThat(absence.getEventType(), is(EventType.ALLOWED_APPLICATION));
        assertThat(absence.getEventSubject(), is("Urlaub Marlene Muster"));
    }


    @Test
    public void ensureThatEventTypeIsSetForSickNote() {

        DateMidnight today = DateMidnight.now();

        SickNote sickNote = new SickNote();
        sickNote.setStartDate(today);
        sickNote.setEndDate(today);
        sickNote.setPerson(person);

        Absence absence = new Absence(sickNote);

        assertThat(absence.getEventType(), is(EventType.SICKNOTE));
        assertThat(absence.getEventSubject(), is("Marlene Muster krank"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnNonExpectedApplicationStatus() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);
        application.setHowLong(DayLength.FULL);
        application.setStatus(ApplicationStatus.CANCELLED);

        new Absence(application, timeConfiguration);
    }
}
