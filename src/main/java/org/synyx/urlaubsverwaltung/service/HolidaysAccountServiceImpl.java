package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.apache.log4j.Logger;

/**
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class HolidaysAccountServiceImpl implements HolidaysAccountService {

    private static final Logger LOG = Logger.getLogger("audit");
    private AccountDAO accountDAO;

    @Autowired
    public HolidaysAccountServiceImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public Account getHolidaysAccount(int year, Person person) {
        return accountDAO.getHolidaysAccountByYearAndPerson(year, person);
    }

    @Override
    public void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire) {

        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), days, remaining, remainingDaysExpire);
        account.calculateActualVacationDays();
        accountDAO.save(account);

        LOG.info("Created holidays account for " + person.getLoginName() + " with following values: " + "{validFrom: " + validFrom.toString("dd.MM.yyyy") + ", validTo: " + validTo.toString("dd.MM.yyyy") + ", annualVacationDays: " + days + ", remainingDays: " + remaining + ", remainingDaysExpiring: " + remainingDaysExpire + "}");

    }

    @Override
    public void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days, BigDecimal remaining, boolean remainingDaysExpire) {

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(days);
        account.setRemainingVacationDays(remaining);
        account.setRemainingVacationDaysExpire(remainingDaysExpire);
        account.calculateActualVacationDays();

        accountDAO.save(account);
        
        LOG.info("Edited holidays account of " + account.getPerson().getLoginName() + " with following values: " + "{validFrom: " + validFrom.toString("dd.MM.yyyy") + ", validTo: " + validTo.toString("dd.MM.yyyy") + ", annualVacationDays: " + days + ", remainingDays: " + remaining + ", remainingDaysExpiring: " + remainingDaysExpire + "}");
    }

    @Override
    public void updateHolidaysAccounts(List<Person> persons, int year) {
        // TODO write method update account
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
