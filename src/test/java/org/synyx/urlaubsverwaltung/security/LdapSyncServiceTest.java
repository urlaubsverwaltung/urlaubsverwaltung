package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
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

    private LdapUserService ldapUserService;

    private LdapSyncService ldapSyncService;
    private PersonService personService;
    private MailService mailService;

    @Before
    public void setUp() {

        ldapUserService = Mockito.mock(LdapUserService.class);
        personService = Mockito.mock(PersonService.class);
        mailService = Mockito.mock(MailService.class);

        ldapSyncService = new LdapSyncService(ldapUserService, personService, mailService);
    }


    @Test
    public void ensureFetchesLdapUsersForLdapAuthentication() {

        ldapSyncService.sync();

        Mockito.verify(ldapUserService).getLdapUsers();
    }


    @Test
    public void ensureFetchesLdapUsersForActiveDirectoryAuthentication() {

        ldapSyncService.sync();

        Mockito.verify(ldapUserService).getLdapUsers();
    }


    @Test
    public void ensureCreatedPersonHasTheCorrectRole() {

        Person person = ldapSyncService.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de"));

        Collection<Role> roles = person.getPermissions();

        Assert.assertEquals("Wrong number of roles", 1, roles.size());
        Assert.assertTrue("Does not contain user role", roles.contains(Role.USER));
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyLoginName() {

        Person person = ldapSyncService.createPerson("murygina", Optional.empty(), Optional.empty(), Optional.empty());

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertNotNull("Missing login name", person.getLoginName());
        Assert.assertEquals("Wrong login name", "murygina", person.getLoginName());

        Assert.assertNull("First name should be not set", person.getFirstName());
        Assert.assertNull("Last name should be not set", person.getLastName());
        Assert.assertNull("Mail address should be not set", person.getEmail());
    }


    @Test
    public void ensureCreatedPersonHasCorrectAttributes() {

        Person person = ldapSyncService.createPerson("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de"));

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertNotNull("Missing login name", person.getLoginName());
        Assert.assertNotNull("Missing first name", person.getFirstName());
        Assert.assertNotNull("Missing last name", person.getLastName());
        Assert.assertNotNull("Missing mail address", person.getEmail());

        Assert.assertEquals("Wrong login name", "murygina", person.getLoginName());
        Assert.assertEquals("Wrong first name", "Aljona", person.getFirstName());
        Assert.assertEquals("Wrong last name", "Murygina", person.getLastName());
        Assert.assertEquals("Wrong mail address", "murygina@synyx.de", person.getEmail());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoLoginNameIsGiven() {

        ldapSyncService.createPerson(null, Optional.of("Aljona"), Optional.of("Murygina"),
            Optional.of("murygina@synyx.de"));
    }


    @Test
    public void ensureSyncedPersonHasCorrectAttributes() {

        Person person = new Person();
        person.setFirstName("Marlene");
        person.setLastName("Muster");
        person.setEmail("marlene@muster.de");
        person.setLoginName("muster");

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

        Person person = new Person();
        person.setFirstName("Marlene");
        person.setLastName("Muster");
        person.setEmail("marlene@muster.de");
        person.setLoginName("muster");

        Person syncedPerson = ldapSyncService.syncPerson(person, Optional.empty(), Optional.empty(), Optional.empty());

        Mockito.verify(personService).save(Mockito.eq(person));

        Assert.assertEquals("Wrong login name", "muster", syncedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Marlene", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Muster", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "marlene@muster.de", syncedPerson.getEmail());
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
