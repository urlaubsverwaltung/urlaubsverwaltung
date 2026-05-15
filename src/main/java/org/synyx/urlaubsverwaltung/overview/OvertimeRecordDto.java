package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;
import java.time.LocalDate;

record OvertimeRecordDto(
    Long id,
    LocalDate startDate,
    LocalDate endDate,
    Duration duration,
    boolean isExternal,
    boolean isAllowedToEdit
) {
    public boolean isNegative() {
        return duration.isNegative();
    }
    public boolean isPositive() {
        return !duration.isNegative() && !duration.isZero();
    }
}
