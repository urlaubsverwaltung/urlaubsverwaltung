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


    @Override
    public List<HolidaysAccount> getHolidaysAccountByYearOrderedByPersons(int year) {

        return holidaysAccountDAO.getHolidaysAccountByYearOrderedByPersons(year);
    }


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
