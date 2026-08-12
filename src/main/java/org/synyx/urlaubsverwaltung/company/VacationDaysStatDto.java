package org.synyx.urlaubsverwaltung.company;

import org.jspecify.annotations.Nullable;

import java.util.List;

record VacationDaysStatDto(
    RemainingVacationDaysDistributionDto remainingVacationDaysDistribution
) {

    record RemainingVacationDaysDistributionDto(int personCount, List<RemainingVacationDaysDistributionEntryDto> entries) {

        public int max() {
            return Math.max(personCount, 1);
        }
    }

    record RemainingVacationDaysDistributionEntryDto(@Nullable Double rangeStart, @Nullable Double rangeEnd, int value) {}
}
