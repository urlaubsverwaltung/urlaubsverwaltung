package org.synyx.urlaubsverwaltung.company;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

record CompanyStatisticsDto(
    LocalDate from,
    LocalDate to,
    OvertimeDurationDto averageOvertime,
    OvertimeDurationDto averageOvertimeGrowth,
    OvertimeDistributionDto overtimeDistribution
) {

    record OvertimeDurationDto(boolean negative, int hours, int minutes) {}

    record OvertimeDistributionDto(int personCount, List<OvertimeDistributionEntryDto> entries) {

        public int max() {
            return Math.max(personCount, 1);
        }
    }

    record OvertimeDistributionEntryDto(int rangeStart, @Nullable Integer rangeEnd, int value) {}
}
