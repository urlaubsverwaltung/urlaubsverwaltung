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
public class StrategyPeriodsSpanningMarchAndAprilTest {
    
    private StrategyPeriodsSpanningMarchAndApril instance;
    
    private OwnCalendarService calendarService = new OwnCalendarService();
    private Application app;
    
    public StrategyPeriodsSpanningMarchAndAprilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        instance = new StrategyPeriodsSpanningMarchAndApril(calendarService); 
        app = new Application();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of calculate method, of class StrategyPeriodsSpanningMarchAndApril.
     */
    @Test
    public void testCalculate() {
        
        // 5 work days = 2 work days before April plus 3 work days after April
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 4));
        app.setHowLong(DayLength.FULL);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(2).setScale(2), app.getDaysBeforeApril());
        assertEquals(BigDecimal.valueOf(3).setScale(2), app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(5).setScale(2), app.getDays());
    }
    
}
