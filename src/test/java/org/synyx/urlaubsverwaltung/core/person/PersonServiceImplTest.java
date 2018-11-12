package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author Aljona Murygina
 * @author Johannes Reuter
 */
public class PersonServiceImplTest {

    private PersonService sut;

    private PersonDAO personDAO;

    @Before
    public void setUp() {

        personDAO = Mockito.mock(PersonDAO.class);

        sut = new PersonServiceImpl(personDAO);
    }


    @Test
    public void ensureCreatedPersonHasCorrectAttributes() {

        Person person = new Person("rick", "Grimes", "Rick", "rick@grimes.de");
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Person createdPerson = sut.create(person);

        Assert.assertEquals("Wrong login name", "rick", createdPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Rick", createdPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Grimes", createdPerson.getLastName());
        Assert.assertEquals("Wrong email", "rick@grimes.de", createdPerson.getEmail());

        Assert.assertEquals("Wrong number of notifications", 2, createdPerson.getNotifications().size());
        Assert.assertTrue("Missing notification",
                createdPerson.getNotifications().contains(MailNotification.NOTIFICATION_USER));
        Assert.assertTrue("Missing notification",
                createdPerson.getNotifications().contains(MailNotification.NOTIFICATION_BOSS));

        Assert.assertEquals("Wrong number of permissions", 2, createdPerson.getPermissions().size());
        Assert.assertTrue("Missing permission", createdPerson.getPermissions().contains(Role.USER));
        Assert.assertTrue("Missing permission", createdPerson.getPermissions().contains(Role.BOSS));
    }


    @Test
    public void ensureCreatedPersonIsPersisted() {

        Person person = TestDataCreator.createPerson();

        Person createdPerson = sut.create(person);

        Mockito.verify(personDAO).save(createdPerson);
    }

    @Test
    public void ensureUpdatedPersonHasCorrectAttributes() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(personDAO.findOne(Mockito.anyInt())).thenReturn(person);

        Person updatedPerson = sut.update(42, "rick", "Grimes", "Rick", "rick@grimes.de",
                Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS),
                Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertEquals("Wrong login name", "rick", updatedPerson.getLoginName());
        Assert.assertEquals("Wrong first name", "Rick", updatedPerson.getFirstName());
        Assert.assertEquals("Wrong last name", "Grimes", updatedPerson.getLastName());
        Assert.assertEquals("Wrong email", "rick@grimes.de", updatedPerson.getEmail());

        Assert.assertEquals("Wrong number of notifications", 2, updatedPerson.getNotifications().size());
        Assert.assertTrue("Missing notification",
                updatedPerson.getNotifications().contains(MailNotification.NOTIFICATION_USER));
        Assert.assertTrue("Missing notification",
                updatedPerson.getNotifications().contains(MailNotification.NOTIFICATION_BOSS));

        Assert.assertEquals("Wrong number of permissions", 2, updatedPerson.getPermissions().size());
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(Role.USER));
        Assert.assertTrue("Missing permission", updatedPerson.getPermissions().contains(Role.BOSS));

  }


    @Test
    public void ensureUpdatedPersonIsPersisted() {

        Person person = TestDataCreator.createPerson();
        person.setId(1);

        sut.update(person);

        Mockito.verify(personDAO).save(person);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfPersonToBeUpdatedHasNoID() {

        Person person = TestDataCreator.createPerson();
        person.setId(null);

        sut.update(person);
    }


    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        Person personToSave = TestDataCreator.createPerson();
        sut.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
    }


    @Test
    public void ensureGetPersonByIDCallsCorrectDaoMethod() {

        sut.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    @Test
    public void ensureGetPersonByLoginCallsCorrectDaoMethod() {

        String login = "foo";

        sut.getPersonByLogin(login);

        Mockito.verify(personDAO).findByLoginName(login);
    }


    @Test
    public void ensureGetActivePersonsReturnsOnlyPersonsThatHaveNotInactiveRole() {

        Person inactive = TestDataCreator.createPerson("inactive");
        inactive.setPermissions(Collections.singletonList(Role.INACTIVE));

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> activePersons = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, activePersons.size());

        Assert.assertTrue("Missing person", activePersons.contains(user));
        Assert.assertTrue("Missing person", activePersons.contains(boss));
        Assert.assertTrue("Missing person", activePersons.contains(office));
    }


    @Test
    public void ensureGetInactivePersonsReturnsOnlyPersonsThatHaveInactiveRole() {

        Person inactive = TestDataCreator.createPerson("inactive");
        inactive.setPermissions(Collections.singletonList(Role.INACTIVE));

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(inactive, user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> inactivePersons = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 1, inactivePersons.size());

        Assert.assertTrue("Missing person", inactivePersons.contains(inactive));
    }


    @Test
    public void ensureGetPersonsByRoleReturnsOnlyPersonsWithTheGivenRole() {

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsByRole(Role.BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnsOnlyPersonsWithTheGivenNotificationType() {

        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));
        user.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER));

        Person boss = TestDataCreator.createPerson("boss");
        boss.setPermissions(Arrays.asList(Role.USER, Role.BOSS));
        boss.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Person office = TestDataCreator.createPerson("office");
        office.setPermissions(Arrays.asList(Role.USER, Role.BOSS, Role.OFFICE));
        office.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS,
                MailNotification.NOTIFICATION_OFFICE));

        List<Person> allPersons = Arrays.asList(user, boss, office);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = sut.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(boss));
        Assert.assertTrue("Missing person", filteredList.contains(office));
    }


    @Test
    public void ensureGetActivePersonsReturnSortedList() {

        Person shane = TestDataCreator.createPerson("shane");
        Person carl = TestDataCreator.createPerson("carl");
        Person rick = TestDataCreator.createPerson("rick");

        List<Person> unsortedPersons = Arrays.asList(shane, carl, rick);

        Mockito.when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getActivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetInactivePersonsReturnSortedList() {

        Person shane = TestDataCreator.createPerson("shane");
        Person carl = TestDataCreator.createPerson("carl");
        Person rick = TestDataCreator.createPerson("rick");

        List<Person> unsortedPersons = Arrays.asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(Collections.singletonList(Role.INACTIVE)));

        Mockito.when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getInactivePersons();

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetPersonsByRoleReturnSortedList() {

        Person shane = TestDataCreator.createPerson("shane");
        Person carl = TestDataCreator.createPerson("carl");
        Person rick = TestDataCreator.createPerson("rick");

        List<Person> unsortedPersons = Arrays.asList(shane, carl, rick);
        unsortedPersons.forEach(person -> person.setPermissions(Collections.singletonList(Role.USER)));

        Mockito.when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getPersonsByRole(Role.USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }


    @Test
    public void ensureGetPersonsByNotificationTypeReturnSortedList() {

        Person shane = TestDataCreator.createPerson("shane");
        Person carl = TestDataCreator.createPerson("carl");
        Person rick = TestDataCreator.createPerson("rick");

        List<Person> unsortedPersons = Arrays.asList(shane, carl, rick);
        unsortedPersons.forEach(person ->
                person.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER)));

        Mockito.when(personDAO.findAll()).thenReturn(unsortedPersons);

        List<Person> sortedList = sut.getPersonsWithNotificationType(MailNotification.NOTIFICATION_USER);

        Assert.assertEquals("Wrong number of persons", 3, sortedList.size());
        Assert.assertEquals("Wrong first person", carl, sortedList.get(0));
        Assert.assertEquals("Wrong second person", rick, sortedList.get(1));
        Assert.assertEquals("Wrong third person", shane, sortedList.get(2));
    }
}
