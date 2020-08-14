package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import java.util.List;

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
