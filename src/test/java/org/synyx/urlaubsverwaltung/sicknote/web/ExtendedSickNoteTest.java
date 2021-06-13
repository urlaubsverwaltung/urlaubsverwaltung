package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(extendedSickNote.getDayLength()).isNotNull();
        assertThat(extendedSickNote.getStartDate()).isNotNull();
        assertThat(extendedSickNote.getEndDate()).isNotNull();
        assertThat(extendedSickNote.getSickNoteType()).isNotNull();

        assertThat(extendedSickNote.getDayLength()).isEqualTo(sickNote.getDayLength());
        assertThat(extendedSickNote.getStartDate()).isEqualTo(sickNote.getStartDate());
        assertThat(extendedSickNote.getEndDate()).isEqualTo(sickNote.getEndDate());
        assertThat(extendedSickNote.getSickNoteType()).isEqualTo(sickNote.getSickNoteType());

        assertThat(extendedSickNote.getWorkDays()).isNotNull();
        assertThat(extendedSickNote.getWorkDays()).isEqualTo(BigDecimal.TEN);
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

        assertThat(extendedSickNote.getWeekDayOfStartDate()).isEqualTo(TUESDAY);
        assertThat(extendedSickNote.getWeekDayOfEndDate()).isEqualTo(FRIDAY);
    }
}
