package org.synyx.urlaubsverwaltung.company;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

record SickDaysStatistic(
    HealthRate healthRate,
    BigDecimal totalNumberOfAllSickNotes,
    BigDecimal shouldWorkDays,
    Distribution distribution
) {

    record HealthRate(double value) {

        public static HealthRate of(BigDecimal bigDecimal) {
            return new HealthRate(bigDecimal.doubleValue() / 100);
        }
    }

    record Distribution(int personCount, List<DistributionEntry> entries) {

        public int max() {
            return Math.max(personCount, 1);
        }

        public static Distribution empty() {
            return new Distribution(0, List.of());
        }
    }

    record DistributionEntry(@Nullable Double rangeStart, @Nullable Double rangeEnd, int value) {}
}
