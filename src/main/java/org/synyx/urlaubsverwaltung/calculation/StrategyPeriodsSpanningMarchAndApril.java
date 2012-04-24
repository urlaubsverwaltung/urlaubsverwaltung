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
public class StrategyPeriodsSpanningMarchAndApril {
    
    private OwnCalendarService calendarService;

    public StrategyPeriodsSpanningMarchAndApril(OwnCalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    Application calculate(Application application) {
        
        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        
        BigDecimal daysBeforeApril = calendarService.getVacationDays(application, startDate, new DateMidnight(startDate.getYear(), DateTimeConstants.MARCH, 31));
        BigDecimal daysAfterApril = calendarService.getVacationDays(application, new DateMidnight(startDate.getYear(), DateTimeConstants.APRIL, 1), endDate);
        
        BigDecimal days = daysBeforeApril.add(daysAfterApril);
        
        application.setDaysBeforeApril(daysBeforeApril);
        application.setDaysAfterApril(daysAfterApril);
        application.setDays(days);
        
        return application;
        
    }
    
}
