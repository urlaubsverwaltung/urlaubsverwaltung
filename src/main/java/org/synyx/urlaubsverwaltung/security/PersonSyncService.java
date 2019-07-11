package org.synyx.urlaubsverwaltung.security;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Syncs the person data from configured LDAP.
 *
 * @deprecated Please use {@link PersonService} directly.
 * @see PersonService
 */
@Transactional
@Deprecated
public class PersonSyncService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;

    public PersonSyncService(PersonService personService) {

        this.personService = personService;
    }

    /**
     * Sync the data of the given {@link Person}.
     *
     * @param person      to update the attributes for
     * @param firstName   to be updated, is optional
     * @param lastName    to be updated, is optional
     * @param mailAddress to be updated, is optional
     * @return the updated person
     */
    public Person syncPerson(Person person, Optional<String> firstName, Optional<String> lastName,
                      Optional<String> mailAddress) {

        firstName.ifPresent(person::setFirstName);
        lastName.ifPresent(person::setLastName);
        mailAddress.ifPresent(person::setEmail);

        final Person savedPerson = personService.save(person);
        LOG.info("Successfully synced person data: {}", savedPerson);

        return savedPerson;
    }


    /**
     * Creates a {@link Person} with the role {@link Role#USER} resp. with the roles {@link Role#USER} and
     * {@link Role#OFFICE} if this is the first person that is created.
     *
     * @param login       of the person to be created, is mandatory to create a person
     * @param firstName   of the person to be created, is optional
     * @param lastName    of the person to be created, is optional
     * @param mailAddress of the person to be created, is optional
     * @return the created person
     */
    public Person createPerson(String login, Optional<String> firstName, Optional<String> lastName,
                        Optional<String> mailAddress) {

        Assert.notNull(login, "Missing login name!");

        final Person person = personService.create(login, lastName.orElse(null), firstName.orElse(null),
            mailAddress.orElse(null), singletonList(NOTIFICATION_USER), singletonList(USER));

        LOG.info("Successfully auto-created person: {}", person);

        return person;
    }


    /**
     * Adds {@link Role#OFFICE} to the roles of the given person if no
     * other active user with a office role is defined.
     *
     * @param person that maybe gets the role {@link Role#OFFICE}
     *
     * @return saved {@link Person} with {@link Role#OFFICE} rights
     * if no other active person with {@link Role#OFFICE} is available.
     */
    public Person appointAsOfficeUserIfNoOfficeUserPresent(Person person) {

        boolean activeOfficeUserAvailable = !personService.getActivePersonsByRole(OFFICE).isEmpty();
        if (activeOfficeUserAvailable) {
            return person;
        }

        final List<Role> permissions = new ArrayList<>(person.getPermissions());
        permissions.add(OFFICE);
        person.setPermissions(permissions);

        final Person savedPerson = personService.save(person);

        LOG.info("Add 'OFFICE' role to person: {}", person);

        return savedPerson;
    }
}
