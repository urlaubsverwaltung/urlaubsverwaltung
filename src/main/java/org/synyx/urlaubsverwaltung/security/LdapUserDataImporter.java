package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;


/**
 * Import person data from configured LDAP or Active Directory.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
@ConditionalOnExpression(
    "('${auth}'=='activeDirectory' and '${uv.security.activeDirectory.sync}'=='true') or ('${auth}'=='ldap' and '${uv.security.ldap.sync}'=='true')" // NOSONAR
)
public class LdapUserDataImporter {

    private static final Logger LOG = Logger.getLogger(LdapSyncService.class);

    private final LdapUserService ldapUserService;
    private final LdapSyncService ldapSyncService;
    private final PersonService personService;

    @Autowired
    public LdapUserDataImporter(LdapUserService ldapUserService, LdapSyncService ldapSyncService,
        PersonService personService) {

        this.ldapUserService = ldapUserService;
        this.ldapSyncService = ldapSyncService;
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
                ldapSyncService.syncPerson(optionalPerson.get(), firstName, lastName, email);
            } else {
                ldapSyncService.createPerson(username, firstName, lastName, email);
            }
        }

        LOG.info("DONE LDAP SYNC ------------------------------------------------------------------------------------");
    }
}
