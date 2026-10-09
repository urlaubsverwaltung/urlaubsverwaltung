package org.synyx.urlaubsverwaltung.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Marks "today" in a list of absences sorted descending by start date: the divider goes in front of the first entry
 * that does not start in the future, active entries spanning today are running.
 *
 * @param today          the date the marker shows, {@code null} when there is no marker
 * @param dividerIndex   index of the entry the divider is rendered in front of, the list size to render it after
 *                       the last entry, {@code -1} when there is no marker
 * @param runningIndexes indexes of the active entries spanning today
 */
public record TodayMarker(LocalDate today, int dividerIndex, Set<Integer> runningIndexes) {

    private static final TodayMarker NONE = new TodayMarker(null, -1, Set.of());

    public TodayMarker {
        runningIndexes = Set.copyOf(runningIndexes);
    }

    public static TodayMarker none() {
        return NONE;
    }

    /**
     * @param entriesNewestFirst entries sorted descending by start date
     * @param selectedYear       the year the list shows, the marker is only shown for the year of {@code today}
     */
    public static <T> TodayMarker of(
        List<T> entriesNewestFirst,
        Function<T, LocalDate> startDate,
        Function<T, LocalDate> endDate,
        Predicate<T> active,
        LocalDate today,
        int selectedYear
    ) {
        if (entriesNewestFirst.isEmpty() || today.getYear() != selectedYear) {
            return NONE;
        }

        final int dividerIndex = IntStream.range(0, entriesNewestFirst.size())
            .filter(index -> !startDate.apply(entriesNewestFirst.get(index)).isAfter(today))
            .findFirst()
            .orElse(entriesNewestFirst.size());

        final Set<Integer> runningIndexes = IntStream.range(0, entriesNewestFirst.size())
            .filter(index -> {
                final T entry = entriesNewestFirst.get(index);
                return active.test(entry) && !startDate.apply(entry).isAfter(today) && !endDate.apply(entry).isBefore(today);
            })
            .boxed()
            .collect(toUnmodifiableSet());

        return new TodayMarker(today, dividerIndex, runningIndexes);
    }

    public boolean isDividerBefore(int index) {
        return today != null && dividerIndex == index;
    }

    public boolean isRunning(int index) {
        return runningIndexes.contains(index);
    }
}
