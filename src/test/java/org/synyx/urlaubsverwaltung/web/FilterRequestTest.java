package org.synyx.urlaubsverwaltung.web;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.util.DateUtil;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.FilterRequest}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class FilterRequestTest {

    @Test
    public void ensureHasDefaultPeriod() {

        FilterRequest filterRequest = new FilterRequest();

        Assert.assertNotNull("Period must be set", filterRequest.getPeriod());
        Assert.assertEquals("Wrong period", FilterRequest.Period.YEAR, filterRequest.getPeriod());
    }


    @Test
    public void ensureReturnsCorrectStartAndEndDateForYear() {

        FilterRequest filterRequest = new FilterRequest(FilterRequest.Period.YEAR);

        Assert.assertNotNull("Period must be set", filterRequest.getPeriod());
        Assert.assertEquals("Wrong period", FilterRequest.Period.YEAR, filterRequest.getPeriod());

        int currentYear = DateMidnight.now().getYear();
        DateMidnight firstDayOfTheYear = DateUtil.getFirstDayOfYear(currentYear);
        DateMidnight lastDayOfTheYear = DateUtil.getLastDayOfYear(currentYear);

        Assert.assertNotNull("Start date must not be null", filterRequest.getStartDate());
        Assert.assertEquals("Wrong start date", firstDayOfTheYear, filterRequest.getStartDate());

        Assert.assertNotNull("End date must not be null", filterRequest.getEndDate());
        Assert.assertEquals("Wrong end date", lastDayOfTheYear, filterRequest.getEndDate());
    }


    @Test
    public void ensureReturnsCorrectStartAndEndDateForMonth() {

        FilterRequest filterRequest = new FilterRequest(FilterRequest.Period.MONTH);

        Assert.assertNotNull("Period must be set", filterRequest.getPeriod());
        Assert.assertEquals("Wrong period", FilterRequest.Period.MONTH, filterRequest.getPeriod());

        int currentYear = DateMidnight.now().getYear();
        int currentMonth = DateMidnight.now().getMonthOfYear();
        DateMidnight firstDayOfMonth = DateUtil.getFirstDayOfMonth(currentYear, currentMonth);
        DateMidnight lastDayOfMonth = DateUtil.getLastDayOfMonth(currentYear, currentMonth);

        Assert.assertNotNull("Start date must not be null", filterRequest.getStartDate());
        Assert.assertEquals("Wrong start date", firstDayOfMonth, filterRequest.getStartDate());

        Assert.assertNotNull("End date must not be null", filterRequest.getEndDate());
        Assert.assertEquals("Wrong end date", lastDayOfMonth, filterRequest.getEndDate());
    }


    @Test
    public void ensureReturnsCorrectStartAndEndDateForQuarter() {

        FilterRequest filterRequest = new FilterRequest(FilterRequest.Period.QUARTER);

        Assert.assertNotNull("Period must be set", filterRequest.getPeriod());
        Assert.assertEquals("Wrong period", FilterRequest.Period.QUARTER, filterRequest.getPeriod());

        Assert.assertNotNull("Start date must not be null", filterRequest.getStartDate());
        Assert.assertNotNull("End date must not be null", filterRequest.getEndDate());
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetStartDateAndNoPeriodIsSet() {

        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setPeriod(null);
        filterRequest.getStartDate();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetEndDateAndNoPeriodIsSet() {

        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setPeriod(null);
        filterRequest.getEndDate();
    }


    @Test
    public void ensureReturnsCorrectQuarterStartDate() {

        FilterRequest filterRequest = new FilterRequest();

        DateMidnight startDateOfQuarter = filterRequest.getStartDateOfQuarter(new DateMidnight(2014, 2, 2));
        Assert.assertNotNull("Should not be null", startDateOfQuarter);
        Assert.assertEquals("Wrong start date of quarter", new DateMidnight(2014, 1, 1), startDateOfQuarter);

        startDateOfQuarter = filterRequest.getStartDateOfQuarter(new DateMidnight(2014, 3, 12));
        Assert.assertNotNull("Should not be null", startDateOfQuarter);
        Assert.assertEquals("Wrong start date of quarter", new DateMidnight(2014, 1, 1), startDateOfQuarter);

        startDateOfQuarter = filterRequest.getStartDateOfQuarter(new DateMidnight(2014, 6, 23));
        Assert.assertNotNull("Should not be null", startDateOfQuarter);
        Assert.assertEquals("Wrong start date of quarter", new DateMidnight(2014, 4, 1), startDateOfQuarter);

        startDateOfQuarter = filterRequest.getStartDateOfQuarter(new DateMidnight(2014, 8, 23));
        Assert.assertNotNull("Should not be null", startDateOfQuarter);
        Assert.assertEquals("Wrong start date of quarter", new DateMidnight(2014, 7, 1), startDateOfQuarter);

        startDateOfQuarter = filterRequest.getStartDateOfQuarter(new DateMidnight(2014, 11, 23));
        Assert.assertNotNull("Should not be null", startDateOfQuarter);
        Assert.assertEquals("Wrong start date of quarter", new DateMidnight(2014, 10, 1), startDateOfQuarter);
    }


    @Test
    public void ensureReturnsCorrectQuarterEndDate() {

        FilterRequest filterRequest = new FilterRequest();

        DateMidnight endDateOfQuarter = filterRequest.getEndDateOfQuarter(new DateMidnight(2014, 2, 2));
        Assert.assertNotNull("Should not be null", endDateOfQuarter);
        Assert.assertEquals("Wrong end date of quarter", new DateMidnight(2014, 3, 31), endDateOfQuarter);

        endDateOfQuarter = filterRequest.getEndDateOfQuarter(new DateMidnight(2014, 3, 12));
        Assert.assertNotNull("Should not be null", endDateOfQuarter);
        Assert.assertEquals("Wrong end date of quarter", new DateMidnight(2014, 3, 31), endDateOfQuarter);

        endDateOfQuarter = filterRequest.getEndDateOfQuarter(new DateMidnight(2014, 6, 23));
        Assert.assertNotNull("Should not be null", endDateOfQuarter);
        Assert.assertEquals("Wrong end date of quarter", new DateMidnight(2014, 6, 30), endDateOfQuarter);

        endDateOfQuarter = filterRequest.getEndDateOfQuarter(new DateMidnight(2014, 8, 23));
        Assert.assertNotNull("Should not be null", endDateOfQuarter);
        Assert.assertEquals("Wrong end date of quarter", new DateMidnight(2014, 9, 30), endDateOfQuarter);

        endDateOfQuarter = filterRequest.getEndDateOfQuarter(new DateMidnight(2014, 11, 23));
        Assert.assertNotNull("Should not be null", endDateOfQuarter);
        Assert.assertEquals("Wrong end date of quarter", new DateMidnight(2014, 12, 31), endDateOfQuarter);
    }
}
