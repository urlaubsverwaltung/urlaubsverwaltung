package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TodayMarkerTest {

    private static final LocalDate TODAY = LocalDate.parse("2026-10-01");

    private record Entry(LocalDate start, LocalDate end, boolean active) {
    }

    private static Entry entry(String start, String end) {
        return new Entry(LocalDate.parse(start), LocalDate.parse(end), true);
    }

    private static TodayMarker markerFor(List<Entry> entries, int selectedYear) {
        return TodayMarker.of(entries, Entry::start, Entry::end, Entry::active, TODAY, selectedYear);
    }

    @Test
    void dividerInFrontOfTheFirstEntryThatDoesNotStartInTheFuture() {
        final List<Entry> entries = List.of(
            entry("2026-12-21", "2026-12-31"),
            entry("2026-10-12", "2026-10-16"),
            entry("2026-09-28", "2026-10-02"),
            entry("2026-08-03", "2026-08-14")
        );

        assertThat(markerFor(entries, 2026)).isEqualTo(new TodayMarker(TODAY, 2, Set.of(2)));
    }

    @Test
    void dividerAfterLastEntryWhenAllEntriesAreUpcoming() {
        final List<Entry> entries = List.of(
            entry("2026-12-21", "2026-12-31"),
            entry("2026-10-12", "2026-10-16")
        );

        final TodayMarker marker = markerFor(entries, 2026);

        assertThat(marker).isEqualTo(new TodayMarker(TODAY, 2, Set.of()));
        assertThat(marker.isDividerBefore(2)).isTrue();
        assertThat(marker.isDividerBefore(0)).isFalse();
    }

    @Test
    void dividerOnTopWhenAllEntriesArePast() {
        final List<Entry> entries = List.of(
            entry("2026-09-14", "2026-09-16"),
            entry("2026-03-09", "2026-03-13")
        );

        assertThat(markerFor(entries, 2026)).isEqualTo(new TodayMarker(TODAY, 0, Set.of()));
    }

    @Test
    void entryStartingTodayIsRunningAndBelowTheDivider() {
        final List<Entry> entries = List.of(
            entry("2026-10-12", "2026-10-16"),
            entry("2026-10-01", "2026-10-02")
        );

        final TodayMarker marker = markerFor(entries, 2026);

        assertThat(marker).isEqualTo(new TodayMarker(TODAY, 1, Set.of(1)));
        assertThat(marker.isRunning(1)).isTrue();
        assertThat(marker.isRunning(0)).isFalse();
    }

    @Test
    void entryEndingTodayIsRunning() {
        final List<Entry> entries = List.of(entry("2026-09-28", "2026-10-01"));

        assertThat(markerFor(entries, 2026)).isEqualTo(new TodayMarker(TODAY, 0, Set.of(0)));
    }

    @Test
    void inactiveEntrySpanningTodayIsNotRunning() {
        final List<Entry> entries = List.of(new Entry(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-10-02"), false));

        assertThat(markerFor(entries, 2026)).isEqualTo(new TodayMarker(TODAY, 0, Set.of()));
    }

    @Test
    void noMarkerForAnotherYear() {
        final List<Entry> entries = List.of(entry("2025-08-04", "2025-08-08"));

        assertThat(markerFor(entries, 2025)).isEqualTo(TodayMarker.none());
        assertThat(markerFor(List.of(entry("2027-01-04", "2027-01-08")), 2027)).isEqualTo(TodayMarker.none());
    }

    @Test
    void noMarkerForAnEmptyList() {
        assertThat(markerFor(List.of(), 2026)).isEqualTo(TodayMarker.none());
    }

    @Test
    void runningIndexesAreCopiedOnCreation() {
        final Set<Integer> runningIndexes = new HashSet<>(Set.of(0));

        final TodayMarker marker = new TodayMarker(TODAY, 0, runningIndexes);
        runningIndexes.add(1);

        assertThat(marker.isRunning(1)).isFalse();
    }

    @Test
    void runningIndexesAreRequired() {
        assertThatNullPointerException().isThrownBy(() -> new TodayMarker(TODAY, 0, null));
    }

    @Test
    void noneHasNoDividerAndNothingRunning() {
        final TodayMarker none = TodayMarker.none();

        assertThat(none.isDividerBefore(0)).isFalse();
        assertThat(none.isDividerBefore(-1)).isFalse();
        assertThat(none.isRunning(0)).isFalse();
    }
}
