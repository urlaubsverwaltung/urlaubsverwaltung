package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.mockito.invocation.InvocationOnMock;

import org.mockito.stubbing.Answer;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Role;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class PersonServiceImplTest {

    private PersonService instance;
    private PersonDAO personDAO = mock(PersonDAO.class);
    private ApplicationService applicationService = mock(ApplicationService.class);
    private HolidaysAccountService accountService = mock(HolidaysAccountService.class);
    private MailService mailService = mock(MailService.class);

    public PersonServiceImplTest() {
    }

    @Before
    public void setUp() {
        instance = new PersonServiceImpl(personDAO, applicationService, mailService, accountService);

    }

    /** Test of save method, of class PersonServiceImpl. */
    @Test
    public void testSave() {

        Person personToSave = new Person();
        instance.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
    }


    /** Test of deactivate method, of class PersonServiceImpl. */
    @Test
    public void testDeactivate() {

        Person person = new Person();
        person.setActive(true);
        person.setRole(Role.USER);

        instance.deactivate(person);

        assertEquals(false, person.isActive());
        assertEquals(Role.INACTIVE, person.getRole());
    }


    /** Test of activate method, of class PersonServiceImpl. */
    @Test
    public void testActivate() {

        Person person = new Person();
        person.setActive(false);
        person.setRole(Role.INACTIVE);

        instance.activate(person);

        assertEquals(true, person.isActive());
        assertEquals(Role.USER, person.getRole());
    }


    /** Test of getPersonByID method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByID() {

        instance.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    /** Test of getPersonByLogin method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByLogin() {

        Person person = new Person();

        Mockito.when(personDAO.getPersonByLogin("abcdastutnichtweh")).thenReturn(person);

        Person returnedPerson = instance.getPersonByLogin("abcdastutnichtweh");
        assertNotNull(returnedPerson);
        assertEquals(person, returnedPerson);
    }


    /** Test of getAllPersons method, of class PersonServiceImpl. */
    @Test
    public void testGetAllPersons() {

        instance.getAllPersons();
        Mockito.verify(personDAO).getPersonsOrderedByLastName();
    }


    /** Test of getPersonsWithRemainingVacationDays method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonsWithRemainingVacationDays() {

        List<Person> persons = new ArrayList<Person>();
        Person person = new Person();
        persons.add(person);
        person.setFirstName("babbel");

        HolidaysAccount account = mock(HolidaysAccount.class);

        Mockito.when(personDAO.getPersonsOrderedByLastName()).thenReturn(persons);
        Mockito.when(accountService.getHolidaysAccount(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(account);
        Mockito.when(account.getRemainingVacationDays()).thenReturn(BigDecimal.valueOf(5.0));

        List<Person> result = instance.getPersonsWithRemainingVacationDays();

        assertEquals(result.get(0).getFirstName(), "babbel");
    }


    /** Test of getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail method, of class PersonServiceImpl. */
    @Test
    public void testGetAllPersonsOnHolidayForThisWeekAndPutItInAnEmail() {

        DateMidnight startDate = new DateMidnight(2011, 12, 12);
        DateMidnight endDate = new DateMidnight(2011, 12, 21);
        List<Application> applications = new ArrayList<Application>();
        Application application = new Application();
        Person person = new Person();
        person.setFirstName("Hans-Peter");
        application.setPerson(person);
        applications.add(application);

        Mockito.when(applicationService.getApplicationsForACertainTime(startDate, endDate)).thenReturn(applications);

        instance.getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(startDate, endDate);

        Mockito.verify(mailService).sendWeeklyVacationForecast(Mockito.anyList());

        Mockito.doAnswer(new Answer() {

                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    List<Person> catchedList = (List<Person>) (args[0]);
                    assertEquals("Hans-Peter", catchedList.get(0).getFirstName());

                    return null;
                }
            }).when(mailService).sendWeeklyVacationForecast(Mockito.anyList());
    }
}
