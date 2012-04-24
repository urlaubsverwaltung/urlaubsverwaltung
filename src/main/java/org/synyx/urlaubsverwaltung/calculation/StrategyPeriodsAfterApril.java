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
public class StrategyPeriodsAfterApril {
    
    private OwnCalendarService calendarService;

    public StrategyPeriodsAfterApril(OwnCalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    Application calculate(Application application) {
        
        // application's period is after April, so days before April are zero
        BigDecimal daysBeforeApril = BigDecimal.ZERO;
                
        // because the application's period is after April you set the attribute daysAfterApril to period's number of days
        BigDecimal daysAfterApril = calendarService.getVacationDays(application, application.getStartDate(), application.getEndDate());
        
        BigDecimal days = daysBeforeApril.add(daysAfterApril);
        
        application.setDaysBeforeApril(daysBeforeApril);
        application.setDaysAfterApril(daysAfterApril);
        application.setDays(days);
        
        return application;
    }
    
}
