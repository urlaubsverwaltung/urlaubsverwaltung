package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapUserDataImporterTest {

    private LdapUserDataImporter ldapUserDataImporter;

    private LdapUserService ldapUserServiceMock;
    private LdapSyncService ldapSyncServiceMock;
    private PersonService personServiceMock;

    @Before
    public void setUp() {

        ldapUserServiceMock = Mockito.mock(LdapUserService.class);
        ldapSyncServiceMock = Mockito.mock(LdapSyncService.class);
        personServiceMock = Mockito.mock(PersonService.class);

        ldapUserDataImporter = new LdapUserDataImporter(ldapUserServiceMock, ldapSyncServiceMock, personServiceMock);
    }


    @Test
    public void ensureFetchesLdapUsers() {

        ldapUserDataImporter.sync();

        verify(ldapUserServiceMock).getLdapUsers();
    }


    @Test
    public void ensureCreatesPersonIfLdapUserNotYetExists() {

        when(personServiceMock.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());
        when(ldapUserServiceMock.getLdapUsers())
            .thenReturn(Collections.singletonList(
                    new LdapUser("muster", Optional.empty(), Optional.empty(), Optional.empty())));

        ldapUserDataImporter.sync();

        verify(personServiceMock, Mockito.times(1)).getPersonByLogin("muster");
        verify(ldapSyncServiceMock)
            .createPerson("muster", Optional.empty(), Optional.empty(), Optional.empty());
    }


    @Test
    public void ensureUpdatesPersonIfLdapUserExists() {

        Person person = TestDataCreator.createPerson();

        when(personServiceMock.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));
        when(ldapUserServiceMock.getLdapUsers())
            .thenReturn(Collections.singletonList(
                    new LdapUser(person.getLoginName(), Optional.of("Vorname"), Optional.of("Nachname"),
                        Optional.of("Email"))));

        ldapUserDataImporter.sync();

        verify(personServiceMock, Mockito.times(1)).getPersonByLogin(person.getLoginName());
        verify(ldapSyncServiceMock)
            .syncPerson(person, Optional.of("Vorname"), Optional.of("Nachname"), Optional.of("Email"));
    }
}
