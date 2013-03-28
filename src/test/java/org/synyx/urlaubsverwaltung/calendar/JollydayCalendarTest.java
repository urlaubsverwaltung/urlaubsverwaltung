
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



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


    /** Test of getPublicHolidays method, of class JollydayCalendar. */
    @Test
    public void testGetPublicHolidays() {

        // statutory holidays (at the weekend)
        // 23th is Friday, 27th is Tuesday
        // this means: only one statutory holiday (Monday, the 26th) should be detected
        // the others are not on weekdays
        DateMidnight startDate = new DateMidnight(2011, 12, 23);
        DateMidnight endDate = new DateMidnight(2011, 12, 27);

        double returnValue = instance.getPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(1.0, returnValue, 0.0);

        // here:
        // 24.12. is a Friday
        // 21.12 is a Tuesday
        // i.e.: it should be detected a half day as holiday
        startDate = new DateMidnight(2010, 12, 23);
        endDate = new DateMidnight(2010, 12, 24);

        returnValue = instance.getPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(0.5, returnValue, 0.0);

        // 25.12.2009 - 31.12.2009
        // a total of statutory holidays: 2.5
        // because of weekend real holidays: 1.5
        startDate = new DateMidnight(2009, 12, 25);
        endDate = new DateMidnight(2009, 12, 31);

        returnValue = instance.getPublicHolidays(startDate, endDate);
        Assert.assertNotNull(returnValue);
        Assert.assertEquals(1.5, returnValue, 0.0);
    }


    /** Test if adding (countBetweenDays) works correctly */
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
        
        double returnValue = instance.getPublicHolidays(startDate, endDate);
        
        // expected that Corpus Christi is found as one and only public holiday in this period
        Assert.assertEquals(1, returnValue, 0);
        
    }

    @Test
    public void testCorpusChristiProblem2() {

        DateMidnight startDate = new DateMidnight(2014, 6, 19);
        DateMidnight endDate = new DateMidnight(2014, 6, 19);

        double returnValue = instance.getPublicHolidays(startDate, endDate);

        // expected that Corpus Christi is found as one and only public holiday in this period
        Assert.assertEquals(1, returnValue, 0);

    }

    
    @Test
    public void testIsPublicHoliday() {

        DateMidnight date = new DateMidnight(2013, 3, 29);   // Karfreitag
        
        boolean returnValue = instance.isPublicHoliday(date);
        
        Assert.assertTrue(returnValue);
        
    }

    @Test
    public void testIsNoPublicHoliday() {

        DateMidnight date = new DateMidnight(2013, 3, 28);   // simple work day

        boolean returnValue = instance.isPublicHoliday(date);

        Assert.assertFalse(returnValue);

    }
    
    
    
}
