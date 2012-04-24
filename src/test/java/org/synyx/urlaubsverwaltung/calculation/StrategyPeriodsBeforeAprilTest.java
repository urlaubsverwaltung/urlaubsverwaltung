/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import java.math.BigDecimal;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import static org.junit.Assert.*;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;

/**
 *
 * @author Aljona Murygina
 */
public class StrategyPeriodsBeforeAprilTest {
    
    private StrategyPeriodsBeforeApril instance;
    
    private OwnCalendarService calendarService = new OwnCalendarService();
    private Application app;
    
    public StrategyPeriodsBeforeAprilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        
        instance = new StrategyPeriodsBeforeApril(calendarService);
        app = new Application();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of calculate method, of class StrategyPeriodsBeforeApril.
     */
    @Test
    public void testCalculate() {
        
        // 6 work days before April
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 23));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 1));
        app.setHowLong(DayLength.FULL);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(6).setScale(2), app.getDaysBeforeApril());
        assertEquals(BigDecimal.ZERO, app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(6).setScale(2), app.getDays());
        
    }
    
        /**
     * Test of calculate method, of class StrategyPeriodsBeforeApril.
     */
    @Test
    public void testCalculateForHalfDays() {
        
        // 0.5 work days before April
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 1));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 1));
        app.setHowLong(DayLength.NOON);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(0.5).setScale(2), app.getDaysBeforeApril());
        assertEquals(BigDecimal.ZERO, app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(0.5).setScale(2), app.getDays());
        
    }
}
