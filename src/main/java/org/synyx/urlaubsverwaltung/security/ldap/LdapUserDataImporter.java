package org.synyx.urlaubsverwaltung.security.ldap;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Import person data from configured LDAP or Active Directory.
 */
@Transactional
public class LdapUserDataImporter {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final List<MailNotification> DEFAULT_MAIL_NOTIFICATIONS = List.of(
        NOTIFICATION_EMAIL_APPLICATION_APPLIED,
        NOTIFICATION_EMAIL_APPLICATION_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_REVOKED,
        NOTIFICATION_EMAIL_APPLICATION_REJECTED,
        NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED,
        NOTIFICATION_EMAIL_APPLICATION_CANCELLATION,
        NOTIFICATION_EMAIL_APPLICATION_EDITED,
        NOTIFICATION_EMAIL_APPLICATION_CONVERTED,
        NOTIFICATION_EMAIL_APPLICATION_UPCOMING,
        NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT,
        NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING
    );

    private final LdapUserService ldapUserService;
    private final PersonService personService;

    LdapUserDataImporter(LdapUserService ldapUserService, PersonService personService) {
        this.ldapUserService = ldapUserService;
        this.personService = personService;
    }

    @PostConstruct
    void sync() {
        LOG.info("STARTING DIRECTORY SERVICE SYNC --------------------------------------------------------------------------------");

        final List<LdapUser> users = ldapUserService.getLdapUsers();
        LOG.info("Found {} user(s)", users.size());

        for (LdapUser user : users) {
            final String username = user.getUsername();
            final String firstName = user.getFirstName();
            final String lastName = user.getLastName();
            final String email = user.getEmail();

            final Optional<Person> optionalPerson = personService.getPersonByUsername(username);
            if (optionalPerson.isPresent()) {
                final Person person = optionalPerson.get();
                person.setFirstName(firstName);
                person.setLastName(lastName);
                person.setEmail(email);
                personService.update(person);
            } else {
                personService.create(username, lastName, firstName, email, DEFAULT_MAIL_NOTIFICATIONS, List.of(USER));
            }
        }

        LOG.info("DONE DIRECTORY SERVICE SYNC ------------------------------------------------------------------------------------");
    }
}
