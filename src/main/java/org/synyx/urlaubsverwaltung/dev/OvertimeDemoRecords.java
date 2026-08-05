package org.synyx.urlaubsverwaltung.dev;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.JANUARY;

/**
 * Schedule of the overtime demo records of a single person: the previous year completely and the current year up to
 * today, so the company wide statistics have something to show for both years.
 *
 * <p>
 * The figures are derived from the person id and the month instead of being drawn at random. Demo data that changes on
 * every start makes screenshots and manual comparisons useless.
 */
final class OvertimeDemoRecords {

    private static final int ACCRUAL_WEEK_START = 8;
    private static final int ACCRUAL_WEEK_END = 12;
    private static final int REDUCTION_DAY = 20;
    private static final int SECOND_ACCRUAL_DAY = 24;

    /**
     * Accrued hours per month, january first. Deliberately uneven with a peak before the summer and towards the end of
     * the year, so the chart shows a seasonal shape instead of twelve bars of the same height.
     */
    private static final int[] ACCRUAL_HOURS_BY_MONTH = {3, 4, 7, 5, 9, 10, 4, 2, 6, 8, 10, 5};

    /**
     * Reduced hours per month, january first. Concentrated in the months where time off is usually taken, so those
     * months end up with a negative balance and the bars point below the zero line.
     */
    private static final int[] REDUCTION_HOURS_BY_MONTH = {0, 2, 0, 3, 0, 0, 9, 12, 2, 0, 0, 8};

    private OvertimeDemoRecords() {
        // ok
    }

    /**
     * @param startDate first day of the record, inclusive
     * @param endDate   last day of the record, inclusive
     * @param duration  positive for accrued overtime, negative for reduced overtime
     */
    record Entry(LocalDate startDate, LocalDate endDate, Duration duration) {
    }

    static List<Entry> of(long personId, LocalDate today) {

        final List<Entry> entries = new ArrayList<>();
        final YearMonth lastMonth = YearMonth.from(today);

        YearMonth month = YearMonth.of(today.getYear() - 1, JANUARY);
        while (!month.isAfter(lastMonth)) {

            final int monthIndex = month.getMonthValue() - 1;
            // spreads the persons apart so the company sums are not just one curve multiplied
            final long personOffset = personId % 4;
            // the previous year is a bit weaker, otherwise both year curves would lie on top of each other
            final int yearOffset = month.getYear() < today.getYear() ? -1 : 0;

            // a week of accrued overtime, cut off at today in the month that is still running
            final LocalDate accrualStart = month.atDay(ACCRUAL_WEEK_START);
            if (!accrualStart.isAfter(today)) {
                final LocalDate accrualEnd = earlier(month.atDay(ACCRUAL_WEEK_END), today);
                final long accrualHours = Math.max(1L, ACCRUAL_HOURS_BY_MONTH[monthIndex] + personOffset + yearOffset);
                entries.add(new Entry(accrualStart, accrualEnd, hours(accrualHours)));
            }

            final int reductionHours = REDUCTION_HOURS_BY_MONTH[monthIndex];
            if (reductionHours > 0) {
                final LocalDate reductionDay = month.atDay(REDUCTION_DAY);
                if (!reductionDay.isAfter(today)) {
                    entries.add(new Entry(reductionDay, reductionDay, hours(reductionHours + personOffset).negated()));
                }
            }

            // a small second accrual keeps the months from being multiples of full hours
            final LocalDate secondAccrualDay = month.atDay(SECOND_ACCRUAL_DAY);
            if (!secondAccrualDay.isAfter(today)) {
                entries.add(new Entry(secondAccrualDay, secondAccrualDay, minutes(15L * (1 + ((personId + monthIndex) % 4)))));
            }

            month = month.plusMonths(1);
        }

        return List.copyOf(entries);
    }

    private static LocalDate earlier(LocalDate date, LocalDate other) {
        return date.isBefore(other) ? date : other;
    }

    private static Duration hours(long hours) {
        return Duration.ofHours(hours);
    }

    private static Duration minutes(long minutes) {
        return Duration.ofMinutes(minutes);
    }
}
