/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import org.synyx.urlaubsverwaltung.domain.Application;

/**
 *
 * @author Aljona Murygina
 */
public class CalculationMethodsHandlerTest {
    
    private CalculationMethodsHandler handler;
    
    private StrategyPeriodsBeforeApril strategyBeforeApril = Mockito.mock(StrategyPeriodsBeforeApril.class);
    private StrategyPeriodsAfterApril strategyAfterApril = Mockito.mock(StrategyPeriodsAfterApril.class);
    private StrategyPeriodsSpanningMarchAndApril strategySpanningMarchAndApril = Mockito.mock(StrategyPeriodsSpanningMarchAndApril.class);
    private StrategyPeriodsSpanningDecemberAndJanuary strategySpanningDecemberAndJanuary = Mockito.mock(StrategyPeriodsSpanningDecemberAndJanuary.class);
    
    private Application app;
    
    public CalculationMethodsHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        
        handler = new CalculationMethodsHandler(strategyAfterApril, strategyBeforeApril, strategySpanningMarchAndApril, strategySpanningDecemberAndJanuary);
        app = new Application();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of forwardToCalculationStrategy method, of class CalculationMethodsHandler.
     */
    @Test
    public void testForwardToCalculationStrategyForPeriodBeforeApril() {
        
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 23));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 31));
        
        handler.forwardToCalculationStrategy(app);
        
        Mockito.verify(strategyBeforeApril).calculate(app);
        
        
    }
    
     /**
     * Test of forwardToCalculationStrategy method, of class CalculationMethodsHandler.
     */
    @Test
    public void testForwardToCalculationStrategyForPeriodAfterApril() {
        
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.APRIL, 1));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 10));
        
        handler.forwardToCalculationStrategy(app);
        
        Mockito.verify(strategyAfterApril).calculate(app);
        
        
    }
    
     /**
     * Test of forwardToCalculationStrategy method, of class CalculationMethodsHandler.
     */
    @Test
    public void testForwardToCalculationStrategyForPeriodSpanningMarchAndApril() {
        
        app.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 23));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        
        handler.forwardToCalculationStrategy(app);
        
        Mockito.verify(strategySpanningMarchAndApril).calculate(app);
        
        
    }
    
     /**
     * Test of forwardToCalculationStrategy method, of class CalculationMethodsHandler.
     */
    @Test
    public void testForwardToCalculationStrategyForPeriodSpanningDecemberAndJanuary() {
        
        app.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 23));
        app.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 6));
        
        handler.forwardToCalculationStrategy(app);
        
        Mockito.verify(strategySpanningDecemberAndJanuary).calculate(app);
        
        
    }
}
