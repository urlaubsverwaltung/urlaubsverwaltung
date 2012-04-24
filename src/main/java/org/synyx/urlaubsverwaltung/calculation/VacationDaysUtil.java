/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import java.math.BigDecimal;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author Aljona Murygina
 */
public class VacationDaysUtil {
    
    private ApplicationDAO applicationDAO;

    public VacationDaysUtil(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }
    
    
    BigDecimal getDaysBeforeApril(Person person, int year) {
        
        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
        
        // get only the days of applications where status is WAITING or ALLOWED and where vacation type is HOLIDAY
        BigDecimal days = applicationDAO.countDaysBeforeApril(person, firstDayOfYear.toDate(), lastDayOfYear.toDate());
        
        return days;
    }
    
    BigDecimal getDaysAfterApril(Person person, int year) {
        
        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
        
        // get only the days of applications where status is WAITING or ALLOWED and where vacation type is HOLIDAY
        BigDecimal days = applicationDAO.countDaysAfterApril(person, firstDayOfYear.toDate(), lastDayOfYear.toDate());
        
        return days;
    }
    
}
