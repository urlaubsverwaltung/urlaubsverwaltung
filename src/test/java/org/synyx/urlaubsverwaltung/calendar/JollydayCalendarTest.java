/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;


/**
 * @author  Aljona Murygina
 */
public class JollydayCalendarTest {

    private JollydayCalendar instance;

    public JollydayCalendarTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new JollydayCalendar();
    }


    @After
    public void tearDown() {
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

        Double returnValue = instance.getPublicHolidays(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(1.0, returnValue, 0.0);

        // here:
        // 24.12. is a Friday
        // 21.12 is a Tuesday
        // i.e.: it should be detected a half day as holiday
        startDate = new DateMidnight(2010, 12, 23);
        endDate = new DateMidnight(2010, 12, 24);

        returnValue = instance.getPublicHolidays(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(0.5, returnValue, 0.0);

        // 25.12.2009 - 31.12.2009
        // a total of statutory holidays: 2.5
        // because of weekend real holidays: 1.5
        startDate = new DateMidnight(2009, 12, 25);
        endDate = new DateMidnight(2009, 12, 31);

        returnValue = instance.getPublicHolidays(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(1.5, returnValue, 0.0);
    }


    /** Test if adding (countBetweenDays) works correctly */
    @Test
    public void testPlusDays() {

        DateMidnight startDate = new DateMidnight(2011, 11, 23);
        DateMidnight endDate = new DateMidnight(2011, 11, 26);

        Integer dings = 0;

        while (!(startDate.equals(endDate))) {
            dings++;
            startDate = startDate.plusDays(1);
        }

        assertSame(3, dings);
    }


    /** Test if getting of Set<Holiday> and comparing dates works */
    @Ignore
    @Test
    public void testHolidaySet() {

        HolidayManager manager = HolidayManager.getInstance("synyx");
        Set<Holiday> holidays = manager.getHolidays(2011);
        DateMidnight date = new DateMidnight(2011, 01, 06, GregorianChronology.getInstance());

        for (Holiday day : holidays) {
            if ((day.getDate()).equals(date.toLocalDate())) {
                System.out.println("EQUALS!!!!");
                System.out.println(day.getDate() + " " + day.getDescription());
            }

            System.out.println(day.getDate());
            System.out.println(day.getDescription());
        }
    }
}
