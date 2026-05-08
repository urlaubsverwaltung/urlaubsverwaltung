package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;

public record OvertimeOverviewDto(
    boolean isOvertimeActive,
    Duration overtimeTotal,
    Duration overtimeLeft
) {
}
