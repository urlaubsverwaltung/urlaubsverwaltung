package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;


/**
 * Syncs the person data from configured LDAP.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
@ConditionalOnExpression("'${auth}'=='activeDirectory' or '${auth}'=='ldap'")
public class LdapSyncService {

    private static final Logger LOG = Logger.getLogger(LdapSyncService.class);

    private final LdapUserService ldapUserService;
    private final PersonService personService;

    @Autowired
    public LdapSyncService(LdapUserService ldapUserService, PersonService personService) {

        this.ldapUserService = ldapUserService;
        this.personService = personService;
    }

    // Sync LDAP/AD data during startup and every night at 01:00 am
    @PostConstruct
    @Scheduled(cron = "0 0 1 * * ?")
    public void sync() {

        LOG.info("STARTING LDAP SYNC --------------------------------------------------------------------------------");

        List<LdapUser> users = ldapUserService.getLdapUsers();

        LOG.info("Found " + users.size() + " user(s)");

        for (LdapUser user : users) {
            String username = user.getUsername();
            Optional<String> firstName = user.getFirstName();
            Optional<String> lastName = user.getLastName();
            Optional<String> email = user.getEmail();

            Optional<Person> optionalPerson = personService.getPersonByLogin(username);

            if (optionalPerson.isPresent()) {
                syncPerson(optionalPerson.get(), firstName, lastName, email);
            } else {
                createPerson(username, firstName, lastName, email);
            }
        }

        LOG.info("DONE LDAP SYNC ------------------------------------------------------------------------------------");
    }


    /**
     * Sync the data of the given {@link Person}.
     *
     * @param  person  to update the attributes for
     * @param  firstName  to be updated, is optional
     * @param  lastName  to be updated, is optional
     * @param  mailAddress  to be updated, is optional
     *
     * @return  the updated person
     */
    Person syncPerson(Person person, Optional<String> firstName, Optional<String> lastName,
        Optional<String> mailAddress) {

        firstName.ifPresent(person::setFirstName);
        lastName.ifPresent(person::setLastName);
        mailAddress.ifPresent(person::setEmail);

        personService.save(person);

        LOG.info("Successfully synced person data: " + person.toString());

        return person;
    }


    /**
     * Creates a {@link Person} with the role {@link Role#USER} resp. with the roles {@link Role#USER} and
     * {@link Role#OFFICE} if this is the first person that is created.
     *
     * @param  login  of the person to be created, is mandatory to create a person
     * @param  firstName  of the person to be created, is optional
     * @param  lastName  of the person to be created, is optional
     * @param  mailAddress  of the person to be created, is optional
     *
     * @return  the created person
     */
    Person createPerson(String login, Optional<String> firstName, Optional<String> lastName,
        Optional<String> mailAddress) {

        Assert.notNull(login, "Missing login name!");

        Person person = personService.create(login, lastName.orElse(null), firstName.orElse(null),
                mailAddress.orElse(null), Collections.singletonList(MailNotification.NOTIFICATION_USER),
                Collections.singletonList(Role.USER));

        LOG.info("Successfully auto-created person: " + person.toString());

        return person;
    }


    /**
     * Adds {@link Role#OFFICE} to the roles of the given person.
     *
     * @param  person  that gets the role {@link Role#OFFICE}
     */
    void appointPersonAsOfficeUser(Person person) {

        List<Role> permissions = new ArrayList<>(person.getPermissions());
        permissions.add(Role.OFFICE);

        person.setPermissions(permissions);

        personService.save(person);

        LOG.info("Add 'OFFICE' to roles of person: " + person.toString());
    }
}
