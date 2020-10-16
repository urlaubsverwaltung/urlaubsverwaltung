package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;


/**
 * Provides access to {@link Account} entities.
 */
public interface AccountService {

    /**
     * Gets the {@link Account} for the given year and person.
     *
     * @param year   to get the holidays account for
     * @param person to get the holidays account for
     * @return optional of {@link Account} that matches the given
     * parameters
     */
    Optional<Account> getHolidaysAccount(int year, Person person);


    /**
     * Saves the given {@link Account}.
     *
     * @param account to be saved
     * @return saved {@link Account}
     */
    Account save(Account account);
}
