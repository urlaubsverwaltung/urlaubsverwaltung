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
 
    List<Account> getHolidaysAccounts(int year, Person person);
    
    void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire);
    
    void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire);
    
    void updateHolidaysAccounts(List<Person> persons, int year);
    
    
}
