package org.synyx.urlaubsverwaltung.overtime.statistics;

import java.time.Duration;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.time.Duration.ZERO;
import static java.util.Collections.nCopies;

/**
 * Company wide overtime figures of a single year, broken down by month.
 *
 * <p>
 * Accrual and reduction are kept apart on purpose: a month with ten hours accrued and ten hours reduced is not the
 * same as a month where nothing happened, even though both have a balance of zero.
 *
 * @param year             the year these figures belong to
 * @param accruedByMonth   accrued overtime per month, january first, always twelve entries, never negative
 * @param reductionByMonth reduced overtime per month, january first, always twelve entries, given as a positive amount
 */
record OvertimeStatistics(Year year, List<Duration> accruedByMonth, List<Duration> reductionByMonth) {

    static final int MONTHS_PER_YEAR = 12;

    static OvertimeStatistics empty(Year year) {
        return new OvertimeStatistics(year, nCopies(MONTHS_PER_YEAR, ZERO), nCopies(MONTHS_PER_YEAR, ZERO));
    }

    /**
     * @return balance per month, january first, negative for a month with more reduction than accrual
     */
    List<Duration> balanceByMonth() {
        return IntStream.range(0, MONTHS_PER_YEAR)
            .mapToObj(month -> accruedByMonth.get(month).minus(reductionByMonth.get(month)))
            .toList();
    }

    /**
     * The balance added up month by month, january first. Starts at the balance of january, not at a carry over from
     * earlier years - this is the movement within the year, not the stock.
     *
     * @return cumulated balance per month, the last entry equals {@link #balance()}
     */
    List<Duration> cumulativeBalanceByMonth() {

        final List<Duration> cumulative = new ArrayList<>(MONTHS_PER_YEAR);

        Duration running = ZERO;
        for (Duration balanceOfMonth : balanceByMonth()) {
            running = running.plus(balanceOfMonth);
            cumulative.add(running);
        }

        return List.copyOf(cumulative);
    }

    /**
     * @return {@code true} when nothing at all happened in this year, neither accrual nor reduction
     */
    boolean hasNoOvertime() {
        return accrued().isZero() && reduction().isZero();
    }

    Duration accrued() {
        return sum(accruedByMonth);
    }

    Duration reduction() {
        return sum(reductionByMonth);
    }

    Duration balance() {
        return accrued().minus(reduction());
    }

    private static Duration sum(List<Duration> durations) {
        return durations.stream().reduce(ZERO, Duration::plus);
    }
}
