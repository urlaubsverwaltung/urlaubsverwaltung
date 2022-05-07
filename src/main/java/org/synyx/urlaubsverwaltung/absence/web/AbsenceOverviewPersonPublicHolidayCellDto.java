package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonPublicHolidayCellDto {

    private final long colStart;
    private final long colspan;
    private final String publicHolidayText;

    public AbsenceOverviewPersonPublicHolidayCellDto(long colStart, long colspan, String publicHolidayText) {
        this.colStart = colStart;
        this.colspan = colspan;
        this.publicHolidayText = publicHolidayText;
    }

    public long getColStart() {
        return colStart;
    }

    public long getColspan() {
        return colspan;
    }

    public String getPublicHolidayText() {
        return publicHolidayText;
    }
}
