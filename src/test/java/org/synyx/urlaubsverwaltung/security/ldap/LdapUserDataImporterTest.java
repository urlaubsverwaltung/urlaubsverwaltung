package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
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
    void ensurecreatePersonIfLdapUserNotYetExists() {

        final LdapUser ldapUser = new LdapUser("muster", null, null, null, List.of());
        when(personService.getPersonByUsername(ldapUser.getUsername())).thenReturn(Optional.empty());
        when(ldapUserService.getLdapUsers()).thenReturn(List.of(ldapUser));

        sut.sync();

        verify(personService).create("muster", null, null, null, List.of(NOTIFICATION_USER), List.of(USER));
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
        verify(personService).save(person);
    }
}
