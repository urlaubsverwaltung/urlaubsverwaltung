package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class PersonSyncServiceTest {

    private PersonSyncService sut;

    @Mock
    private PersonService personService;

    @Before
    public void setUp() {
        sut = new PersonSyncService(personService);
    }


    @Test
    public void ensurePersonIsCreatedWithCorrectAttributes() {

        final String username = "murygina";
        final String firstName = "Aljona";
        final String lastName = "Murygina";
        final String email = "murygina@synyx.de";

        final Person person = new Person();
        person.setUsername(username);
        when(personService.create(username, lastName, firstName, email, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);

        final Person createdPerson = sut.createPerson(username, of(firstName), of(lastName), of(email));
        assertThat(createdPerson.getUsername()).isEqualTo(username);
    }


    @Test
    public void ensurePersonCanBeCreatedWithOnlyUsername() {

        final String username = "murygina";

        final Person person = new Person();
        person.setUsername(username);
        when(personService.create(username, null, null, null, singletonList(NOTIFICATION_USER), singletonList(USER))).thenReturn(person);

        final Person createdPerson = sut.createPerson(username, Optional.empty(), Optional.empty(), Optional.empty());
        assertThat(createdPerson.getUsername()).isEqualTo(username);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfNoUsernameIsGiven() {

        sut.createPerson(null, of("Aljona"), of("Murygina"), of("murygina@synyx.de"));
    }


    @Test
    public void ensureSyncedPersonHasCorrectAttributes() {

        final Person person = createPerson("muster", "Marlene", "Muster", "marlene@firma.test");
        when(personService.save(person)).thenReturn(person);

        final Person syncedPerson = sut.syncPerson(person, of("Aljona"), of("Murygina"), of("murygina@synyx.de"));

        Assert.assertNotNull("Missing username", syncedPerson.getUsername());
        Assert.assertNotNull("Missing first name", syncedPerson.getFirstName());
        Assert.assertNotNull("Missing last name", syncedPerson.getLastName());
        Assert.assertNotNull("Missing mail address", syncedPerson.getEmail());

        Assert.assertEquals("Wrong username", "muster", syncedPerson.getUsername());
        Assert.assertEquals("Wrong first name", "Aljona", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Murygina", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "murygina@synyx.de", syncedPerson.getEmail());
    }


    @Test
    public void ensureSyncDoesNotEmptyAttributes() {

        Person person = createPerson("muster", "Marlene", "Muster", "marlene@firma.test");
        when(personService.save(person)).thenReturn(person);

        Person syncedPerson = sut.syncPerson(person, Optional.empty(), Optional.empty(), Optional.empty());

        Assert.assertEquals("Wrong username", "muster", syncedPerson.getUsername());
        Assert.assertEquals("Wrong first name", "Marlene", syncedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Muster", syncedPerson.getLastName());
        Assert.assertEquals("Wrong mail address", "marlene@firma.test", syncedPerson.getEmail());
    }


    @Test
    public void ensureCanAppointPersonAsOfficeUser() {

        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(emptyList());
        when(personService.save(any())).then(returnsFirstArg());

        final Person person = createPerson();
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions).hasSize(2);
        assertThat(permissions).contains(USER, OFFICE);
    }

    @Test
    public void ensureCanNotAppointPersonAsOfficeUser() {

        when(personService.getActivePersonsByRole(OFFICE)).thenReturn(singletonList(new Person()));

        final Person person = createPerson();
        person.setPermissions(singletonList(USER));
        assertThat(person.getPermissions()).containsOnly(USER);

        final Person personWithOfficeRole = sut.appointAsOfficeUserIfNoOfficeUserPresent(person);

        final Collection<Role> permissions = personWithOfficeRole.getPermissions();
        assertThat(permissions).hasSize(1);
        assertThat(permissions).containsOnly(USER);
    }
}
