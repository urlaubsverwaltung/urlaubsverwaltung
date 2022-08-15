package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;

import java.time.Duration;

public class LeftOvertimeDateRange {

    private final DateRange dateRange;
    private final Duration leftOvertime;

    public LeftOvertimeDateRange(DateRange dateRange, Duration leftOvertime) {
        this.dateRange = dateRange;
        this.leftOvertime = leftOvertime;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public Duration getLeftOvertime() {
        return leftOvertime;
    }
}
