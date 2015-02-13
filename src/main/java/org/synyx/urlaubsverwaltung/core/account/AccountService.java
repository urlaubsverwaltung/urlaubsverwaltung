
package org.synyx.urlaubsverwaltung.core.account;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * Provides services related to {@link Account} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface AccountService {

    /**
     * Just saves the given {@link Account}.
     *
     * @param  account {@link Account}
     */
    void save(Account account);


    /**
     * Gets the {@link Account} for the given year and person.
     *
     * @param  year  int
     * @param  person {@link Person}
     *
     * @return  {@link Account} for the given params
     */
    Account getHolidaysAccount(int year, Person person);


    /**
     * Creates a {@link Account} with the given params.
     *
     * @param  person {@link Person}
     * @param  validFrom {@link DateMidnight}, e.g. 1.1.2012
     * @param  validTo {@link DateMidnight}, e.g. 31.12.2012
     * @param  days {@link BigDecimal}   number of annual vacation days (method calculates the actual vacation days
     *               using the validity period)
     * @param  remaining {@link BigDecimal} number of remaining vacation days
     * @param  remainingDaysExpire  boolean true if remaining vacation days expire on 1st April, else false
     */
    void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire);


    /**
     * Try to get the {@link Account} for the given year and person, if it doesn't exist, try to touch a new
     * {@link Account} with the information of the {@link Account} of the last year. (@param year - 1) If a new account
     * is created, the attribute remainingVacationDaysExpire is set to true. (was desired by the office as default
     * value)
     *
     * @param  year  int
     * @param  person {@link Person}
     *
     * @return  {@link Account} for the given year and person, if not existing yet touch it at first
     */
    Account getOrCreateNewAccount(int year, Person person);


    /**
     * Edits the given {@link Account} with the given params.
     *
     * @param  account {@link Account} to be edited
     * @param  validFrom {@link DateMidnight}, e.g. 1.1.2012
     * @param  validTo {@link DateMidnight}, e.g. 31.12.2012
     * @param  days {@link BigDecimal} number of annual vacation days (method calculates the actual vacation days using
     *               the validity period)
     * @param  remaining {@link BigDecimal} number of remaining vacation days
     * @param  remainingDaysExpire  boolean true if remaining vacation days expire on 1st April, else false
     */
    void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire);


    /**
     * Method to calculate the actual vacation days of an {@link Account}: (months * annual vacation days) / months per
     * year e.g.: (5 months * 28 days)/12 = 11.6666 = 12 Please notice following rounding rules: 11.1 --> 11.0 11.3 -->
     * 11.5 11.6 --> 12.0
     *
     * @param  account {@link Account}
     *
     * @return  {@link BigDecimal} the number of actual vacation days for the given {@link Account}
     */
    BigDecimal calculateActualVacationDays(Account account);
}
