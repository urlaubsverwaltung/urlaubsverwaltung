package org.synyx.urlaubsverwaltung.sicknote.me;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MAY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

class YearlySickDaysSummaryTest {

    @Test
    void ensureGeneratesCorrectSickDaysOverviewWithSickNoteSpanningMultipleYears() {

        final LocalDate from = LocalDate.of(2015, JANUARY, 1);
        final LocalDate to = LocalDate.of(2017, DECEMBER, 31);

        final SickNote sickNote = SickNote.builder()
            .dayLength(FULL)
            .sickNoteType(sickNoteType(SICK_NOTE))
            .status(ACTIVE)
            .startDate(LocalDate.of(2016, JANUARY, 1))
            .endDate(LocalDate.of(2016, DECEMBER, 31))
            .aubStartDate(LocalDate.of(2016, MAY, 5))
            .aubEndDate(LocalDate.of(2016, JUNE, 6))
            .workingTimeCalendar(workingEveryDay(from, to))
            .build();

        final YearlySickDaysSummary sut = new YearlySickDaysSummary(List.of(sickNote), from, to);

        assertThat(sut.getSickDays().getDays().get("TOTAL")).isEqualByComparingTo("366");
        assertThat(sut.getSickDays().getDays().get("WITH_AUB")).isEqualByComparingTo("33");
    }

    @Test
    void ensureGeneratesCorrectSickDaysOverviewWithSickNoteSpanningMultipleYearsButAubEndsInLastYear() {

        final LocalDate from = LocalDate.of(2026, JANUARY, 1);
        final LocalDate to = LocalDate.of(2026, DECEMBER, 31);

        final LocalDate sickNoteStartDate = LocalDate.of(2025, DECEMBER, 29);
        final SickNote sickNote = SickNote.builder()
            .dayLength(FULL)
            .sickNoteType(sickNoteType(SICK_NOTE))
            .status(ACTIVE)
            // extend beyond turn of the year (actually it just has to extend the requested interval below)
            .startDate(sickNoteStartDate)
            .endDate(LocalDate.of(2026, JANUARY, 9))
            // aub must end before the start of the requested interval below
            .aubStartDate(sickNoteStartDate)
            .aubEndDate(LocalDate.of(2025, DECEMBER, 31))
            .workingTimeCalendar(workingEveryDay(from, to))
            .build();

        final YearlySickDaysSummary sut = new YearlySickDaysSummary(List.of(sickNote), from, to);

        assertThat(sut.getSickDays().getDays().get("TOTAL")).isEqualByComparingTo("9");
        assertThat(sut.getSickDays().getDays().get("WITH_AUB")).isEqualByComparingTo("0");
    }

    @Test
    void ensureGeneratesCorrectSickDaysOverview() {

        final LocalDate from = LocalDate.of(2014, OCTOBER, 10);
        final LocalDate to = LocalDate.of(2014, OCTOBER, 18);
        final WorkingTimeCalendar workingTimeCalendar = workingEveryDay(from, to);

        final SickNote sickNoteWithoutAUB = sickNote(SICK_NOTE, ACTIVE, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 13), LocalDate.of(2014, OCTOBER, 13), null, null);
        final SickNote sickNoteWithAUB = sickNote(SICK_NOTE, ACTIVE, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 14), LocalDate.of(2014, OCTOBER, 16),
            LocalDate.of(2014, OCTOBER, 15), LocalDate.of(2014, OCTOBER, 15));
        final SickNote childSickNoteWithoutAUB = sickNote(SICK_NOTE_CHILD, ACTIVE, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 15), LocalDate.of(2014, OCTOBER, 15), null, null);
        final SickNote childSickNoteWithAUB = sickNote(SICK_NOTE_CHILD, ACTIVE, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 16), LocalDate.of(2014, OCTOBER, 18),
            LocalDate.of(2014, OCTOBER, 16), LocalDate.of(2014, OCTOBER, 17));
        final SickNote inactiveSickNote = sickNote(SICK_NOTE, CANCELLED, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 17), LocalDate.of(2014, OCTOBER, 17), null, null);
        final SickNote inactiveChildSickNote = sickNote(SICK_NOTE_CHILD, CANCELLED, workingTimeCalendar,
            LocalDate.of(2014, OCTOBER, 18), LocalDate.of(2014, OCTOBER, 18), null, null);

        final List<SickNote> sickNotes = List.of(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
            childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        final YearlySickDaysSummary sut = new YearlySickDaysSummary(sickNotes, from, to);

        final SickDays sickDays = sut.getSickDays();
        assertThat(sickDays.getDays().get("TOTAL")).isEqualByComparingTo("4");
        assertThat(sickDays.getDays().get("WITH_AUB")).isEqualByComparingTo("1");

        final SickDays childSickDays = sut.getChildSickDays();
        assertThat(childSickDays.getDays().get("TOTAL")).isEqualByComparingTo("4");
        assertThat(childSickDays.getDays().get("WITH_AUB")).isEqualByComparingTo("2");
    }

    @ParameterizedTest
    @CsvSource({"MORNING, 0.5", "NOON, 0"})
    void ensureCountsOnlyTheWorkedHalfOfChristmasEve(DayLength sickNoteDayLength, String expectedDays) {

        final LocalDate christmasEve = LocalDate.of(2026, DECEMBER, 24);
        // christmas eve with a working morning and a free afternoon
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(
            Map.of(christmasEve, new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY)));

        final SickNote sickNote = SickNote.builder()
            .dayLength(sickNoteDayLength)
            .sickNoteType(sickNoteType(SICK_NOTE))
            .status(ACTIVE)
            .startDate(christmasEve)
            .endDate(christmasEve)
            .aubStartDate(christmasEve)
            .aubEndDate(christmasEve)
            .workingTimeCalendar(workingTimeCalendar)
            .build();

        final YearlySickDaysSummary sut = new YearlySickDaysSummary(List.of(sickNote),
            LocalDate.of(2026, JANUARY, 1), LocalDate.of(2026, DECEMBER, 31));

        assertThat(sut.getSickDays().getDays().get("TOTAL")).isEqualByComparingTo(expectedDays);
        assertThat(sut.getSickDays().getDays().get("WITH_AUB")).isEqualByComparingTo(expectedDays);
    }

    private static SickNote sickNote(
        SickNoteCategory category,
        SickNoteStatus status,
        WorkingTimeCalendar workingTimeCalendar,
        LocalDate startDate, LocalDate endDate, LocalDate aubStartDate, LocalDate aubEndDate
    ) {
        return SickNote.builder()
            .dayLength(FULL)
            .sickNoteType(sickNoteType(category))
            .status(status)
            .startDate(startDate)
            .endDate(endDate)
            .aubStartDate(aubStartDate)
            .aubEndDate(aubEndDate)
            .workingTimeCalendar(workingTimeCalendar)
            .build();
    }

    private static SickNoteType sickNoteType(SickNoteCategory category) {
        final SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(category);
        sickNoteType.setMessageKey(category.name());
        return sickNoteType;
    }

    private static WorkingTimeCalendar workingEveryDay(LocalDate from, LocalDate to) {
        final Map<LocalDate, WorkingDayInformation> workingDays = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            workingDays.put(date, new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        }
        return new WorkingTimeCalendar(workingDays);
    }
}
