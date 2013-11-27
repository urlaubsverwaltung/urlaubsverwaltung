
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import java.util.List;


/**
 * Unit test for {@link JollydayCalendar}.
 *
 * @author  Aljona Murygina
 */
public class JollydayCalendarTest {

    private JollydayCalendar instance;

    @Before
    public void setUp() {

        instance = new JollydayCalendar();
    }


    /**
     * Test of getNumberOfPublicHolidays method, of class JollydayCalendar.
     */
    @Test
    public void testGetPublicHolidays() {

        // statutory holidays (at the weekend)
        // 23th is Friday, 27th is Tuesday
        // this means: only one statutory holiday (Monday, the 26th) should be detected
        // the others are not on weekdays
        DateMidnight startDate = new DateMidnight(2011, 12, 23);
        DateMidnight endDate = new DateMidnight(2011, 12, 27);

        double returnValue = instance.getNumberOfPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(1.0, returnValue, 0.0);

        // here:
        // 24.12. is a Friday
        // 21.12 is a Tuesday
        // i.e.: it should be detected a half day as holiday
        startDate = new DateMidnight(2010, 12, 23);
        endDate = new DateMidnight(2010, 12, 24);

        returnValue = instance.getNumberOfPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(0.5, returnValue, 0.0);

        // 25.12.2009 - 31.12.2009
        // a total of statutory holidays: 2.5
        // because of weekend real holidays: 1.5
        startDate = new DateMidnight(2009, 12, 25);
        endDate = new DateMidnight(2009, 12, 31);

        returnValue = instance.getNumberOfPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(1.5, returnValue, 0.0);
    }


    /**
     * Test if adding (countBetweenDays) works correctly.
     */
    @Test
    public void testPlusDays() {

        DateMidnight startDate = new DateMidnight(2011, 11, 23);
        DateMidnight endDate = new DateMidnight(2011, 11, 26);

        int dings = 0;

        while (!(startDate.equals(endDate))) {
            dings++;
            startDate = startDate.plusDays(1);
        }

        Assert.assertSame(3, dings);
    }


    @Test
    public void testCorpusChristiProblem() {

        DateMidnight startDate = new DateMidnight(2013, 5, 29);
        DateMidnight endDate = new DateMidnight(2013, 6, 6);

        double returnValue = instance.getNumberOfPublicHolidays(startDate, endDate);

        // expected that Corpus Christi is found as one and only public holiday in this period
        Assert.assertEquals(1, returnValue, 0);
    }


    @Test
    public void testCorpusChristiProblem2() {

        DateMidnight startDate = new DateMidnight(2014, 6, 19);
        DateMidnight endDate = new DateMidnight(2014, 6, 19);

        double returnValue = instance.getNumberOfPublicHolidays(startDate, endDate);

        // expected that Corpus Christi is found as one and only public holiday in this period
        Assert.assertEquals(1, returnValue, 0);
    }


    @Test
    public void testGetPublicHolidaysFoo() {

        List<String> holidays = instance.getPublicHolidays(2013, 10);

        // 3.10.2013
        Assert.assertEquals(holidays.size(), 1);
        Assert.assertEquals(holidays.get(0).toString(), "2013-10-03");
    }


    @Test
    public void testIsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(true, returnValue);
    }


    @Test
    public void testIsNotPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 20);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(false, returnValue);
    }


    @Test
    public void testCorpusChristiIsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.MAY, 30);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(true, returnValue);
    }


    @Test
    public void testChristmasEveIsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(true, returnValue);
    }


    @Test
    public void testNewYearsEveIsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(true, returnValue);
    }


    @Test
    public void testEasterIsPublicHoliday() {

        DateMidnight testDate = new DateMidnight(2012, DateTimeConstants.APRIL, 8);

        boolean returnValue = instance.isPublicHoliday(testDate);

        Assert.assertEquals(true, returnValue);
    }


    @Test
    public void testGetWorkingDurationOfDateNoHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 27);

        BigDecimal returnValue = instance.getWorkingDurationOfDate(testDate);

        Assert.assertEquals(BigDecimal.ONE, returnValue);
    }


    @Test
    public void testGetWorkingDurationOfDateIsHoliday() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 25);

        BigDecimal returnValue = instance.getWorkingDurationOfDate(testDate);

        Assert.assertEquals(BigDecimal.ZERO, returnValue);
    }


    @Test
    public void testGetWorkingDurationOfDateIsChristmasEve() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal returnValue = instance.getWorkingDurationOfDate(testDate);

        Assert.assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkingDurationOfDateIsNewYearsEve() {

        DateMidnight testDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkingDurationOfDate(testDate);

        Assert.assertEquals(new BigDecimal("0.5"), returnValue);
    }
}
