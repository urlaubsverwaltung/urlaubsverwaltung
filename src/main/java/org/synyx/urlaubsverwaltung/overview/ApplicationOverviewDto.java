package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

final class ApplicationOverviewDto {

    private final List<ApplicationDto> applications;
    private final ApplicationDaysUsedSummaryDto usedDaysOverview;
    private final boolean canAddApplicationForLeaveForMyself;
    private final boolean canAddApplicationForLeaveForAnotherUser;
    private final int numberOfShownApplications;
    private final int numberOfTotalApplications;

    ApplicationOverviewDto(
        List<ApplicationDto> applications,
        ApplicationDaysUsedSummaryDto usedDaysOverview,
        boolean canAddApplicationForLeaveForMyself,
        boolean canAddApplicationForLeaveForAnotherUser,
        int numberOfShownApplications,
        int numberOfTotalApplications
    ) {
        this.applications = applications;
        this.usedDaysOverview = usedDaysOverview;
        this.canAddApplicationForLeaveForMyself = canAddApplicationForLeaveForMyself;
        this.canAddApplicationForLeaveForAnotherUser = canAddApplicationForLeaveForAnotherUser;
        this.numberOfShownApplications = numberOfShownApplications;
        this.numberOfTotalApplications = numberOfTotalApplications;
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

    public int getNumberOfShownApplications() {
        return numberOfShownApplications;
    }

    public int getNumberOfTotalApplications() {
        return numberOfTotalApplications;
    }
}
