package org.synyx.urlaubsverwaltung.account;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;

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
     * Creates an {@link AccountDraft} for the given year and person.
     * This may consider information of the persons {@link Account} of the previous year.
     *
     * <p>
     * Note that this does not take care about whether the person has an actual {@link Account} for this year or not.
     * </p>
     *
     * @param year   to get the holidays account draft for
     * @param person to get the holiday account draft for
     * @return {@link AccountDraft}
     */
    AccountDraft createHolidaysAccountDraft(int year, Person person);

    /**
     * Get existing {@link Account}s for the given year and persons.
     *
     * @param year    to get the holidays account for
     * @param persons to get the holidays account for
     * @return {@link Account}s that matches the given parameters.
     */
    List<Account> getHolidaysAccount(int year, List<Person> persons);

    /**
     * Saves the given {@link Account}.
     *
     * @param account to be saved
     * @return saved {@link Account}
     */
    Account save(Account account);

    /**
     * Deletes all {@link Account}s in the database of person id.
     *
     * @param person the person whose account should be deleted
     */
    void deleteAllByPerson(Person person);

    /**
     * Get all {@link Account}s for the given person.
     *
     * @param person to get the holidays accounts for
     * @return {@link Account}s that matches the given parameters.
     */
    List<Account> getHolidaysAccountsByPerson(Person person);
}
