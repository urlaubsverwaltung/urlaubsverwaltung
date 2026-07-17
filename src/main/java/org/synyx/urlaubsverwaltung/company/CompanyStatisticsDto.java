package org.synyx.urlaubsverwaltung.company;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

record CompanyStatisticsDto(
    LocalDate from,
    LocalDate to,
    BigDecimal averageOvertime,
    BigDecimal averageOvertimeGrowth,
    OvertimeDistribution overtimeDistribution
) {

    record OvertimeDistribution(int personCount, List<OvertimeDistributionEntry> entries) {

        public int max() {
            return entries.stream().mapToInt(OvertimeDistributionEntry::value).max().orElse(1);
        }
    }

    record OvertimeDistributionEntry(int rangeStart, @Nullable Integer rangeEnd, int value) {}
}
