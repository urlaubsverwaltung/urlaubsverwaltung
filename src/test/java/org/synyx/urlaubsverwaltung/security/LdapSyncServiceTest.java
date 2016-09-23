package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapSyncServiceTest {

    private PersonService personService;

    private LdapSyncService ldapSyncService;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);

        ldapSyncService = new LdapSyncService(personService);
    }


    @Test
    public void ensurePersonIsCreatedWithCorrectAttributes() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personService.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyListOf(MailNotification.class), Mockito.anyListOf(Role.class)))
            .thenReturn(person);

        ldapSyncService.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"));

        Mockito.verify(personService)
            .create("murygina", "Murygina", "Aljona", "murygina@synyx.de",
                Collections.singletonList(MailNotification.NOTIFICATION_USER), Collections.singletonList(Role.USER));
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyLoginName() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personService.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyListOf(MailNotification.class), Mockito.anyListOf(Role.class)))
            .thenReturn(person);

        ldapSyncService.createPerson("murygina", Optional.empty(), Optional.empty(), Optional.empty());

        Mockito.verify(personService)
            .create("murygina", null, null, null, Collections.singletonList(MailNotification.NOTIFICATION_USER),
                Collections.singletonList(Role.USER));
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

        Mockito.verify(personService).save(Mockito.eq(person));

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

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertEquals("Wrong login name", "muster", syncedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Marlene", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Muster", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "marlene@firma.test", syncedPerson.getEmail());
    }


    @Test
    public void ensureCanAppointPersonAsOfficeUser() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        Assert.assertEquals("Wrong initial permissions", 1, person.getPermissions().size());

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

        ldapSyncService.appointPersonAsOfficeUser(person);

        Mockito.verify(personService).save(personCaptor.capture());

        Collection<Role> permissions = personCaptor.getValue().getPermissions();

        Assert.assertEquals("Wrong number of permissions", 2, permissions.size());
        Assert.assertTrue("Should have user role", permissions.contains(Role.USER));
        Assert.assertTrue("Should have office role", permissions.contains(Role.OFFICE));
    }
}
