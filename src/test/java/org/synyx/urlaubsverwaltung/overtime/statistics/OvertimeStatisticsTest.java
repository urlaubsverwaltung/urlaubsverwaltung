package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Year;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;

class OvertimeStatisticsTest {

    private static final Year YEAR = Year.of(2026);

    @Test
    void ensureCumulativeBalanceStartsAtTheBalanceOfJanuaryAndNotAtACarryOver() {

        final OvertimeStatistics statistics = statistics(
            months(Duration.ofHours(5)),
            months(ZERO)
        );

        assertThat(statistics.cumulativeBalanceByMonth().get(0)).isEqualTo(Duration.ofHours(5));
    }

    @Test
    void ensureCumulativeBalanceAddsUpMonthByMonth() {

        final OvertimeStatistics statistics = statistics(
            months(Duration.ofHours(5)),
            months(Duration.ofHours(2))
        );

        final List<Duration> cumulative = statistics.cumulativeBalanceByMonth();

        assertThat(cumulative).hasSize(12);
        assertThat(cumulative.get(0)).isEqualTo(Duration.ofHours(3));
        assertThat(cumulative.get(1)).isEqualTo(Duration.ofHours(6));
        assertThat(cumulative.get(11)).isEqualTo(Duration.ofHours(36));
    }

    @Test
    void ensureTheLastCumulativeValueIsTheBalanceOfTheYear() {

        final OvertimeStatistics statistics = statistics(
            months(Duration.ofMinutes(150)),
            months(Duration.ofHours(2))
        );

        assertThat(statistics.cumulativeBalanceByMonth().get(11)).isEqualTo(statistics.balance());
    }

    @Test
    void ensureCumulativeBalanceCanGoNegativeAndRecover() {

        final OvertimeStatistics statistics = statistics(
            List.of(ZERO, ZERO, Duration.ofHours(20), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO),
            List.of(Duration.ofHours(4), Duration.ofHours(6), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO)
        );

        final List<Duration> cumulative = statistics.cumulativeBalanceByMonth();

        assertThat(cumulative.get(0)).isEqualTo(Duration.ofHours(4).negated());
        assertThat(cumulative.get(1)).isEqualTo(Duration.ofHours(10).negated());
        assertThat(cumulative.get(2)).isEqualTo(Duration.ofHours(10));
    }

    @Test
    void ensureEmptyYearIsZeroInEveryMonth() {
        assertThat(OvertimeStatistics.empty(YEAR).cumulativeBalanceByMonth()).hasSize(12).containsOnly(ZERO);
    }

    @Test
    void ensureAYearWithoutAnyMovementIsRecognisable() {

        assertThat(OvertimeStatistics.empty(YEAR).hasNoOvertime()).isTrue();
        assertThat(statistics(months(Duration.ofHours(1)), months(ZERO)).hasNoOvertime()).isFalse();
        assertThat(statistics(months(ZERO), months(Duration.ofHours(1))).hasNoOvertime()).isFalse();
    }

    private static OvertimeStatistics statistics(List<Duration> accrued, List<Duration> reduction) {
        return new OvertimeStatistics(YEAR, accrued, reduction);
    }

    private static List<Duration> months(Duration duration) {
        return nCopies(12, duration);
    }
}
