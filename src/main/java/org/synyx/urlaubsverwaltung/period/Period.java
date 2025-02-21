package org.synyx.urlaubsverwaltung.period;

import org.springframework.util.Assert;

import java.time.LocalDate;

/**
 * Represents a period of time.
 */
public record Period(
    LocalDate startDate,
    LocalDate endDate,
    DayLength dayLength
) {
    public Period {
        Assert.isTrue(!dayLength.equals(DayLength.ZERO), "Day length may not be zero");

        if (dayLength.isFull() && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (dayLength.isHalfDay() && !startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Start and end date must be same for half day length");
        }
    }
}
