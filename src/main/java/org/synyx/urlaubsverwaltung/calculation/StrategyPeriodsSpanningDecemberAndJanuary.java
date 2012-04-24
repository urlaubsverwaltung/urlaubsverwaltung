/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import java.math.BigDecimal;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;

/**
 *
 * @author Aljona Murygina
 */
public class StrategyPeriodsSpanningDecemberAndJanuary {
    
     private OwnCalendarService calendarService;

    public StrategyPeriodsSpanningDecemberAndJanuary(OwnCalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    Application calculate(Application application) {
        
        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        
        BigDecimal daysBeforeApril = calendarService.getVacationDays(application, new DateMidnight(endDate.getYear(), DateTimeConstants.JANUARY, 1), endDate);
        BigDecimal daysAfterApril = calendarService.getVacationDays(application, startDate, new DateMidnight(startDate.getYear(), DateTimeConstants.DECEMBER, 31));
        
        BigDecimal days = daysBeforeApril.add(daysAfterApril);
        
        application.setDaysBeforeApril(daysBeforeApril);
        application.setDaysAfterApril(daysAfterApril);
        application.setDays(days);
        
        return application;
    }
    
    
}
