package org.synyx.urlaubsverwaltung.web.sicknote;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.WeekDay;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExtendedSickNoteTest {

    private WorkDaysService calendarService;

    @Before
    public void setUp() {

        calendarService = Mockito.mock(WorkDaysService.class);
    }


    @Test
    public void ensureCreatesCorrectExtendedSickNote() {

        Person person = TestDataCreator.createPerson();

        SickNote sickNote = TestDataCreator.createSickNote(person, new DateMidnight(2015, 3, 3),
                new DateMidnight(2015, 3, 6), DayLength.MORNING);

        when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(BigDecimal.TEN);

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, calendarService);

        verify(calendarService)
            .getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(), sickNote.getEndDate(), person);

        Assert.assertNotNull("Should not be null", extendedSickNote.getDayLength());
        Assert.assertNotNull("Should not be null", extendedSickNote.getStartDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getEndDate());
        Assert.assertNotNull("Should not be null", extendedSickNote.getSickNoteType());

        Assert.assertEquals("Wrong day length", sickNote.getDayLength(), extendedSickNote.getDayLength());
        Assert.assertEquals("Wrong start date", sickNote.getStartDate(), extendedSickNote.getStartDate());
        Assert.assertEquals("Wrong end date", sickNote.getEndDate(), extendedSickNote.getEndDate());
        Assert.assertEquals("Wrong type", sickNote.getSickNoteType(), extendedSickNote.getSickNoteType());

        Assert.assertNotNull("Should not be null", extendedSickNote.getWorkDays());
        Assert.assertEquals("Wrong number of work days", BigDecimal.TEN, extendedSickNote.getWorkDays());
    }


    @Test
    public void ensureExtendedSickNoteHasInformationAboutDayOfWeek() {

        Person person = TestDataCreator.createPerson();

        SickNote sickNote = TestDataCreator.createSickNote(person, new DateMidnight(2016, 3, 1),
                new DateMidnight(2016, 3, 4), DayLength.FULL);

        when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(BigDecimal.valueOf(4));

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, calendarService);

        Assert.assertNotNull("Missing day of week for start date", extendedSickNote.getWeekDayOfStartDate());
        Assert.assertEquals("Wrong day of week for start date", WeekDay.TUESDAY,
            extendedSickNote.getWeekDayOfStartDate());

        Assert.assertNotNull("Missing day of week for end date", extendedSickNote.getWeekDayOfEndDate());
        Assert.assertEquals("Wrong day of week for end date", WeekDay.FRIDAY, extendedSickNote.getWeekDayOfEndDate());
    }
}
