/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author  aljona
 */
public class OwnCalendarServiceTest {

    private OwnCalendarService instance;

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
    }


    @After
    public void tearDown() {
    }


    /** Test of getWorkDays method, of class OwnCalendarService. */
    @Test
    public void testGetWorkDays() {

        DateMidnight start = new DateMidnight(2011, 11, 16);
        DateMidnight end = new DateMidnight(2011, 11, 28);

        Integer returnValue = instance.getWorkDays(start, end);

        assertNotNull(returnValue);
        assertSame(9, returnValue);
    }


    /** Test of getVacationDays method, of class OwnCalendarService. */
    @Test
    public void testGetVacationDays() throws Exception {

        // not yet able to implement
    }
}
