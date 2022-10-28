package org.synyx.urlaubsverwaltung.overview;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sickdays.web.SickDays;
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

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("Kind-Krankmeldung");

        SickNote sickNoteWithoutAUB = TestDataCreator.anySickNote();
        sickNoteWithoutAUB.setSickNoteType(sickNoteType);
        sickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithoutAUB.setStartDate(LocalDate.of(2014, 10, 13));
        sickNoteWithoutAUB.setEndDate(LocalDate.of(2014, 10, 13));

        SickNote sickNoteWithAUB = TestDataCreator.anySickNote();
        sickNoteWithAUB.setSickNoteType(sickNoteType);
        sickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithAUB.setStartDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setEndDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setAubStartDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setAubEndDate(LocalDate.of(2014, 10, 14));

        SickNote childSickNoteWithoutAUB = TestDataCreator.anySickNote();
        childSickNoteWithoutAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithoutAUB.setStartDate(LocalDate.of(2014, 10, 15));
        childSickNoteWithoutAUB.setEndDate(LocalDate.of(2014, 10, 15));

        SickNote childSickNoteWithAUB = TestDataCreator.anySickNote();
        childSickNoteWithAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithAUB.setStartDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setEndDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setAubStartDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setAubEndDate(LocalDate.of(2014, 10, 16));

        SickNote inactiveSickNote = TestDataCreator.anySickNote();
        inactiveSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveSickNote.setStartDate(LocalDate.of(2014, 10, 17));
        inactiveSickNote.setEndDate(LocalDate.of(2014, 10, 17));

        SickNote inactiveChildSickNote = TestDataCreator.anySickNote();
        inactiveChildSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveChildSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveChildSickNote.setStartDate(LocalDate.of(2014, 10, 18));
        inactiveChildSickNote.setEndDate(LocalDate.of(2014, 10, 18));

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
