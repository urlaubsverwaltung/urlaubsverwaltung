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
import static org.junit.Assert.*;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;

/**
 *
 * @author Aljona Murygina
 */
public class StrategyPeriodsAfterAprilTest {
    
    private StrategyPeriodsAfterApril instance;
    
    private OwnCalendarService calendarService = new OwnCalendarService();
    private Application app;
    
    public StrategyPeriodsAfterAprilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        instance = new StrategyPeriodsAfterApril(calendarService);
        app = new Application();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of calculate method, of class StrategyPeriodsAfterApril.
     */
    @Test
    public void testCalculate() {
        
        // 5 work days (because of 'Ostern')
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.APRIL, 2));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 10));
        app.setHowLong(DayLength.FULL);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.ZERO, app.getDaysBeforeApril());
        assertEquals(BigDecimal.valueOf(5).setScale(2), app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(5).setScale(2), app.getDays()); // days before April plus days after April
    }
    
        /**
     * Test of calculate method, of class StrategyPeriodsAfterApril.
     */
    @Test
    public void testCalculateForHalfDay() {
        
        // 0.5 work days
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.APRIL, 2));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 2));
        app.setHowLong(DayLength.MORNING);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.ZERO, app.getDaysBeforeApril());
        assertEquals(BigDecimal.valueOf(0.5).setScale(2), app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(0.5).setScale(2), app.getDays()); // days before April plus days after April
    }
}
