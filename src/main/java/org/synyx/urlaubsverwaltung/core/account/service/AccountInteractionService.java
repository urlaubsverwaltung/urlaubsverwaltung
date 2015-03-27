
package org.synyx.urlaubsverwaltung.core.account.service;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * Provides interactions with {@link org.synyx.urlaubsverwaltung.core.account.domain.Account}s like creating or editing.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface AccountInteractionService {

    /**
     * Creates a {@link org.synyx.urlaubsverwaltung.core.account.domain.Account} with the given parameters.
     *
     * @param  person  defines the owner of the holidays account
     * @param  validFrom  defines the start of the validity period, e.g. 1.1.2012
     * @param  validTo  defines the end of the validity period, e.g. 31.12.2012
     * @param  days  defines number of annual vacation days (the actual vacation days are calculated using the validity
     *               period)
     * @param  remainingDays  defines the number of remaining vacation days from the last year
     * @param  remainingDaysNotExpiring  defines the number of remaining vacation days that do not expire on 1st April
     *
     * @return  the created holidays account
     */
    Account createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remainingDays, BigDecimal remainingDaysNotExpiring);


    /**
     * Edits the given {@link Account} with the given params.
     *
     * @param  account  to be edited
     * @param  validFrom  defines the start of the validity period, e.g. 1.1.2012
     * @param  validTo  defines the end of the validity period, e.g. 31.12.2012
     * @param  days  defines number of annual vacation days (the actual vacation days are calculated using the validity
     *               period)
     * @param  remainingDays  defines the number of remaining vacation days from the last year
     * @param  remainingDaysNotExpiring  defines the number of remaining vacation days that do not expire on 1st April
     *
     * @return  the updated holidays account
     */
    Account editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remainingDays, BigDecimal remainingDaysNotExpiring);


    /**
     * Auto-creates a new {@link Account} based on the information of the given reference account.
     *
     * @param  referenceAccount  to get the information about annual vacation days, left vacation days etc.
     *
     * @return  the created holidays account
     */
    Account autoCreateHolidaysAccount(Account referenceAccount);


    /**
     * Updates the remaining vacation days of all {@link Account}s that follow the {@link Account} of the given year.
     * Updating is stopped when there is no next year's {@link Account}.
     *
     * @param  year  to start the update for
     * @param  person  to update the remaining vacation days for
     */
    void updateRemainingVacationDays(int year, Person person);
}
