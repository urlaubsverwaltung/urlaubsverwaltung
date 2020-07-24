package org.synyx.urlaubsverwaltung.overview;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sickdays.web.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SickDaysOverviewTest {

    private WorkDaysService calendarService;

    @BeforeEach
    void setUp() {

        calendarService = mock(WorkDaysService.class);
    }


    @Test
    void ensureGeneratesCorrectSickDaysOverview() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setMessageKey("Krankmeldung");

        SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteTypeChild.setMessageKey("Kind-Krankmeldung");

        SickNote sickNoteWithoutAUB = DemoDataCreator.anySickNote();
        sickNoteWithoutAUB.setSickNoteType(sickNoteType);
        sickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithoutAUB.setStartDate(LocalDate.of(2014, 10, 13));
        sickNoteWithoutAUB.setEndDate(LocalDate.of(2014, 10, 13));

        SickNote sickNoteWithAUB = DemoDataCreator.anySickNote();
        sickNoteWithAUB.setSickNoteType(sickNoteType);
        sickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithAUB.setStartDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setEndDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setAubStartDate(LocalDate.of(2014, 10, 14));
        sickNoteWithAUB.setAubEndDate(LocalDate.of(2014, 10, 14));

        SickNote childSickNoteWithoutAUB = DemoDataCreator.anySickNote();
        childSickNoteWithoutAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithoutAUB.setStartDate(LocalDate.of(2014, 10, 15));
        childSickNoteWithoutAUB.setEndDate(LocalDate.of(2014, 10, 15));

        SickNote childSickNoteWithAUB = DemoDataCreator.anySickNote();
        childSickNoteWithAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithAUB.setStartDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setEndDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setAubStartDate(LocalDate.of(2014, 10, 16));
        childSickNoteWithAUB.setAubEndDate(LocalDate.of(2014, 10, 16));

        SickNote inactiveSickNote = DemoDataCreator.anySickNote();
        inactiveSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveSickNote.setStartDate(LocalDate.of(2014, 10, 17));
        inactiveSickNote.setEndDate(LocalDate.of(2014, 10, 17));

        SickNote inactiveChildSickNote = DemoDataCreator.anySickNote();
        inactiveChildSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveChildSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveChildSickNote.setStartDate(LocalDate.of(2014, 10, 18));
        inactiveChildSickNote.setEndDate(LocalDate.of(2014, 10, 18));

        List<SickNote> sickNotes = Arrays.asList(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
            childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        // just return 1 day for each sick note
        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, calendarService);

        SickDays sickDays = sickDaysOverview.getSickDays();
        Assert.assertNotNull("Should not be null", sickDays.getDays());
        Assert.assertEquals("Wrong number of sick days without AUB", new BigDecimal("2"),
            sickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of sick days with AUB", BigDecimal.ONE, sickDays.getDays().get("WITH_AUB"));

        SickDays childSickDays = sickDaysOverview.getChildSickDays();
        Assert.assertNotNull("Should not be null", childSickDays.getDays());
        Assert.assertEquals("Wrong number of child sick days without AUB", new BigDecimal("2"),
            childSickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of child sick days with AUB", BigDecimal.ONE,
            childSickDays.getDays().get("WITH_AUB"));
    }

}
