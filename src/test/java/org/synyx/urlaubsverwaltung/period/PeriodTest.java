package org.synyx.urlaubsverwaltung.period;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;


public class PeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStartDate() {

        new Period(null, LocalDate.now(UTC), DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullEndDate() {

        new Period(LocalDate.now(UTC), null, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullDayLength() {

        new Period(LocalDate.now(UTC), LocalDate.now(UTC), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnZeroDayLength() {

        new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.ZERO);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfEndDateIsBeforeStartDate() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDateBeforeStartDate = startDate.minusDays(1);

        new Period(startDate, endDateBeforeStartDate, DayLength.FULL);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForMorningDayLength() {

        LocalDate startDate = LocalDate.now(UTC);

        new Period(startDate, startDate.plusDays(1), DayLength.MORNING);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfStartAndEndDateAreNotSameForNoonDayLength() {

        LocalDate startDate = LocalDate.now(UTC);

        new Period(startDate, startDate.plusDays(1), DayLength.NOON);
    }


    @Test
    public void ensureCanBeInitializedWithFullDay() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(1);

        Period period = new Period(startDate, endDate, DayLength.FULL);

        Assert.assertEquals("Wrong start date", startDate, period.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.FULL, period.getDayLength());
    }


    @Test
    public void ensureCanBeInitializedWithHalfDay() {

        LocalDate date = LocalDate.now(UTC);

        Period period = new Period(date, date, DayLength.MORNING);

        Assert.assertEquals("Wrong start date", date, period.getStartDate());
        Assert.assertEquals("Wrong end date", date, period.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.MORNING, period.getDayLength());
    }
}
