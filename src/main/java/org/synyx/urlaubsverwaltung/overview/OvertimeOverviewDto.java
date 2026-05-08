package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;

record OvertimeOverviewDto(
    boolean isOvertimeActive,
    boolean userIsAllowedToCreateOvertime,
    Duration overtimeTotal,
    Duration overtimeLeft
) {
}
