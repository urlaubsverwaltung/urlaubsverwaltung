package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@RunWith(MockitoJUnitRunner.class)
public class LdapUserDataImporterTest {

    private LdapUserDataImporter sut;

    @Mock
    private LdapUserService ldapUserServiceMock;
    @Mock
    private PersonService personServiceMock;

    @Before
    public void setUp() {
        sut = new LdapUserDataImporter(ldapUserServiceMock, personServiceMock);
    }

    @Test
    public void ensureFetchesLdapUsers() {

        sut.sync();

        verify(ldapUserServiceMock).getLdapUsers();
    }

    @Test
    public void ensureCreatesPersonIfLdapUserNotYetExists() {

        when(personServiceMock.getPersonByUsername(anyString())).thenReturn(Optional.empty());
        when(ldapUserServiceMock.getLdapUsers()).thenReturn(singletonList(new LdapUser("muster", Optional.empty(), Optional.empty(), Optional.empty())));

        sut.sync();

        verify(personServiceMock, times(1)).getPersonByUsername("muster");
        verify(personServiceMock)
            .create("muster", null, null, null, singletonList(NOTIFICATION_USER), singletonList(USER));
    }

    @Test
    public void ensureUpdatesPersonIfLdapUserExists() {

        final Person person = TestDataCreator.createPerson();

        when(personServiceMock.getPersonByUsername(anyString())).thenReturn(Optional.of(person));
        when(ldapUserServiceMock.getLdapUsers())
            .thenReturn(singletonList(new LdapUser(person.getUsername(), Optional.of("Vorname"), Optional.of("Nachname"), Optional.of("Email"))));

        sut.sync();

        verify(personServiceMock, times(1)).getPersonByUsername(person.getUsername());
        assertThat(person.getEmail()).isEqualTo("Email");
        assertThat(person.getFirstName()).isEqualTo("Vorname");
        assertThat(person.getLastName()).isEqualTo("Nachname");
        verify(personServiceMock).save(person);
    }
}
