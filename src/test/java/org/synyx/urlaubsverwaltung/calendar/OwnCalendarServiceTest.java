/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class OwnCalendarServiceTest {

    private OwnCalendarService instance;
    private Application application;

    public OwnCalendarServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new OwnCalendarService();

        application = new Application();
    }


    @After
    public void tearDown() {
    }


    /** Test of getWorkDays method, of class OwnCalendarService. */
    @Test
    public void testGetWorkDays() {

        DateMidnight start = new DateMidnight(2011, 11, 16);
        DateMidnight end = new DateMidnight(2011, 11, 28);

        double returnValue = instance.getWorkDays(start, end);

        assertNotNull(returnValue);
        assertEquals(9.0, returnValue, 0.0);
    }


    /** Test of getVacationDays method, of class OwnCalendarService. */
    @Test
    public void testGetVacationDays() {

        // testing for full days
        application.setHowLong(DayLength.FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 10 (considering public holidays and weekends)

        DateMidnight start = new DateMidnight(2010, 12, 17);
        DateMidnight end = new DateMidnight(2010, 12, 31);

        BigDecimal returnValue = instance.getVacationDays(application, start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(10.0).setScale(2), returnValue);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 9 (considering public holidays and weekends)

        start = new DateMidnight(2009, 12, 17);
        end = new DateMidnight(2009, 12, 31);

        returnValue = instance.getVacationDays(application, start, end);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9.0).setScale(2), returnValue);
    }
}
