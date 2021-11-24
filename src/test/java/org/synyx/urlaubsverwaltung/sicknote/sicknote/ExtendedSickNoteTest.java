package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;

@ExtendWith(MockitoExtension.class)
class ExtendedSickNoteTest {

    @Mock
    private WorkDaysCountService workDaysCountService;

    @Test
    void ensureCreatesCorrectExtendedSickNote() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final SickNote sickNote = createSickNote(person, LocalDate.of(2015, 3, 3),
            LocalDate.of(2015, 3, 6), MORNING);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.TEN);

        final ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, workDaysCountService);
        verify(workDaysCountService).getWorkDaysCount(sickNote.getDayLength(), sickNote.getStartDate(), sickNote.getEndDate(), person);

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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 3, 1),
            LocalDate.of(2016, 3, 4), DayLength.FULL);

        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.valueOf(4));

        final ExtendedSickNote extendedSickNote = new ExtendedSickNote(sickNote, workDaysCountService);
        assertThat(extendedSickNote.getWeekDayOfStartDate()).isEqualTo(TUESDAY);
        assertThat(extendedSickNote.getWeekDayOfEndDate()).isEqualTo(FRIDAY);
    }
}
