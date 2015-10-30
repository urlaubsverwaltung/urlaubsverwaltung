package org.synyx.urlaubsverwaltung.core.period;

import org.joda.time.DateMidnight;

import org.springframework.util.Assert;


/**
 * Represents a period of time.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Period {

    private final DateMidnight startDate;
    private final DateMidnight endDate;
    private final DayLength dayLength;

    public Period(DateMidnight startDate, DateMidnight endDate, DayLength dayLength) {

        Assert.notNull(startDate, "Start date must be given");
        Assert.notNull(endDate, "End date must be given");
        Assert.notNull(dayLength, "Day length must be given");
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

    public DateMidnight getStartDate() {

        return startDate;
    }


    public DateMidnight getEndDate() {

        return endDate;
    }


    public DayLength getDayLength() {

        return dayLength;
    }
}
