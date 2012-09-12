/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateMidnight;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface HolidaysAccountService {
 
    Account getHolidaysAccount(int year, Person person);
    
    void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire);
    
    void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire);
    
    void updateHolidaysAccounts(List<Person> persons, int year);
    
    /**
     * Method to calculate the actual vacation days: (months * annual vacation days) / months per year
     * e.g.: (5 months * 28 days)/12 = 11.6666 = 12
     * 
     * Please notice following rounding rules:
     * 11.1 --> 11.0
     * 11.3 --> 11.5
     * 11.6 --> 12.0
     */
    BigDecimal calculateActualVacationDays(Account account);
    
}
