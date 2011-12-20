/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.HolidaysAccountDAO;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.util.CalcUtil;

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

    @Autowired
    public HolidaysAccountServiceImpl(HolidaysAccountDAO holidaysAccountDAO,
        HolidayEntitlementDAO holidaysEntitlementDAO) {

        this.holidaysAccountDAO = holidaysAccountDAO;
        this.holidaysEntitlementDAO = holidaysEntitlementDAO;
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
            HolidayEntitlement entitlement = getHolidayEntitlement(year, person);

            if (entitlement == null) {
                entitlement = newHolidayEntitlement(person, year,
                        getHolidayEntitlement(year - 1, person).getVacationDays());
            }

            saveHolidayEntitlement(entitlement);

            // create new account
            account = newHolidaysAccount(person, entitlement.getVacationDays(), BigDecimal.ZERO, year);
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
        entitlement.setActive(true);

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
        account.setActive(true);

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
     * @see  HolidaysAccountService#getHolidaysAccountByYearOrderedByPersons(int)
     */
    @Override
    public List<HolidaysAccount> getHolidaysAccountByYearOrderedByPersons(int year) {

        return holidaysAccountDAO.getAllHolidaysAccountsByYear(year);
    }


    /**
     * @see  HolidaysAccountService#editHolidayEntitlement(org.synyx.urlaubsverwaltung.domain.Person, int,
     *       java.math.BigDecimal)
     */
    @Override
    public void editHolidayEntitlement(Person person, int year, BigDecimal days) {

        // get current entitlement before editing
        HolidayEntitlement currentEntitlement = getHolidayEntitlement(year, person);
        currentEntitlement.setActive(false);
        saveHolidayEntitlement(currentEntitlement);

        // get current account before editing
        HolidaysAccount currentAccount = getHolidaysAccount(year, person);
        currentAccount.setActive(false);
        saveHolidaysAccount(currentAccount);

        // create new entitlement
        HolidayEntitlement newEntitlement = newHolidayEntitlement(person, year, days);

        HolidaysAccount newAccount = new HolidaysAccount();

        // how many days has person used this year?
        BigDecimal usedDays = currentEntitlement.getVacationDays().subtract(currentAccount.getVacationDays());

        // check if person may take a holiday with the edited entitlement or not
        if (usedDays.compareTo(newEntitlement.getVacationDays()) >= 0) {
            // person is not allowed to take a holiday anymore
            newAccount = newHolidaysAccount(person, BigDecimal.ZERO, BigDecimal.ZERO, year);
        } else {
            BigDecimal vac = newEntitlement.getVacationDays().subtract(usedDays);
            newAccount = newHolidaysAccount(person, vac, BigDecimal.ZERO, year);
        }

        saveHolidayEntitlement(newEntitlement);
        saveHolidaysAccount(newAccount);
    }


    /**
     * @see  HolidaysAccountService#updateHolidayEntitlement(java.util.List, int)
     */
    @Override
    public void updateHolidayEntitlement(List<Person> persons, int year) {

        // it's the first January...

        for (Person person : persons) {
            // check if there is an existing entitlement
            HolidayEntitlement entitlement = getHolidayEntitlement(year, person);

            if (entitlement == null) {
                entitlement = newHolidayEntitlement(person, year,
                        getHolidayEntitlement(year - 1, person).getVacationDays());
            }

            // get holidays account of last year to check how many vacation days are left over
            HolidaysAccount accountLastYear = getHolidaysAccount(year - 1, person);

            // get holidays account of current year to update vacation days and remaining vacation days
            HolidaysAccount accountCurrentYear = getHolidaysAccount(year, person);

            if (CalcUtil.isGreaterThanZero(accountLastYear.getVacationDays())) {
                entitlement.setRemainingVacationDays(accountLastYear.getVacationDays());

                BigDecimal vac = accountCurrentYear.getVacationDays().add(entitlement.getRemainingVacationDays());

                // if vacation days > entitlement
                if (vac.compareTo(entitlement.getVacationDays()) == 1) {
                    accountCurrentYear.setVacationDays(entitlement.getVacationDays());
                    accountCurrentYear.setRemainingVacationDays(vac.subtract(entitlement.getVacationDays()));
                } else {
                    // if vacation days <= entitlement, number of remaining vacation days is zero
                    accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
                }

                saveHolidaysAccount(accountCurrentYear);
            } else {
                // no vacation days left over
                // that means: no remaining vacation days for current year
                entitlement.setRemainingVacationDays(BigDecimal.ZERO);
            }

            saveHolidayEntitlement(entitlement);
        }
    }
}
