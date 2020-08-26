package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;

import java.util.List;

public class AbsenceOverviewDto {

    private final List<AbsenceOverviewMonthDto> months;

    AbsenceOverviewDto(List<AbsenceOverviewMonthDto> months) {
        this.months = months;
    }

    public List<AbsenceOverviewMonthDto> getMonths() {
        return months;
    }
}
