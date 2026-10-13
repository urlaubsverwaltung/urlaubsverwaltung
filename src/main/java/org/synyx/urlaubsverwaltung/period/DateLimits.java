package org.synyx.urlaubsverwaltung.period;

import org.synyx.urlaubsverwaltung.absence.DateRange;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Limits for date ranges and dates.
 * <p>
 * Working time calendars and public holidays are calculated for every single day or year of a date range.
 * Without a limit a single request with a date range of millions of years exhausts the memory.
 * The same applies to stored dates far in the past or the future, every later read calculates up to them.
 */
public final class DateLimits {

    /**
     * Maximum duration of a date range requested by a client: three calendar years, including leap days.
     */
    public static final Duration MAXIMUM_REQUESTED_DURATION = Duration.ofDays(3 * 366L);

    /**
     * Maximum duration of a date range that is calculated internally: one hundred years, including leap days.
     * <p>
     * The largest internal date range is the calendar period 'all', one hundred years back from today.
     * Date ranges derived from stored data span the age of the installation.
     */
    public static final Duration MAXIMUM_CALCULATED_DURATION = Duration.ofDays(100 * 366L);

    /**
     * Maximum period a stored date may lie in the future, e.g. the end of a sick note or of overtime.
     */
    public static final java.time.Period MAXIMUM_PERIOD_INTO_THE_FUTURE = java.time.Period.ofYears(2);

    /**
     * Maximum period a stored date may lie in the past, e.g. the start of a sick note or of overtime.
     */
    public static final java.time.Period MAXIMUM_PERIOD_INTO_THE_PAST = java.time.Period.ofYears(10);

    private DateLimits() {
    }

    /**
     * @param date to check
     * @param clock to get today from
     * @return true if the date lies more than {@link #MAXIMUM_PERIOD_INTO_THE_FUTURE} after today, false otherwise
     */
    public static boolean isTooFarInTheFuture(LocalDate date, Clock clock) {
        return date.isAfter(LocalDate.now(clock).plus(MAXIMUM_PERIOD_INTO_THE_FUTURE));
    }

    /**
     * @param date to check
     * @param clock to get today from
     * @return true if the date lies more than {@link #MAXIMUM_PERIOD_INTO_THE_PAST} before today, false otherwise
     */
    public static boolean isTooFarInThePast(LocalDate date, Clock clock) {
        return date.isBefore(LocalDate.now(clock).minus(MAXIMUM_PERIOD_INTO_THE_PAST));
    }

    /**
     * @param startDate start of the date range, inclusive
     * @param endDate end of the date range, inclusive, must not be before startDate
     * @return true if the date range is longer than {@link #MAXIMUM_REQUESTED_DURATION}, false otherwise
     */
    public static boolean exceedsMaximumRequestedDuration(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate).duration().compareTo(MAXIMUM_REQUESTED_DURATION) > 0;
    }

    /**
     * @param dateRange to check
     * @throws IllegalArgumentException if the date range is longer than {@link #MAXIMUM_CALCULATED_DURATION}
     */
    public static void requireCalculableDuration(DateRange dateRange) {
        if (dateRange.duration().compareTo(MAXIMUM_CALCULATED_DURATION) > 0) {
            throw new IllegalArgumentException("Date range " + dateRange + " exceeds the maximum duration of " + MAXIMUM_CALCULATED_DURATION.toDays() + " days.");
        }
    }
}
