package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import java.util.List;

/**
 * @deprecated this class has been used for the client side rendered vacation overview which is obsolete now.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
public class VacationOverviewsDto {

    private List<VacationOverviewDto> overviews;

    public VacationOverviewsDto(List<VacationOverviewDto> overviews) {
        this.overviews = overviews;
    }

    public List<VacationOverviewDto> getOverviews() {
        return overviews;
    }

    public void setOverviews(List<VacationOverviewDto> overviews) {
        this.overviews = overviews;
    }
}
