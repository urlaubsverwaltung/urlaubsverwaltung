package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Months;

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
        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);
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
        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);

        accountDAO.save(account);

        LOG.info("Edited holidays account of " + account.getPerson().getLoginName() + " with following values: " + "{validFrom: " + validFrom.toString("dd.MM.yyyy") + ", validTo: " + validTo.toString("dd.MM.yyyy") + ", annualVacationDays: " + days + ", remainingDays: " + remaining + ", remainingDaysExpiring: " + remainingDaysExpire + "}");
    }

    /**
     * Method to calculate the actual vacation days: (months * annual vacation days) / months per year
     * e.g.: (5 months * 28 days)/12 = 11.6666 = 12
     * 
     * Please notice following rounding rules:
     * 11.1 --> 11.0
     * 11.3 --> 11.5
     * 11.6 --> 12.0
     */
    @Override
    public BigDecimal calculateActualVacationDays(Account account) {
        
        int months = Months.monthsBetween(new DateTime(account.getValidFrom()).toDateMidnight(), new DateTime(account.getValidTo()).toDateMidnight()).getMonths() + 1;

        double unroundedVacationDays = (months * account.getAnnualVacationDays().doubleValue()) / 12;
        BigDecimal bd = new BigDecimal(unroundedVacationDays).setScale(2, RoundingMode.HALF_UP);

        String bdString = bd.toString();
        bdString = bdString.split("\\.")[1];
        Integer referenceValue = Integer.parseInt(bdString);
        BigDecimal days;

        // please notice: bd.intValue() is an Integer, e.g. 11
        int bdIntValue = bd.intValue();

        if (referenceValue > 0 && referenceValue < 30) {

            days = new BigDecimal(bdIntValue);

        } else if (referenceValue >= 30 && referenceValue < 50) {

            days = new BigDecimal(bdIntValue + 0.5);

        } else if (referenceValue >= 50) {

            days = new BigDecimal(bdIntValue + 1);

        } else {
            // default fallback because I'm a scaredy cat
            days = new BigDecimal(unroundedVacationDays).setScale(2);
        }
        
        return days;
    }

    @Override
    public void updateHolidaysAccounts(List<Person> persons, int year) {
        // TODO write method update account
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
