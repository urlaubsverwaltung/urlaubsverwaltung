package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

record ApplicationOverviewDto(
    List<ApplicationDto> applications,
    ApplicationDaysUsedSummaryDto usedDaysOverview,
    boolean canAddApplicationForLeave,
    boolean canAddApplicationForLeaveForMyself,
    boolean canAddApplicationForLeaveForAnotherUser,
    int numberOfShownApplications,
    int numberOfTotalApplications
) {
}
