package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.WeekDay;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ExtendedSickNoteTest {

    private WorkDaysCountService workDaysCountService;

    @BeforeEach
    void setUp() {

        workDaysCountService = mock(WorkDaysCountService.class);
    }


    @Test
    void ensureCreatesCorrectExtendedSickNote() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        SickNote sickNote = TestDataCreator.createSickNote(person, LocalDate.of(2015, 3, 3),
            LocalDate.of(2015, 3, 6), DayLength.MORNING);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.TEN);

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, workDaysCountService);

        verify(workDaysCountService)
            .getWorkDaysCount(sickNote.getDayLength(), sickNote.getStartDate(), sickNote.getEndDate(), person);

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
    void ensureExtendedSickNoteHasInformationAboutDayOfWeek() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        SickNote sickNote = TestDataCreator.createSickNote(person, LocalDate.of(2016, 3, 1),
            LocalDate.of(2016, 3, 4), DayLength.FULL);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.valueOf(4));

        ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, workDaysCountService);

        Assert.assertNotNull("Missing day of week for start date", extendedSickNote.getWeekDayOfStartDate());
        Assert.assertEquals("Wrong day of week for start date", WeekDay.TUESDAY,
            extendedSickNote.getWeekDayOfStartDate());

        Assert.assertNotNull("Missing day of week for end date", extendedSickNote.getWeekDayOfEndDate());
        Assert.assertEquals("Wrong day of week for end date", WeekDay.FRIDAY, extendedSickNote.getWeekDayOfEndDate());
    }
}
