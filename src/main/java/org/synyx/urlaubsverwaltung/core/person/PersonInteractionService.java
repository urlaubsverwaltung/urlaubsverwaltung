package org.synyx.urlaubsverwaltung.core.person;

import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.util.Locale;


/**
 * Provides possibility to create/edit {@link Person}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface PersonInteractionService {

    /**
     * Creates/Updates a {@link Person} with the values of the given
     * {@link org.synyx.urlaubsverwaltung.web.person.PersonForm} incl. creating/updating
     * {@link org.synyx.urlaubsverwaltung.core.account.Account} and
     * {@link org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime}.
     *
     * @param  person  that will be updated and persisted
     * @param  personForm  contains information about person attributes and working time and holidays account
     *                     information
     * @param  locale  for number formatting
     */
    void createOrUpdate(Person person, PersonForm personForm, Locale locale);


    /**
     * Sets a {@link Person} to inactive, i.e. this {@link Person} has no right to login, to apply for leave, etc. but
     * information about the person remains for archiving.
     *
     * @param  person  to be set to inactive
     */
    void deactivate(Person person);


    /**
     * Sets a {@link Person} to active (e.g. after unintended deactivating of a person), i.e. this {@link Person} has
     * once again his user rights.
     *
     * @param  person  to be set to active
     */
    void activate(Person person);
}
