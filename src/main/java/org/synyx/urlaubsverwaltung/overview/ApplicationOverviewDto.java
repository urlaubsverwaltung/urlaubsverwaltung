package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

final class ApplicationOverviewDto {

    private final List<ApplicationDto> applications;
    private final ApplicationDaysUsedSummaryDto usedDaysOverview;
    private final boolean canAddApplicationForLeaveForMyself;
    private final boolean canAddApplicationForLeaveForAnotherUser;

    ApplicationOverviewDto(
        List<ApplicationDto> applications,
        ApplicationDaysUsedSummaryDto usedDaysOverview, boolean canAddApplicationForLeaveForMyself, boolean canAddApplicationForLeaveForAnotherUser
    ) {
        this.applications = applications;
        this.usedDaysOverview = usedDaysOverview;
        this.canAddApplicationForLeaveForMyself = canAddApplicationForLeaveForMyself;
        this.canAddApplicationForLeaveForAnotherUser = canAddApplicationForLeaveForAnotherUser;
    }

    public List<ApplicationDto> getApplications() {
        return applications;
    }

    public ApplicationDaysUsedSummaryDto getUsedDaysOverview() {
        return usedDaysOverview;
    }

    public boolean isCanAddApplicationForLeaveForMyself() {
        return canAddApplicationForLeaveForMyself;
    }

    public boolean isCanAddApplicationForLeaveForAnotherUser() {
        return canAddApplicationForLeaveForAnotherUser;
    }
}
