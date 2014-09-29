package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.security.Role;

import java.util.Arrays;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class PersonServiceTest {

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
    public void ensureGetAllPersonsCallsCorrectDaoMethod() {

        service.getAllPersons();
        Mockito.verify(personDAO).findAll();
    }


    @Test
    public void ensureGetInactivePersonsCallsCorrectDaoMethod() {

        service.getInactivePersons();
        Mockito.verify(personDAO).findInactive();
    }


    @Test
    public void ensureGetAllPersonsExceptAGivenOneReturnsAllPersonsExceptTheGivenOne() {

        Person hansPeter = new Person("hpeter", "Peter", "Hans", "hpeter@foo.de");
        Person horstDieter = new Person("hdieter", "Horst", "Dieter", "hdieter@foo.de");
        Person berndMeier = new Person("bmeier", "Meier", "Bernd", "bmeier@foo.de");

        List<Person> allPersons = Arrays.asList(hansPeter, horstDieter, berndMeier);

        Mockito.when(personDAO.findAll()).thenReturn(allPersons);

        List<Person> filteredList = service.getAllPersonsExcept(horstDieter);

        Assert.assertEquals("Wrong number of persons", 2, filteredList.size());

        Assert.assertTrue("Missing person", filteredList.contains(hansPeter));
        Assert.assertTrue("Missing person", filteredList.contains(berndMeier));
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
}
