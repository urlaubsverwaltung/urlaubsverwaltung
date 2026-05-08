package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;

record OvertimeOverviewDto(
    boolean isOvertimeActive,
    Duration overtimeTotal,
    Duration overtimeLeft
) {
}
