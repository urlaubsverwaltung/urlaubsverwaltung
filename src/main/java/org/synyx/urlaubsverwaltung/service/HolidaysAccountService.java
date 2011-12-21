package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface HolidaysAccountService {

    /**
     * get HolidayEntitlement for certain year and person
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    HolidayEntitlement getHolidayEntitlement(int year, Person person);


    /**
     * get HolidaysAccount for certain year and person
     *
     * @param  year
     * @param  person
     *
     * @return  only the active(!) holidays account of the given person for the given year
     */
    HolidaysAccount getHolidaysAccount(int year, Person person);


    /**
     * get a list of HolidaysAccount for a certain year
     *
     * @param  year
     *
     * @return
     */
    List<HolidaysAccount> getHolidaysAccountsForYear(int year);


    /**
     * get HolidaysAccount by year and person and if account not existent, creates a new one
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    HolidaysAccount getAccountOrCreateOne(int year, Person person);


    /**
     * creates a new HolidayEntitlement for a person with params year and number of days
     *
     * @param  person
     * @param  year
     * @param  days
     */
    HolidayEntitlement newHolidayEntitlement(Person person, int year, BigDecimal days);


    /**
     * updates an entitlement (respectively the number of this year's entitlement of remaining vacation days) on first
     * of January.
     *
     * @param  person
     * @param  year
     *
     * @return
     */
    void updateHolidayEntitlement(List<Person> persons, int year);


    /**
     * if an existing entitlement is edited, a new entitlement and a new account must be created, the current
     * entitlement and account are set inactive.
     *
     * @param  person
     * @param  year
     * @param  days
     */
    void editHolidayEntitlement(Person person, int year, BigDecimal days);


    /**
     * creates a new HolidaysAccount for a person with params vacation days, resturlaub and year
     *
     * @param  person
     * @param  vacDays
     * @param  remainingVacDays
     * @param  year
     *
     * @return
     */
    HolidaysAccount newHolidaysAccount(Person person, BigDecimal vacDays, BigDecimal remainingVacDays, int year);


    /**
     * saves HolidayEntitlement
     *
     * @param  entitlement
     */
    void saveHolidayEntitlement(HolidayEntitlement entitlement);


    /**
     * saves HolidaysAccount
     *
     * @param  account
     */
    void saveHolidaysAccount(HolidaysAccount account);


    /**
     * get a list of HolidaysAccount by year, list ordered by last names of persons
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    List<HolidaysAccount> getHolidaysAccountByYearOrderedByPersons(int year);
}
