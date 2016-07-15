package org.synyx.urlaubsverwaltung.web.person;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;


/**
 * Process {@link PersonForm} to create/edit {@link Person} with holidays account, working time etc.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface PersonFormProcessor {

    /**
     * Creates a {@link Person} with the values of the given {@link org.synyx.urlaubsverwaltung.web.person.PersonForm}
     * incl. creating/updating {@link org.synyx.urlaubsverwaltung.core.account.domain.Account} and {@link WorkingTime}.
     *
     * @param  personForm  contains information about person attributes and working time and holidays account
     *                     information
     */
    Person create(PersonForm personForm);


    /**
     * Updates a person with the values of the given {@link org.synyx.urlaubsverwaltung.web.person.PersonForm} incl.
     * creating/updating {@link org.synyx.urlaubsverwaltung.core.account.domain.Account} and {@link WorkingTime}.
     *
     * @param  personForm  contains information about person attributes and working time and holidays account
     *                     information
     */
    Person update(PersonForm personForm);
}
