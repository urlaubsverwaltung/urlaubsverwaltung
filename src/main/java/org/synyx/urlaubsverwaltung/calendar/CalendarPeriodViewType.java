package org.synyx.urlaubsverwaltung.calendar;

import java.time.Period;

public enum CalendarPeriodViewType {

    THREE_MONTHS("P3M"),
    HALF_YEAR("P6M"),
    YEAR("P1Y"),
    ALL("P100Y");

    private final Period period;

    CalendarPeriodViewType(String period) {

        this.period = Period.parse(period);
    }

    public Period getPeriod() {
        return period;
    }

    public static CalendarPeriodViewType ofPeriod(Period period) {
        final long months = period.toTotalMonths();

        if (months <= 3) {
            return THREE_MONTHS;
        }

        if (months <= 6) {
            return HALF_YEAR;
        }

        if (months <= 12) {
            return YEAR;
        }

        return ALL;
    }
}
