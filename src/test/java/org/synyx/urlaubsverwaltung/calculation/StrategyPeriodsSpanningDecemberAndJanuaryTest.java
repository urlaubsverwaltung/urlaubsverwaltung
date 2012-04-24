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
public class StrategyPeriodsSpanningDecemberAndJanuaryTest {
    
    private StrategyPeriodsSpanningDecemberAndJanuary instance;
    
    private OwnCalendarService calendarService = new OwnCalendarService();
    private Application app;
    
    public StrategyPeriodsSpanningDecemberAndJanuaryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        
        instance = new StrategyPeriodsSpanningDecemberAndJanuary(calendarService); 
        app = new Application();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of calculate method, of class StrategyPeriodsSpanningDecemberAndJanuary.
     */
    @Test
    public void testCalculate() {
        
        // 5.5 work days = 2.5 work days before April (in the new year) plus 3 work days after April (in the old year)
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 26));
        app.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 4));
        app.setHowLong(DayLength.FULL);
        
        Application returnValue = instance.calculate(app);
        
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(3).setScale(2), app.getDaysBeforeApril());
        assertEquals(BigDecimal.valueOf(2.5).setScale(2), app.getDaysAfterApril());
        assertEquals(BigDecimal.valueOf(5.5).setScale(2), app.getDays());
    }
}
