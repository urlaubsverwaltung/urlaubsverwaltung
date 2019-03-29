package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class LdapSyncServiceTest {

    private PersonService personService;

    private LdapSyncService ldapSyncService;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);

        ldapSyncService = new LdapSyncService(personService);
    }


    @Test
    public void ensurePersonIsCreatedWithCorrectAttributes() {

        Person person = TestDataCreator.createPerson();

        when(personService.create(anyString(), anyString(), anyString(), anyString(), anyList(), anyList())).thenReturn(person);

        ldapSyncService.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"));

        verify(personService)
            .create("murygina", "Murygina", "Aljona", "murygina@synyx.de",
                singletonList(MailNotification.NOTIFICATION_USER), singletonList(Role.USER));
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyLoginName() {

        Person person = TestDataCreator.createPerson();

        when(personService.create(anyString(), isNull(), isNull(), isNull(), anyList(), anyList())).thenReturn(person);

        ldapSyncService.createPerson("murygina", Optional.empty(), Optional.empty(), Optional.empty());

        verify(personService)
            .create("murygina", null, null, null, singletonList(MailNotification.NOTIFICATION_USER), singletonList(Role.USER));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoLoginNameIsGiven() {

        ldapSyncService.createPerson(null, Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"));
    }


    @Test
    public void ensureSyncedPersonHasCorrectAttributes() {

        Person person = TestDataCreator.createPerson("muster", "Marlene", "Muster", "marlene@firma.test");

        Person syncedPerson = ldapSyncService.syncPerson(person, Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"));

        verify(personService).save(eq(person));

        Assert.assertNotNull("Missing login name", syncedPerson.getLoginName());
        Assert.assertNotNull("Missing first name", syncedPerson.getFirstName());
        Assert.assertNotNull("Missing last name", syncedPerson.getLastName());
        Assert.assertNotNull("Missing mail address", syncedPerson.getEmail());

        Assert.assertEquals("Wrong login name", "muster", syncedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Aljona", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Murygina", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "murygina@synyx.de", syncedPerson.getEmail());
    }


    @Test
    public void ensureSyncDoesNotEmptyAttributes() {

        Person person = TestDataCreator.createPerson("muster", "Marlene", "Muster", "marlene@firma.test");

        Person syncedPerson = ldapSyncService.syncPerson(person, Optional.empty(), Optional.empty(), Optional.empty());

        verify(personService).save(eq(person));

        Assert.assertEquals("Wrong login name", "muster", syncedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Marlene", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Muster", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "marlene@firma.test", syncedPerson.getEmail());
    }


    @Test
    public void ensureCanAppointPersonAsOfficeUser() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(singletonList(Role.USER));

        Assert.assertEquals("Wrong initial permissions", 1, person.getPermissions().size());

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

        ldapSyncService.appointPersonAsOfficeUser(person);

        verify(personService).save(personCaptor.capture());

        Collection<Role> permissions = personCaptor.getValue().getPermissions();

        Assert.assertEquals("Wrong number of permissions", 2, permissions.size());
        Assert.assertTrue("Should have user role", permissions.contains(Role.USER));
        Assert.assertTrue("Should have office role", permissions.contains(Role.OFFICE));
    }
}
