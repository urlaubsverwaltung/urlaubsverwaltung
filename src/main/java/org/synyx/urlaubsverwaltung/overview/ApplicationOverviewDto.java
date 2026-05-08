package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

final class ApplicationOverviewDto {

    private final List<ApplicationDto> applications;
    private final ApplicationDaysUsedSummaryDto usedDaysOverview;

    ApplicationOverviewDto(
        List<ApplicationDto> applications,
        ApplicationDaysUsedSummaryDto usedDaysOverview
    ) {
        this.applications = applications;
        this.usedDaysOverview = usedDaysOverview;
    }

    public List<ApplicationDto> getApplications() {
        return applications;
    }

    public ApplicationDaysUsedSummaryDto getUsedDaysOverview() {
        return usedDaysOverview;
    }
}
