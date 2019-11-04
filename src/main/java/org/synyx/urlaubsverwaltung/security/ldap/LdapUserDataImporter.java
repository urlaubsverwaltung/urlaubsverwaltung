package org.synyx.urlaubsverwaltung.security.ldap;

import org.slf4j.Logger;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Import person data from configured LDAP or Active Directory.
 */
@Transactional
public class LdapUserDataImporter implements SchedulingConfigurer {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final LdapUserService ldapUserService;
    private final PersonService personService;
    private final DirectoryServiceSecurityProperties directoryServiceSecurityProperties;

    LdapUserDataImporter(LdapUserService ldapUserService,
                         PersonService personService, DirectoryServiceSecurityProperties directoryServiceSecurityProperties) {

        this.ldapUserService = ldapUserService;
        this.personService = personService;
        this.directoryServiceSecurityProperties = directoryServiceSecurityProperties;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(this::sync, determinePeriodicExecutionTrigger());
    }

    private Trigger determinePeriodicExecutionTrigger() {
        return new CronTrigger(directoryServiceSecurityProperties.getSync().getCron());
    }

    @PostConstruct
    void sync() {

        LOG.info("STARTING DIRECTORY SERVICE SYNC --------------------------------------------------------------------------------");

        final List<LdapUser> users = ldapUserService.getLdapUsers();

        LOG.info("Found {} user(s)", users.size());

        for (LdapUser user : users) {
            final String username = user.getUsername();
            final Optional<String> firstName = user.getFirstName();
            final Optional<String> lastName = user.getLastName();
            final Optional<String> email = user.getEmail();

            final Optional<Person> optionalPerson = personService.getPersonByUsername(username);

            if (optionalPerson.isPresent()) {

                Person person = optionalPerson.get();

                firstName.ifPresent(person::setFirstName);
                lastName.ifPresent(person::setLastName);
                email.ifPresent(person::setEmail);

                personService.save(person);
            } else {
                personService.create(username, lastName.orElse(null), firstName.orElse(null),
                    email.orElse(null), singletonList(NOTIFICATION_USER), singletonList(USER));
            }
        }

        LOG.info("DONE DIRECTORY SERVICE SYNC ------------------------------------------------------------------------------------");
    }
}
