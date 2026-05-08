package org.synyx.urlaubsverwaltung.overview;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ApplicationOverviewDto) obj;
        return Objects.equals(this.applications, that.applications) &&
            Objects.equals(this.usedDaysOverview, that.usedDaysOverview);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applications, usedDaysOverview);
    }

    @Override
    public String toString() {
        return "ApplicationOverviewDto[" +
            "applications=" + applications + ", " +
            "usedDaysOverview=" + usedDaysOverview + ']';
    }

}
