/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import java.math.BigDecimal;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;

/**
 *
 * @author Aljona Murygina
 */
public class StrategyPeriodsBeforeApril {
    
    private OwnCalendarService calendarService;

    public StrategyPeriodsBeforeApril(OwnCalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    
    Application calculate(Application application) {
        
        // because the application's period is before April you set the attribute daysBeforeApril to period's number of days
        BigDecimal daysBeforeApril = calendarService.getVacationDays(application, application.getStartDate(), application.getEndDate());
        
        // application's period is before April, so days after April are zero
        BigDecimal daysAfterApril = BigDecimal.ZERO;
        
        BigDecimal days = daysBeforeApril.add(daysAfterApril);
        
        application.setDaysBeforeApril(daysBeforeApril);
        application.setDaysAfterApril(daysAfterApril);
        application.setDays(days);
        
        return application;
    }
    
}
