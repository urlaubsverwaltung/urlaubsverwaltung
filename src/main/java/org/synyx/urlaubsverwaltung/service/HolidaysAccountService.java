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
     * get HolidaysAccount by year and person and if account not existent, creates a new one
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    HolidaysAccount getAccountOrCreateOne(int year, Person person);


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
     * creates a new HolidayEntitlement for a person with params year and number of days
     *
     * @param  person
     * @param  year
     * @param  days
     * @param  remaining
     */
    HolidayEntitlement newHolidayEntitlement(Person person, int year, BigDecimal days, BigDecimal remaining);


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
    HolidaysAccount newHolidaysAccount(Person person, int year, BigDecimal vacDays, BigDecimal remainingVacDays,
        boolean remainingDaysExpire);


    /**
     * method to edit an existent entitlement and to save the updated entitlement
     *
     * @param  person
     * @param  year
     * @param  days
     */
    void editHolidayEntitlement(HolidayEntitlement entitlement, BigDecimal days, BigDecimal remaining);


    /**
     * method to edit an existent holidays account and to save the updated holidays account
     *
     * @param  account
     * @param  days
     * @param  remaining
     */
    void editHolidaysAccount(HolidaysAccount account, BigDecimal days, BigDecimal remaining,
        boolean remainingDaysExpire);


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
}
