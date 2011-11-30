/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.HolidaysAccountDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Transactional
public class HolidaysAccountServiceImpl implements HolidaysAccountService {

    private HolidaysAccountDAO holidaysAccountDAO;
    private HolidayEntitlementDAO holidaysEntitlementDAO;
    private CalculationService calculationService;

    @Autowired
    public HolidaysAccountServiceImpl(HolidaysAccountDAO holidaysAccountDAO,
        HolidayEntitlementDAO holidaysEntitlementDAO, CalculationService calculationService) {

        this.holidaysAccountDAO = holidaysAccountDAO;
        this.holidaysEntitlementDAO = holidaysEntitlementDAO;
        this.calculationService = calculationService;
    }

    /**
     * @see  HolidaysAccountService#getHolidayEntitlement(int, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public HolidayEntitlement getHolidayEntitlement(int year, Person person) {

        return holidaysEntitlementDAO.getHolidayEntitlementByYearAndPerson(year, person);
    }


    /**
     * @see  HolidaysAccountService#getHolidaysAccount(int, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public HolidaysAccount getHolidaysAccount(int year, Person person) {

        return holidaysAccountDAO.getHolidaysAccountByYearAndPerson(year, person);
    }


    /**
     * @see  HolidaysAccountService#getHolidaysAccountsForYear(int)
     */
    @Override
    public List<HolidaysAccount> getHolidaysAccountsForYear(int year) {

        return holidaysAccountDAO.getAllHolidaysAccountsByYear(year);
    }


    /**
     * @see  HolidaysAccountService#getAccountOrCreateOne(int, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public HolidaysAccount getAccountOrCreateOne(int year, Person person) {

        HolidaysAccount account = holidaysAccountDAO.getHolidaysAccountByYearAndPerson(year, person);

        // if account not yet existent
        if (account == null) {
            // create new account
            account = newHolidaysAccount(person, getHolidayEntitlement(year, person).getVacationDays(), BigDecimal.ZERO,
                    year);
        }

        return account;
    }


    /**
     * @see  HolidaysAccountService#newHolidayEntitlement(org.synyx.urlaubsverwaltung.domain.Person, int,
     *       java.math.BigDecimal)
     */
    @Override
    public HolidayEntitlement newHolidayEntitlement(Person person, int year, BigDecimal days) {

        HolidayEntitlement entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);
        entitlement.setVacationDays(days);
        entitlement.setYear(year);

        holidaysEntitlementDAO.save(entitlement);

        return entitlement;
    }


    /**
     * @see  HolidaysAccountService#newHolidaysAccount(org.synyx.urlaubsverwaltung.domain.Person, java.math.BigDecimal,
     *       java.math.BigDecimal, int)
     */
    @Override
    public HolidaysAccount newHolidaysAccount(Person person, BigDecimal vacDays, BigDecimal remainingVacDays,
        int year) {

        HolidaysAccount account = new HolidaysAccount();

        account.setPerson(person);
        account.setRemainingVacationDays(remainingVacDays);
        account.setVacationDays(vacDays);
        account.setYear(year);

        holidaysAccountDAO.save(account);

        return account;
    }


    /**
     * @see  HolidaysAccountService#saveHolidayEntitlement(org.synyx.urlaubsverwaltung.domain.HolidayEntitlement)
     */
    @Override
    public void saveHolidayEntitlement(HolidayEntitlement entitlement) {

        holidaysEntitlementDAO.save(entitlement);
    }


    /**
     * @see  HolidaysAccountService#saveHolidaysAccount(org.synyx.urlaubsverwaltung.domain.HolidaysAccount)
     */
    @Override
    public void saveHolidaysAccount(HolidaysAccount account) {

        holidaysAccountDAO.save(account);
    }


    /**
     * @see  HolidaysAccountService#rollbackUrlaub(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void rollbackUrlaub(Application application) {

        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();
        Person person = application.getPerson();

        // if start date != end date, special case January has to be noticed
        if (start.getYear() != end.getYear()) {
            HolidaysAccount accountCurrentYear = getHolidaysAccount(start.getYear(), person);
            BigDecimal entitlementCurrentYear = getHolidayEntitlement(start.getYear(), person).getVacationDays();

            HolidaysAccount accountNextYear = getHolidaysAccount(end.getYear(), person);
            BigDecimal entitlementNextYear = getHolidayEntitlement(end.getYear(), person).getVacationDays();

            calculationService.rollbackNoticeJanuary(application, accountCurrentYear, accountNextYear,
                entitlementCurrentYear, entitlementNextYear);

            saveHolidaysAccount(accountCurrentYear);
            saveHolidaysAccount(accountNextYear);
        } else {
            // special case April has to be noticed
            HolidaysAccount account = getHolidaysAccount(start.getYear(), person);
            BigDecimal entitlement = getHolidayEntitlement(start.getYear(), person).getVacationDays();

            calculationService.rollbackNoticeApril(application, account, entitlement);

            saveHolidaysAccount(account);
        }
    }
}
