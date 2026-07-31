package org.synyx.urlaubsverwaltung.overtime.statistics;

import java.time.Duration;
import java.time.Year;
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
