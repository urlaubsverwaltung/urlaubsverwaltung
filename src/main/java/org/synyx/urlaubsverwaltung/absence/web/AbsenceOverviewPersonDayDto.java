package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean workday;
    private final List<AbsenceOverviewBarPieceDto> bars;
    private final String publicHolidayName;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean workday, List<AbsenceOverviewBarPieceDto> bars, String publicHolidayName) {
        this.type = type;
        this.workday = workday;
        this.bars = bars;
        this.publicHolidayName = publicHolidayName;
    }

    public AbsenceOverviewDayType getType() {
        return type;
    }

    public boolean isWorkday() {
        return workday;
    }

    public List<AbsenceOverviewBarPieceDto> getBars() {
        return bars;
    }

    public String getPublicHolidayName() {
        return publicHolidayName;
    }
}
