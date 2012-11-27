
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface HolidaysAccountService {

    /**
     * Just saves the given {@link Account}.
     *
     * @param  account
     */
    void save(Account account);


    /**
     * Gets the {@link Account} for the given year and person.
     *
     * @param  year
     * @param  person
     *
     * @return  {@link Account} for the given params
     */
    Account getHolidaysAccount(int year, Person person);


    /**
     * Creates a {@link Account} with the given params.
     *
     * @param  person {@link Person}
     * @param  validFrom  DateMidnight, e.g. 1.1.2012
     * @param  validTo  DateMidnight, e.g. 31.12.2012
     * @param  days  number of annual vacation days (method calculates the actual vacation days using the validity
     *               period)
     * @param  remaining  number of remaining vacation days
     * @param  remainingDaysExpire  true if remaining vacation days expire on 1st April, else false
     */
    void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire);


    /**
     * Try to get the {@link Account} for the given year and person, if it doesn't exist, try to create a new
     * {@link Account} with the information of the {@link Account} of the last year. (@param year - 1)
     *
     * @param  year
     * @param  person
     *
     * @return
     */
    Account getOrCreateNewAccount(int year, Person person);


    /**
     * Edits the given {@link Account} with the given params.
     *
     * @param  account {@link Account} to be edited
     * @param  validFrom  DateMidnight, e.g. 1.1.2012
     * @param  validTo  DateMidnight, e.g. 31.12.2012
     * @param  days  number of annual vacation days (method calculates the actual vacation days using the validity
     *               period)
     * @param  remaining  number of remaining vacation days
     * @param  remainingDaysExpire  true if remaining vacation days expire on 1st April, else false
     */
    void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire);


    /**
     * Method to calculate the actual vacation days: (months * annual vacation days) / months per year e.g.: (5 months *
     * 28 days)/12 = 11.6666 = 12
     *
     * <p>Please notice following rounding rules: 11.1 --> 11.0 11.3 --> 11.5 11.6 --> 12.0</p>
     */
    BigDecimal calculateActualVacationDays(Account account);
}
