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
 * @author  aljona
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


    /** Test of getFeiertage method, of class JollydayCalendar. */
    @Test
    public void testGetFeiertage() {

        // Feiertage, die auf ein Wochenende fallen
        // 23. ist Freitag, 27. ist Dienstag
        // d.h. es sollte nur ein Feiertag, naemlich Mo., der 26. erkannt werden
        // die anderen fallen auf ein Wochenende
        DateMidnight startDate = new DateMidnight(2011, 12, 23);
        DateMidnight endDate = new DateMidnight(2011, 12, 27);

        Double returnValue = instance.getFeiertage(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(1.0, returnValue, 0.0);

        // 24.12. oder 31.12. kommt vor
        // hier kommt der 24.12. vor und ist ein Freitag
        // 21. ist ein Dienstag, 24. ist ein Freitag
        // d.h. es sollte ein halber Feiertag erkannt werden
        startDate = new DateMidnight(2010, 12, 23);
        endDate = new DateMidnight(2010, 12, 24);

        returnValue = instance.getFeiertage(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(0.5, returnValue, 0.0);

        // 31.12. kommt vor, Feiertage am Wochenende UND unter der Woche
        // 25.12.2009 - 31.12.2009
        // Feiertage insgesamt: 2.5
        // wegen Wochenende netto Feiertage: 1.5
        startDate = new DateMidnight(2009, 12, 25);
        endDate = new DateMidnight(2009, 12, 31);

        returnValue = instance.getFeiertage(startDate, endDate);
        assertNotNull(returnValue);
        assertEquals(1.5, returnValue, 0.0);
    }


    /** Teste, ob Addieren (countBetweenDays) funktioniert */
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


    /** Teste, ob Holen von Set<Holiday> funktioniert und ob sich Datumsangaben vergleichen lassen */
    @Ignore
    @Test
    public void testHolidaySet() {

        HolidayManager manager = HolidayManager.getInstance("synyx");
        Set<Holiday> holidays = manager.getHolidays(2011);
        DateMidnight date = new DateMidnight(2011, 01, 06, GregorianChronology.getInstance());

        for (Holiday day : holidays) {
            if ((day.getDate()).equals(date.toLocalDate())) {
                System.out.println("DIE SIND GLEICH!!!!");
                System.out.println(day.getDate() + " " + day.getDescription());
            }

            System.out.println(day.getDate());
            System.out.println(day.getDescription());
        }
    }
}
