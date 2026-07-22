package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.PersonId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.DeltaTrend.DECREASE;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.DeltaTrend.INCREASE;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.DeltaTrend.NEUTRAL;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.DeltaTrend.NONE;

class OvertimeStatisticsTest {

    private static final PersonId PERSON_ID = new PersonId(1L);

    @Test
    void ensureTotalOvertimeHoursByMonth() {

        final Year year = Year.of(2024);

        final Overtime january = overtime(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 10), Duration.ofHours(5));
        final Overtime february = overtime(LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 5), Duration.ofHours(10));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 3, 15), List.of(january, february), 2);

        final List<BigDecimal> byMonth = statistics.getTotalOvertimeHoursByMonth();
        assertThat(byMonth).hasSize(12);
        assertThat(byMonth.get(0)).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(byMonth.get(1)).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(byMonth.get(2)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureDeltaToPreviousMonthHoursAndJanuaryIsNull() {

        final Year year = Year.of(2024);
        final Overtime january = overtime(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 10), Duration.ofHours(5));
        final Overtime february = overtime(LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 5), Duration.ofHours(15));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 3, 15), List.of(january, february), 2);

        final List<BigDecimal> deltas = statistics.getDeltaToPreviousMonthHours();
        assertThat(deltas.get(0)).isNull();
        assertThat(deltas.get(1)).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(deltas.get(2)).isEqualByComparingTo(BigDecimal.valueOf(-15));
    }

    @Test
    void ensureDeltaTrendIsNoneForJanuaryAndNeutralForReferenceMonth() {

        final Year year = Year.of(2024);
        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 3, 15), List.of(), 1);

        final List<OvertimeStatistics.DeltaTrend> trends = statistics.getDeltaTrend();
        assertThat(trends.get(0)).isEqualTo(NONE);
        assertThat(trends.get(2)).isEqualTo(NEUTRAL);
        assertThat(trends.get(1)).isEqualTo(NEUTRAL);
    }

    @Test
    void ensureDeltaTrendDecreaseAndIncreaseAndReferenceMonthIsAlwaysNeutral() {

        final Year year = Year.of(2024);
        final Overtime january = overtime(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 10), Duration.ofHours(10));
        final Overtime february = overtime(LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 5), Duration.ofHours(5));
        final Overtime march = overtime(LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 5), Duration.ofHours(20));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 4, 15), List.of(january, february, march), 1);

        final List<OvertimeStatistics.DeltaTrend> trends = statistics.getDeltaTrend();
        assertThat(trends.get(1)).isEqualTo(DECREASE);
        assertThat(trends.get(2)).isEqualTo(INCREASE);
        assertThat(trends.get(3)).isEqualTo(NEUTRAL);
    }

    @Test
    void ensureDeltaToPreviousMonthPercentIsNullForJanuary() {

        final Year year = Year.of(2024);
        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 1, 15), List.of(), 1);

        assertThat(statistics.getDeltaToPreviousMonthPercent()).isNull();
    }

    @Test
    void ensureDeltaToPreviousMonthPercentIsNullWhenPreviousMonthIsZero() {

        final Year year = Year.of(2024);
        final Overtime march = overtime(LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 5), Duration.ofHours(10));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 3, 15), List.of(march), 1);

        assertThat(statistics.getDeltaToPreviousMonthPercent()).isNull();
    }

    @Test
    void ensureDeltaToPreviousMonthPercentCalculation() {

        final Year year = Year.of(2024);
        final Overtime february = overtime(LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 5), Duration.ofHours(10));
        final Overtime march = overtime(LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 5), Duration.ofHours(15));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 3, 15), List.of(february, march), 1);

        assertThat(statistics.getDeltaToPreviousMonthPercent()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void ensureRolling30DayAveragePerPersonReturnsZeroWhenNoPersons() {

        final Year year = Year.of(2024);
        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 6, 15), List.of(), 0);

        assertThat(statistics.getRolling30DayAveragePerPerson()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void ensureRolling30DayAveragePerPersonIsClampedToStartOfYear() {

        final Year year = Year.of(2024);
        // a naive 30-day window from January 10th would reach into December 2023, but no cross-year
        // data has been fetched for this feature, so the window must be clamped to January 1st.
        final Overtime beforeYear = overtime(LocalDate.of(2023, 12, 20), LocalDate.of(2023, 12, 20), Duration.ofHours(100));
        final Overtime withinWindow = overtime(LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 5), Duration.ofHours(10));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 1, 10), List.of(beforeYear, withinWindow), 2);

        assertThat(statistics.getRolling30DayAveragePerPerson()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void ensureRolling30DayAveragePerPerson() {

        final Year year = Year.of(2024);
        final Overtime overtime = overtime(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 15), Duration.ofHours(30));

        final OvertimeStatistics statistics = new OvertimeStatistics(year, LocalDate.of(2024, 6, 20), List.of(overtime), 3);

        assertThat(statistics.getRolling30DayAveragePerPerson()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    private static Overtime overtime(LocalDate start, LocalDate end, Duration duration) {
        return new Overtime(new OvertimeId(1L), PERSON_ID, new DateRange(start, end), duration, OvertimeType.UV_INTERNAL, Instant.EPOCH);
    }
}
