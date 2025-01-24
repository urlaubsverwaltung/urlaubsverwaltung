package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;

class SickDaysOverviewTest {

    @Test
    void ensureGeneratesCorrectSickDaysOverviewWithSickNoteSpanningMultipleYears() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        final LocalDate startDate = LocalDate.of(2016, 1, 1);
        final LocalDate endDate = LocalDate.of(2016, 12, 31);
        final LocalDate aubStartDate = LocalDate.of(2016, 5, 5);
        final LocalDate aubEndDate = LocalDate.of(2016, 6, 6);

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .startDate(startDate)
            .endDate(endDate)
            .aubStartDate(aubStartDate)
            .aubEndDate(aubEndDate)
            .build();

        final WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);

        final BigDecimal workingDays = BigDecimal.valueOf(209);
        when(workDaysCountService.getWorkDaysCount(FULL, startDate, endDate, person)).thenReturn(workingDays);

        final BigDecimal workingDaysAub = BigDecimal.valueOf(100);
        when(workDaysCountService.getWorkDaysCount(FULL, aubStartDate, aubEndDate, person)).thenReturn(workingDaysAub);

        final LocalDate from = LocalDate.of(2015, 1, 1);
        final LocalDate to = LocalDate.of(2017, 12, 31);
        final SickDaysOverview sickDaysOverview = new SickDaysOverview(List.of(sickNote), workDaysCountService, from, to);
        final SickDays sickDays = sickDaysOverview.getSickDays();
        assertThat(sickDays.getDays())
            .containsEntry("TOTAL", workingDays)
            .containsEntry("WITH_AUB", workingDaysAub);
    }

    @Test
    void ensureGeneratesCorrectSickDaysOverview() {

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        final SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("Kind-Krankmeldung");

        final WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);

        final SickNote sickNoteWithoutAUB = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .startDate(LocalDate.of(2014, 10, 13))
            .endDate(LocalDate.of(2014, 10, 13))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 13), LocalDate.of(2014, 10, 13), person))
            .thenReturn(ONE);

        final SickNote sickNoteWithAUB = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteType)
            .status(ACTIVE)
            .startDate(LocalDate.of(2014, 10, 14))
            .endDate(LocalDate.of(2014, 10, 16))
            .aubStartDate(LocalDate.of(2014, 10, 15))
            .aubEndDate(LocalDate.of(2014, 10, 15))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 14), LocalDate.of(2014, 10, 16), person))
            .thenReturn(BigDecimal.valueOf(3));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 15), LocalDate.of(2014, 10, 15), person))
            .thenReturn(ONE);

        final SickNote childSickNoteWithoutAUB = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteTypeChild)
            .status(ACTIVE)
            .startDate(LocalDate.of(2014, 10, 15))
            .endDate(LocalDate.of(2014, 10, 15))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 15), LocalDate.of(2014, 10, 15), person))
            .thenReturn(ONE);

        final SickNote childSickNoteWithAUB = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteTypeChild)
            .status(ACTIVE)
            .startDate(LocalDate.of(2014, 10, 16))
            .endDate(LocalDate.of(2014, 10, 18))
            .aubStartDate(LocalDate.of(2014, 10, 16))
            .aubEndDate(LocalDate.of(2014, 10, 17))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 16), LocalDate.of(2014, 10, 18), person))
            .thenReturn(BigDecimal.valueOf(3));
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 16), LocalDate.of(2014, 10, 17), person))
            .thenReturn(BigDecimal.valueOf(2));

        final SickNote inactiveSickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .sickNoteType(sickNoteTypeChild)
            .status(CANCELLED)
            .startDate(LocalDate.of(2014, 10, 17))
            .endDate(LocalDate.of(2014, 10, 17))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 17), LocalDate.of(2014, 10, 17), person))
            .thenReturn(ONE);

        final SickNote inactiveChildSickNote = SickNote.builder()
            .person(person)
            .sickNoteType(sickNoteTypeChild)
            .status(CANCELLED)
            .startDate(LocalDate.of(2014, 10, 18))
            .endDate(LocalDate.of(2014, 10, 18))
            .build();
        when(workDaysCountService.getWorkDaysCount(FULL, LocalDate.of(2014, 10, 18), LocalDate.of(2014, 10, 18), person))
            .thenReturn(ONE);

        final List<SickNote> sickNotes = List.of(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
            childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        final SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, workDaysCountService,
            LocalDate.of(2014, 10, 10), LocalDate.of(2014, 10, 18));

        final SickDays sickDays = sickDaysOverview.getSickDays();
        assertThat(sickDays.getDays())
            .isNotNull()
            .containsEntry("TOTAL", new BigDecimal("4"))
            .containsEntry("WITH_AUB", ONE);

        final SickDays childSickDays = sickDaysOverview.getChildSickDays();
        assertThat(childSickDays.getDays())
            .isNotNull()
            .containsEntry("TOTAL", new BigDecimal("4"))
            .containsEntry("WITH_AUB", new BigDecimal("2"));
    }
}
