
package org.synyx.urlaubsverwaltung.core.util;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link DateUtil}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DateUtilTest {

    @Test
    public void ensureReturnsTrueIfGivenDayIsAWorkDay() {

        // Monday
        DateMidnight date = new DateMidnight(2011, 12, 26);

        boolean returnValue = DateUtil.isWorkDay(date);

        Assert.assertTrue("Should return true for a work day", returnValue);
    }


    @Test
    public void ensureReturnsFalseIfGivenDayIsNotAWorkDay() {

        // Sunday
        DateMidnight date = new DateMidnight(2014, 11, 23);

        boolean returnValue = DateUtil.isWorkDay(date);

        Assert.assertFalse("Should return false for not a work day", returnValue);
    }


    @Test
    public void ensureReturnsCorrectFirstDayOfMonth() {

        int year = 2014;
        int month = 11;

        DateMidnight firstDayOfMonth = new DateMidnight(year, month, 1);

        Assert.assertEquals("Not the correct first day of month", firstDayOfMonth,
            DateUtil.getFirstDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfMonth() {

        int year = 2014;
        int month = 11;

        DateMidnight lastDayOfMonth = new DateMidnight(year, month, 30);

        Assert.assertEquals("Not the correct last day of month", lastDayOfMonth,
            DateUtil.getLastDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfMonthForSpecialMonths() {

        int year = 2014;
        int month = 2;

        DateMidnight lastDayOfMonth = new DateMidnight(year, month, 28);

        Assert.assertEquals("Not the correct last day of month", lastDayOfMonth,
            DateUtil.getLastDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectFirstDayOfYear() {

        int year = 2014;

        DateMidnight firstDayOfYear = new DateMidnight(year, 1, 1);

        Assert.assertEquals("Not the correct first day of year", firstDayOfYear, DateUtil.getFirstDayOfYear(year));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfYear() {

        int year = 2014;

        DateMidnight lastDayOfYear = new DateMidnight(year, 12, 31);

        Assert.assertEquals("Not the correct last day of year", lastDayOfYear, DateUtil.getLastDayOfYear(year));
    }


    @Test
    public void ensureReturnsTrueForChristmasEve() {

        DateMidnight date = new DateMidnight(2011, 12, 24);

        boolean returnValue = DateUtil.isChristmasEve(date);

        Assert.assertTrue("Should return true for 24th December", returnValue);
    }


    @Test
    public void ensureReturnsFalseForNotChristmasEve() {

        DateMidnight date = new DateMidnight(2011, 12, 25);

        boolean returnValue = DateUtil.isChristmasEve(date);

        Assert.assertFalse("Should return false for 25th December", returnValue);
    }


    @Test
    public void ensureReturnsTrueForNewYearsEve() {

        DateMidnight date = new DateMidnight(2014, 12, 31);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        Assert.assertTrue("Should return true for 31st December", returnValue);
    }


    @Test
    public void ensureReturnsFalseForNotNewYearsEve() {

        DateMidnight date = new DateMidnight(2011, 12, 25);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        Assert.assertFalse("Should return false for 25th December", returnValue);
    }
}
