package org.synyx.urlaubsverwaltung.core.period;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;

import static org.junit.Assert.*;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStartDate() {

        new Period(null, DateMidnight.now(), DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEndDate() {

        new Period(DateMidnight.now(), null, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullDayLength() {

        new Period(DateMidnight.now(), DateMidnight.now(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnZeroDayLength() {

        new Period(DateMidnight.now(), DateMidnight.now(), DayLength.ZERO);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfEndDateIsBeforeStartDate() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDateBeforeStartDate = startDate.minusDays(1);

        new Period(startDate, endDateBeforeStartDate, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForMorningDayLength() {

        DateMidnight startDate = DateMidnight.now();

        new Period(startDate, startDate.plusDays(1), DayLength.MORNING);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForNoonDayLength() {

        DateMidnight startDate = DateMidnight.now();

        new Period(startDate, startDate.plusDays(1), DayLength.NOON);
    }


    @Test
    public void ensureCanBeInitializedWithFullDay() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(1);

        Period period = new Period(startDate, endDate, DayLength.FULL);

        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.FULL, period.getDayLength());
    }


    @Test
    public void ensureCanBeInitializedWithHalfDay() {

        DateMidnight date = DateMidnight.now();

        Period period = new Period(date, date, DayLength.MORNING);

        Assert.assertEquals("Wrong start date", date, period.getStartDate());
        Assert.assertEquals("Wrong end date", date, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.MORNING, period.getDayLength());
    }
}
