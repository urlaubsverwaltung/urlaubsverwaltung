package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SickDaysOverviewTest {

    @Test
    void ensureGeneratesCorrectSickDaysOverview() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("Kind-Krankmeldung");

        final SickNote sickNoteWithoutAUB = SickNote.builder()
                .person(person)
                .dayLength(DayLength.FULL)
                .sickNoteType(sickNoteType)
                .status(SickNoteStatus.ACTIVE)
                .startDate(LocalDate.of(2014, 10, 13))
                .endDate(LocalDate.of(2014, 10, 13))
                .build();

        final SickNote sickNoteWithAUB = SickNote.builder()
                .person(person)
                .dayLength(DayLength.FULL)
                .sickNoteType(sickNoteType)
                .status(SickNoteStatus.ACTIVE)
                .startDate(LocalDate.of(2014, 10, 14))
                .endDate(LocalDate.of(2014, 10, 14))
                .aubStartDate(LocalDate.of(2014, 10, 14))
                .aubEndDate(LocalDate.of(2014, 10, 14))
                .build();

        final SickNote childSickNoteWithoutAUB = SickNote.builder()
                .person(person)
                .dayLength(DayLength.FULL)
                .sickNoteType(sickNoteTypeChild)
                .status(SickNoteStatus.ACTIVE)
                .startDate(LocalDate.of(2014, 10, 15))
                .endDate(LocalDate.of(2014, 10, 15))
                .build();

        final SickNote childSickNoteWithAUB = SickNote.builder()
                .person(person)
                .dayLength(DayLength.FULL)
                .sickNoteType(sickNoteTypeChild)
                .status(SickNoteStatus.ACTIVE)
                .startDate(LocalDate.of(2014, 10, 16))
                .endDate(LocalDate.of(2014, 10, 16))
                .aubStartDate(LocalDate.of(2014, 10, 16))
                .aubEndDate(LocalDate.of(2014, 10, 16))
                .build();

        final SickNote inactiveSickNote = SickNote.builder()
                .person(person)
                .dayLength(DayLength.FULL)
                .sickNoteType(sickNoteTypeChild)
                .status(SickNoteStatus.CANCELLED)
                .startDate(LocalDate.of(2014, 10, 17))
                .endDate(LocalDate.of(2014, 10, 17))
                .build();

        final SickNote inactiveChildSickNote = SickNote.builder()
                .person(person)
                .sickNoteType(sickNoteTypeChild)
                .status(SickNoteStatus.CANCELLED)
                .startDate(LocalDate.of(2014, 10, 18))
                .endDate(LocalDate.of(2014, 10, 18))
                .build();

        final List<SickNote> sickNotes = List.of(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
            childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        final WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);

        // just return 1 day for each sick note
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        final SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, workDaysCountService);

        final SickDays sickDays = sickDaysOverview.getSickDays();
        assertThat(sickDays.getDays())
            .isNotNull()
            .containsEntry("TOTAL", new BigDecimal("2"))
            .containsEntry("WITH_AUB", BigDecimal.ONE);

        final SickDays childSickDays = sickDaysOverview.getChildSickDays();
        assertThat(childSickDays.getDays())
            .isNotNull()
            .containsEntry("TOTAL", new BigDecimal("2"))
            .containsEntry("WITH_AUB", BigDecimal.ONE);
    }
}
