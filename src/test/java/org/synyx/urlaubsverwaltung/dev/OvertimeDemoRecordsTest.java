package org.synyx.urlaubsverwaltung.dev;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.dev.OvertimeDemoRecords.Entry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDemoRecordsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Test
    void ensureEveryMonthOfThePreviousYearIsCovered() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        final Set<YearMonth> monthsOfPreviousYear = entries.stream()
            .map(entry -> YearMonth.from(entry.startDate()))
            .filter(month -> month.getYear() == 2025)
            .collect(toSet());

        assertThat(monthsOfPreviousYear).hasSize(12);
    }

    @Test
    void ensureEveryMonthOfTheCurrentYearUpToTodayIsCovered() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        final Set<YearMonth> monthsOfCurrentYear = entries.stream()
            .map(entry -> YearMonth.from(entry.startDate()))
            .filter(month -> month.getYear() == 2026)
            .collect(toSet());

        // january up to and including july, the month of TODAY
        assertThat(monthsOfCurrentYear).hasSize(7);
    }

    @Test
    void ensureNothingIsCreatedInTheFuture() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        assertThat(entries).isNotEmpty().allSatisfy(entry -> {
            assertThat(entry.startDate()).isBeforeOrEqualTo(TODAY);
            assertThat(entry.endDate()).isBeforeOrEqualTo(TODAY);
        });
    }

    @Test
    void ensureNothingIsCreatedBeforeThePreviousYear() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        assertThat(entries).allSatisfy(entry ->
            assertThat(entry.startDate()).isAfterOrEqualTo(LocalDate.of(2025, 1, 1)));
    }

    @Test
    void ensureBothAccrualAndReductionArePresent() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        assertThat(entries).anySatisfy(entry -> assertThat(entry.duration()).isPositive());
        assertThat(entries).anySatisfy(entry -> assertThat(entry.duration()).isNegative());
    }

    @Test
    void ensureNoRecordHasAZeroDuration() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        assertThat(entries).allSatisfy(entry -> assertThat(entry.duration()).isNotEqualTo(Duration.ZERO));
    }

    @Test
    void ensureRangeIsNeverInverted() {

        final List<Entry> entries = OvertimeDemoRecords.of(1L, TODAY);

        assertThat(entries).allSatisfy(entry ->
            assertThat(entry.endDate()).isAfterOrEqualTo(entry.startDate()));
    }

    @Test
    void ensureDifferentPersonsGetDifferentFigures() {

        final List<Duration> marie = OvertimeDemoRecords.of(1L, TODAY).stream().map(Entry::duration).toList();
        final List<Duration> klaus = OvertimeDemoRecords.of(2L, TODAY).stream().map(Entry::duration).toList();

        assertThat(marie).isNotEqualTo(klaus);
    }

    @Test
    void ensureSamePersonAlwaysGetsTheSameFiguresSoScreenshotsStayComparable() {

        assertThat(OvertimeDemoRecords.of(1L, TODAY)).isEqualTo(OvertimeDemoRecords.of(1L, TODAY));
    }

    @Test
    void ensureTheMonthOfTodayIsCutOffAtToday() {

        // the accrual week starts on the 8th, today is the 10th
        final List<Entry> entries = OvertimeDemoRecords.of(1L, LocalDate.of(2026, 7, 10));

        assertThat(entries).filteredOn(entry -> entry.startDate().equals(LocalDate.of(2026, 7, 8)))
            .singleElement()
            .satisfies(entry -> assertThat(entry.endDate()).isEqualTo(LocalDate.of(2026, 7, 10)));
    }

    @Test
    void ensureAMonthThatHasNotStartedYetIsSkipped() {

        // first day of the month, the accrual week on the 8th has not happened yet
        final List<Entry> entries = OvertimeDemoRecords.of(1L, LocalDate.of(2026, 7, 1));

        assertThat(entries).noneSatisfy(entry ->
            assertThat(YearMonth.from(entry.startDate())).isEqualTo(YearMonth.of(2026, 7)));
    }
}
