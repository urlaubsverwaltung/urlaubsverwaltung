/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.util.DateUtil;

/**
 *
 * @author Aljona Murygina
 */
public class CalculationMethodsHandler {
    
    private StrategyPeriodsAfterApril strategyPeriodsAfterApril;
    private StrategyPeriodsBeforeApril strategyPeriodsBeforeApril;
    private StrategyPeriodsSpanningMarchAndApril strategyPeriodsSpanningMarchAndApril;
    private StrategyPeriodsSpanningDecemberAndJanuary strategyPeriodsSpanningDecemberAndJanuary;

    public CalculationMethodsHandler(StrategyPeriodsAfterApril strategyPeriodsAfterApril, StrategyPeriodsBeforeApril strategyPeriodsBeforeApril, StrategyPeriodsSpanningMarchAndApril strategyPeriodsSpanningMarchAndApril, StrategyPeriodsSpanningDecemberAndJanuary strategyPeriodsSpanningDecemberAndJanuary) {
        this.strategyPeriodsAfterApril = strategyPeriodsAfterApril;
        this.strategyPeriodsBeforeApril = strategyPeriodsBeforeApril;
        this.strategyPeriodsSpanningMarchAndApril = strategyPeriodsSpanningMarchAndApril;
        this.strategyPeriodsSpanningDecemberAndJanuary = strategyPeriodsSpanningDecemberAndJanuary;
    }
    
    public void forwardToCalculationStrategy(Application application) {
        
        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        
        if (DateUtil.spansDecemberAndJanuary(startDate, endDate)) {
            // you persist only one application but you have to check for two years if there are enough vacation days to apply for leave
            strategyPeriodsSpanningDecemberAndJanuary.calculate(application);
        }
        
        if(DateUtil.spansMarchAndApril(startDate, endDate)) {
            strategyPeriodsSpanningMarchAndApril.calculate(application);
        }
        
        if(DateUtil.isBeforeApril(application.getEndDate())) {
            strategyPeriodsBeforeApril.calculate(application);
        }
        
        if(DateUtil.isAfterApril(endDate)) {
            strategyPeriodsAfterApril.calculate(application);
        }
        
    }
    
}
