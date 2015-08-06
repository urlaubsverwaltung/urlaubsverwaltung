package org.synyx.urlaubsverwaltung.core.sync;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;


/**
 * Unit test for {@link Absence}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AbsenceTest {

    private Person person;

    @Before
    public void setUp() {

        person = new Person("foo", "Muster", "Marlene", "muster@muster.de");
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

        Absence absence = new Absence(application);

        Assert.assertNotNull("Start date must not be null", absence.getStartDate());
        Assert.assertNotNull("End date must not be null", absence.getEndDate());
        Assert.assertNotNull("Person must not be null", absence.getPerson());

        Assert.assertEquals("Wrong start date", start.toDate(), absence.getStartDate());
        Assert.assertEquals("Wrong end date", end.toDate(), absence.getEndDate());
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

        Absence absence = new Absence(application);

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

        Absence absence = new Absence(application);

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

        Absence absence = new Absence(application);

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

        Absence absence = new Absence(application);

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

        Absence absence = new Absence(application);

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
        Assert.assertEquals("Wrong end date", end.toDate(), absence.getEndDate());
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

        new Absence(application);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionOnZeroDayLength() {

        DateMidnight today = DateMidnight.now();

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(today);
        application.setEndDate(today);

        application.setHowLong(DayLength.ZERO);

        new Absence(application);
    }
}
