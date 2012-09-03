
package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class HolidaysAccountServiceImpl implements HolidaysAccountService {

    @Autowired
    private AccountDAO accountDAO;
    
    @Override
    public List<Account> getHolidaysAccounts(int year, Person person) {
        return accountDAO.getHolidaysAccountsByYearAndPerson(year, person);
    }
    
    @Override
    public void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire) {
        
        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), days, remaining, remainingDaysExpire);
        account.calculateActualVacationDays();
        accountDAO.save(account);
        
    }

    @Override
    public void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire) {
        
        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setYear(validFrom.getYear());
        account.setAnnualVacationDays(days);
        account.setRemainingVacationDays(remaining);
        account.setRemainingVacationDaysExpire(remainingDaysExpire);
        account.calculateActualVacationDays();
        
        accountDAO.save(account);
    }


    @Override
    public void updateHolidaysAccounts(List<Person> persons, int year) {
        // TODO write method update account
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
