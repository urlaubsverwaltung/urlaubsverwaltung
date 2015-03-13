package org.synyx.urlaubsverwaltung.core.account;

import org.synyx.urlaubsverwaltung.core.person.Person;


/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.core.account.Account} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface AccountService {

    /**
     * Gets the {@link Account} for the given year and person.
     *
     * @param  year  to get the holidays account for
     * @param  person  to get the holidays account for
     *
     * @return  {@link Account} that matches the given parameters
     */
    Account getHolidaysAccount(int year, Person person);


    /**
     * Saves the given {@link Account}.
     *
     * @param  account  to be saved
     */
    void save(Account account);
}
