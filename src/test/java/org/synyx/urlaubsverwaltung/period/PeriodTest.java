package org.synyx.urlaubsverwaltung.period;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;


public class PeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStartDate() {

        new Period(null, Instant.now(), DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEndDate() {

        new Period(Instant.now(), null, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullDayLength() {

        new Period(Instant.now(), Instant.now(), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnZeroDayLength() {

        new Period(Instant.now(), Instant.now(), DayLength.ZERO);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfEndDateIsBeforeStartDate() {

        Instant startDate = Instant.now();
        Instant endDateBeforeStartDate = startDate.minus(1, DAYS);

        new Period(startDate, endDateBeforeStartDate, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForMorningDayLength() {

        Instant startDate = Instant.now();

        new Period(startDate, startDate.plus(1, DAYS), DayLength.MORNING);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForNoonDayLength() {

        Instant startDate = Instant.now();

        new Period(startDate, startDate.plus(1, DAYS), DayLength.NOON);
    }


    @Test
    public void ensureCanBeInitializedWithFullDay() {

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(1, DAYS);

        Period period = new Period(startDate, endDate, DayLength.FULL);

        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.FULL, period.getDayLength());
    }


    @Test
    public void ensureCanBeInitializedWithHalfDay() {

        Instant date = Instant.now();

        Period period = new Period(date, date, DayLength.MORNING);

        Assert.assertEquals("Wrong start date", date, period.getStartDate());
        Assert.assertEquals("Wrong end date", date, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.MORNING, period.getDayLength());
    }
}
