package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;

import org.synyx.urlaubsverwaltung.security.Role;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.when;

import static org.synyx.urlaubsverwaltung.core.mail.MailNotification.NOTIFICATION_BOSS;
import static org.synyx.urlaubsverwaltung.core.mail.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.security.Role.BOSS;

import static java.util.Arrays.asList;


@RunWith(MockitoJUnitRunner.class)
public class PersonServiceImplTest {

    private PersonServiceImpl sut;

    @Mock
    private PersonDAO personDAOMock;
    private ArrayList<Person> persons;
    private Person activeBoss;

    @Before
    public void setUp() {

        sut = new PersonServiceImpl(personDAOMock);

        activeBoss = new Person();
        activeBoss.setFirstName("pete");
        activeBoss.setPermissions(asList(BOSS));
        activeBoss.setNotifications(asList(NOTIFICATION_BOSS));

        Person activeUser = new Person();
        activeUser.setPermissions(asList(Role.USER));
        activeUser.setFirstName("bete");
        activeUser.setNotifications(asList(NOTIFICATION_OFFICE));

        Person notActivePerson = new Person();
        notActivePerson.setFirstName("maria");
        notActivePerson.setPermissions(asList(Role.INACTIVE));

        persons = new ArrayList<>();
        persons.add(activeBoss);
        persons.add(activeUser);
        persons.add(notActivePerson);
    }


    @Test
    public void getActivePersons() {

        when(personDAOMock.findAll()).thenReturn(persons);

        List<Person> activePersons = sut.getActivePersons();

        assertThat(activePersons.size(), is(2));
        assertThat(activePersons.get(0).getFirstName(), is("pete"));
        assertThat(activePersons.get(1).getFirstName(), is("bete"));
    }


    @Test
    public void getInactivePersons() {

        when(personDAOMock.findAll()).thenReturn(persons);

        List<Person> activePersons = sut.getInactivePersons();

        assertThat(activePersons.size(), is(1));
        assertThat(activePersons.get(0).getFirstName(), is("maria"));
    }


    @Test
    public void getPersonsByRole() {

        when(personDAOMock.findAll()).thenReturn(persons);

        List<Person> bosses = sut.getPersonsByRole(BOSS);

        assertThat(bosses.size(), is(1));
        assertThat(bosses.get(0).getFirstName(), is("pete"));
    }


    @Test
    public void getPersonsWithNotificationType() {

        when(personDAOMock.findAll()).thenReturn(persons);

        List<Person> bosses = sut.getPersonsWithNotificationType(NOTIFICATION_OFFICE);

        assertThat(bosses.size(), is(1));
        assertThat(bosses.get(0).getFirstName(), is("bete"));
    }
}
