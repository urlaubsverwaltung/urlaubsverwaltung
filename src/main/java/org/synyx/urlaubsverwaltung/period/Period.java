package org.synyx.urlaubsverwaltung.period;

import org.springframework.util.Assert;

import java.time.LocalDate;

/**
 * Represents a period of time.
 */
public class Period {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DayLength dayLength;

    public Period(LocalDate startDate, LocalDate endDate, DayLength dayLength) {
        Assert.isTrue(!dayLength.equals(DayLength.ZERO), "Day length may not be zero");

        boolean isFullDay = dayLength.equals(DayLength.FULL);

        if (isFullDay && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        boolean isHalfDay = dayLength.equals(DayLength.MORNING) || dayLength.equals(DayLength.NOON);

        if (isHalfDay && !startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Start and end date must be same for half day length");
        }

        this.startDate = startDate;
        this.endDate = endDate;
        this.dayLength = dayLength;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public DayLength getDayLength() {
        return dayLength;
    }
}
