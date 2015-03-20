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

        FilterRequest filterRequest = new FilterRequest(FilterRequest.Period.QUARTAL);

        Assert.assertNotNull("Period must be set", filterRequest.getPeriod());
        Assert.assertEquals("Wrong period", FilterRequest.Period.QUARTAL, filterRequest.getPeriod());

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
}
