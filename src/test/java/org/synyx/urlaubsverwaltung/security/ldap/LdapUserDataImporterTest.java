package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

@ExtendWith(MockitoExtension.class)
class LdapUserDataImporterTest {

    private LdapUserDataImporter sut;

    @Mock
    private LdapUserService ldapUserService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new LdapUserDataImporter(ldapUserService, personService);
    }

    @Test
    void ensureFetchesLdapUsers() {

        sut.sync();

        verify(ldapUserService).getLdapUsers();
    }

    @Test
    void ensureCreatePersonIfLdapUserNotYetExists() {

        final LdapUser ldapUser = new LdapUser("muster", null, null, null, List.of());
        when(personService.getPersonByUsername(ldapUser.getUsername())).thenReturn(Optional.empty());
        when(ldapUserService.getLdapUsers()).thenReturn(List.of(ldapUser));

        sut.sync();

        final List<MailNotification> defaultMailNotifications = List.of(
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

        verify(personService).create("muster", null, null, null, defaultMailNotifications, List.of(USER));
    }

    @Test
    void ensureUpdatesPersonIfLdapUserExists() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LdapUser ldapUser = new LdapUser(person.getUsername(), "Vorname", "Nachname", "Email", List.of());
        when(personService.getPersonByUsername(ldapUser.getUsername())).thenReturn(Optional.of(person));
        when(ldapUserService.getLdapUsers()).thenReturn(List.of(ldapUser));

        sut.sync();

        assertThat(person.getEmail()).isEqualTo("Email");
        assertThat(person.getFirstName()).isEqualTo("Vorname");
        assertThat(person.getLastName()).isEqualTo("Nachname");
        verify(personService).update(person);
    }
}
