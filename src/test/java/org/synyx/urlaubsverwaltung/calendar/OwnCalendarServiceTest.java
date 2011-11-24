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
// @Test
// public void testGetVacationDays() {
//
// // Testen fuer 2010: 17.12. ist Freitag, 31.12. ist Freitag
// // dazwischen liegen insgesamt 2 Feiertage (25., 26.) plus zwei Mal halbe Tage (24., 31.)
// // Brutto sind es 14 Tage
// // Netto sind es unter Beruecksichtigung von Feiertagen und Wochenenden 10 Tage
//
// DateMidnight start = new DateMidnight(2010, 12, 17);
// DateMidnight end = new DateMidnight(2010, 12, 31);
//
// Double returnValue = instance.getVacationDays(start, end);
//
// assertNotNull(returnValue);
// assertEquals(10.0, returnValue, 0.0);
//
// // Testen fuer 2009: 17.12. ist Donnerstag, 31.12. ist Donnerstag
// // dazwischen liegen insgesamt 2 Feiertage (25., 26.) plus zwei Mal halbe Tage (24., 31.)
// // Brutto sind es 14 Tage
// // Netto sind es unter Beruecksichtigung von Feiertagen und Wochenenden 9 Tage
//
// start = new DateMidnight(2009, 12, 17);
// end = new DateMidnight(2009, 12, 31);
//
// returnValue = instance.getVacationDays(start, end);
//
// assertNotNull(returnValue);
// assertEquals(9.0, returnValue, 0.0);
// }
}
