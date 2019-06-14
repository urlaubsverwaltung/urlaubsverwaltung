package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.ldap.LdapSyncService;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class LdapSyncServiceTest {

    private LdapSyncService sut;

    @Mock
    private PersonService personService;

    @Before
    public void setUp() {
        sut = new LdapSyncService(personService);
    }


    @Test
    public void ensurePersonIsCreatedWithCorrectAttributes() {

        final String loginName = "murygina";
        final String firstName = "Aljona";
        final String lastName = "Murygina";
        final String email = "murygina@synyx.de";

        final Person person = new Person();
        person.setLoginName(loginName);
        when(personService.create(loginName, lastName, firstName, email, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);

        final Person createdPerson = sut.createPerson(loginName, of(firstName), of(lastName), of(email));
        assertThat(createdPerson.getLoginName()).isEqualTo(loginName);
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyLoginName() {

        final String loginName = "murygina";

        final Person person = new Person();
        person.setLoginName(loginName);
        when(personService.create(loginName, null, null, null, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);

        final Person createdPerson = sut.createPerson(loginName, Optional.empty(), Optional.empty(), Optional.empty());
        assertThat(createdPerson.getLoginName()).isEqualTo(loginName);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoLoginNameIsGiven() {

        sut.createPerson(null, of("Aljona"), of("Murygina"), of("murygina@synyx.de"));
    }


    @Test
    public void ensureSyncedPersonHasCorrectAttributes() {

        final Person person = createPerson("muster", "Marlene", "Muster", "marlene@firma.test");
        when(personService.save(person)).thenReturn(person);

        final Person syncedPerson = sut.syncPerson(person, of("Aljona"), of("Murygina"), of("murygina@synyx.de"));

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

        Person person = createPerson("muster", "Marlene", "Muster", "marlene@firma.test");
        when(personService.save(person)).thenReturn(person);

        Person syncedPerson = sut.syncPerson(person, Optional.empty(), Optional.empty(), Optional.empty());

        Assert.assertEquals("Wrong login name", "muster", syncedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Marlene", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Muster", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "marlene@firma.test", syncedPerson.getEmail());
    }


    @Test
    public void ensureCanAppointPersonAsOfficeUser() {

        Person person = createPerson();
        person.setPermissions(singletonList(USER));

        Assert.assertEquals("Wrong initial permissions", 1, person.getPermissions().size());

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);

        sut.appointPersonAsOfficeUser(person);

        verify(personService).save(personCaptor.capture());

        Collection<Role> permissions = personCaptor.getValue().getPermissions();

        Assert.assertEquals("Wrong number of permissions", 2, permissions.size());
        Assert.assertTrue("Should have user role", permissions.contains(USER));
        Assert.assertTrue("Should have office role", permissions.contains(Role.OFFICE));
    }
}
