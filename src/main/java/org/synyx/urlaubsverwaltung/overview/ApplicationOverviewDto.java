package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

public final class ApplicationOverviewDto {

    private final List<ApplicationDto> applications;
    private final ApplicationDaysUsedSummaryDto usedDaysOverview;

    public ApplicationOverviewDto(
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
