package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class PersonServiceImplTest {

    private PersonService service;
    private PersonDAO personDAO;

    @Before
    public void setUp() {

        personDAO = Mockito.mock(PersonDAO.class);

        service = new PersonServiceImpl(personDAO);
    }


    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        Person personToSave = new Person();
        service.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
    }


    @Test
    public void ensureGetPersonByIDCallsCorrectDaoMethod() {

        service.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    @Test
    public void ensureGetPersonByLoginCallsCorrectDaoMethod() {

        String login = "foo";

        service.getPersonByLogin(login);

        Mockito.verify(personDAO).findByLoginName(login);
    }


    @Test
    public void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        Person inactive = new Person();
        inactive.setPermissions(Arrays.asList(Role.INACTIVE));

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER));

        Person boss = new Person();
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = new Person();
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> activePersons = service.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, activePersons.size());

        Assert.assertTrue("Missing person", activePersons.contains(user));
        Assert.assertTrue("Missing person", activePersons.contains(boss));
        Assert.assertTrue("Missing person", activePersons.contains(office));
    }


    @Test
    public void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        Person inactive = new Person();
        inactive.setPermissions(Arrays.asList(Role.INACTIVE));

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER));

        Person boss = new Person();
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = new Person();
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> inactivePersons = service.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 1, inactivePersons.size());

        Assert.assertTrue("Missing person", inactivePersons.contains(inactive));
    }


    @Test
    public void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER));

        Person boss = new Person();
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = new Person();
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = service.getPersonsByRole(Role.BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER));
        user.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER));

        Person boss = new Person();
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        boss.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Person office = new Person();
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));
        office.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS,
                MailNotification.NOTIFICATION_OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = service.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }
}
