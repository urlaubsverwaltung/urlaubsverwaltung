/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service.legacy;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.legacy.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.legacy.dao.HolidaysAccountDAO;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidaysAccount;
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
     * @see  HolidaysAccountService#getAccountOrCreateOne(int, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public HolidaysAccount getAccountOrCreateOne(int year, Person person) {

        HolidaysAccount account = holidaysAccountDAO.getHolidaysAccountByYearAndPerson(year, person);

        // if account not yet existent
        if (account == null) {
            HolidayEntitlement entitlement = getHolidayEntitlement(year, person);

            if (entitlement == null) {
                if (year < DateMidnight.now().getYear()) {
                    HolidayEntitlement currentEntitlement = getHolidayEntitlement(DateMidnight.now().getYear(), person);
                    entitlement = newHolidayEntitlement(person, year, currentEntitlement.getAnnualVacationDays(),
                            currentEntitlement.getAnnualVacationDays(), BigDecimal.ZERO);
                } else {
                    HolidayEntitlement lastYearEntitlement = getHolidayEntitlement(year - 1, person);
                    entitlement = newHolidayEntitlement(person, year, lastYearEntitlement.getAnnualVacationDays(),
                            lastYearEntitlement.getAnnualVacationDays(), BigDecimal.ZERO);
                }
            }

            saveHolidayEntitlement(entitlement);

            // create new account
            account = newHolidaysAccount(person, year, entitlement.getVacationDays(), BigDecimal.ZERO, true);
        }

        return account;
    }


    /**
     * @see  HolidaysAccountService#newHolidayEntitlement(org.synyx.urlaubsverwaltung.domain.Person, int,
     *       java.math.BigDecimal, java.math.BigDecimal)
     */
    @Override
    public HolidayEntitlement newHolidayEntitlement(Person person, int year, BigDecimal annualVacationDays,
        BigDecimal days, BigDecimal remaining) {

        HolidayEntitlement entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);
        entitlement.setAnnualVacationDays(annualVacationDays);
        entitlement.setVacationDays(days);
        entitlement.setRemainingVacationDays(remaining);
        entitlement.setYear(year);

        return entitlement;
    }


    /**
     * @see  HolidaysAccountService#newHolidaysAccount(org.synyx.urlaubsverwaltung.domain.Person, int,
     *       java.math.BigDecimal, java.math.BigDecimal, boolean)
     */
    @Override
    public HolidaysAccount newHolidaysAccount(Person person, int year, BigDecimal vacDays, BigDecimal remainingVacDays,
        boolean remainingDaysExpire) {

        HolidaysAccount account = new HolidaysAccount();

        account.setPerson(person);
        account.setRemainingVacationDays(remainingVacDays);
        account.setVacationDays(vacDays);
        account.setYear(year);
        account.setRemainingVacationDaysExpire(remainingDaysExpire);

        return account;
    }


    /**
     * @see  HolidaysAccountService#editHolidayEntitlement(org.synyx.urlaubsverwaltung.domain.HolidayEntitlement,java.math.BigDecimal,
     *       java.math.BigDecimal)
     */
    @Override
    public void editHolidayEntitlement(HolidayEntitlement entitlement, BigDecimal annualVacationDays, BigDecimal days,
        BigDecimal remaining) {

        entitlement.setAnnualVacationDays(annualVacationDays);
        entitlement.setVacationDays(days);
        entitlement.setRemainingVacationDays(remaining);

        saveHolidayEntitlement(entitlement);
    }


    /**
     * @see  HolidaysAccountService#editHolidaysAccount(org.synyx.urlaubsverwaltung.domain.HolidaysAccount,java.math.BigDecimal,
     *       java.math.BigDecimal, boolean)
     */
    @Override
    public void editHolidaysAccount(HolidaysAccount account, BigDecimal days, BigDecimal remaining,
        boolean remainingDaysExpire) {

        account.setVacationDays(days);
        account.setRemainingVacationDays(remaining);
        account.setRemainingVacationDaysExpire(remainingDaysExpire);
        saveHolidaysAccount(account);
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
                HolidayEntitlement lastYearEntitlement = getHolidayEntitlement(year - 1, person);
                entitlement = newHolidayEntitlement(person, year, lastYearEntitlement.getAnnualVacationDays(),
                        lastYearEntitlement.getVacationDays(), lastYearEntitlement.getRemainingVacationDays());
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
