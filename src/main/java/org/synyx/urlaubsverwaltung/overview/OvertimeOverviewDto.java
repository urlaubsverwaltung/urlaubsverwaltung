package org.synyx.urlaubsverwaltung.overview;

import java.time.Duration;
import java.util.List;

record OvertimeOverviewDto(
    boolean isOvertimeActive,
    boolean userIsAllowedToCreateOvertime,
    Duration overtimeTotal,
    Duration overtimeLeft,
    List<OvertimeRecordDto> shownOvertimes,
    int numberOfShownOvertimes,
    int numberOfTotalOvertimes
) {
}
