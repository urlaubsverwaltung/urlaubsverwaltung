package org.synyx.urlaubsverwaltung.company;

import org.jspecify.annotations.Nullable;

import java.util.List;

record OvertimeStatDto(
    OvertimeDurationDto average,
    OvertimeDurationDto averageGrowth,
    OvertimeDistributionDto distribution
) {

    record OvertimeDurationDto(boolean negative, int hours, int minutes) {}

    record OvertimeDistributionDto(int personCount, List<OvertimeDistributionEntryDto> entries) {

        public int max() {
            return Math.max(personCount, 1);
        }
    }

    record OvertimeDistributionEntryDto(int rangeStart, @Nullable Integer rangeEnd, int value) {}
}
