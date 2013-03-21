package org.synyx.urlaubsverwaltung.person;

import org.synyx.urlaubsverwaltung.account.HolidaysAccountService;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonServiceImpl;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.security.CryptoService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


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
    private CryptoService cryptoService = mock(CryptoService.class);

    public PersonServiceImplTest() {
    }

    @Before
    public void setUp() {

        instance = new PersonServiceImpl(personDAO, applicationService, mailService, accountService, cryptoService);
    }


    /**
     * Test of save method, of class PersonServiceImpl.
     */
    @Test
    public void testSave() {

        Person personToSave = new Person();
        instance.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
    }


    /**
     * Test of deactivate method, of class PersonServiceImpl.
     */
    @Test
    public void testDeactivate() {

        Person person = new Person();
        person.setActive(true);

        instance.deactivate(person);

        assertEquals(false, person.isActive());
    }


    /**
     * Test of activate method, of class PersonServiceImpl.
     */
    @Test
    public void testActivate() {

        Person person = new Person();
        person.setActive(false);

        instance.activate(person);

        assertEquals(true, person.isActive());
    }


    /**
     * Test of getPersonByID method, of class PersonServiceImpl.
     */
    @Test
    public void testGetPersonByID() {

        instance.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    /**
     * Test of getPersonByLogin method, of class PersonServiceImpl.
     */
    @Test
    public void testGetPersonByLogin() {

        Person person = new Person();

        Mockito.when(personDAO.getPersonByLogin("abcdastutnichtweh")).thenReturn(person);

        Person returnedPerson = instance.getPersonByLogin("abcdastutnichtweh");
        assertNotNull(returnedPerson);
        assertEquals(person, returnedPerson);
    }


    /**
     * Test of getAllPersons method, of class PersonServiceImpl.
     */
    @Test
    public void testGetAllPersons() {

        instance.getAllPersons();
        Mockito.verify(personDAO).getPersonsOrderedByLastName();
    }


    /**
     * Test of getPersonsWithExpiringRemainingVacationDays method, of class PersonServiceImpl.
     */
    @Test
    public void testGetPersonsWithExpiringRemainingVacationDays() {

        List<Person> persons = new ArrayList<Person>();

        Person babbel = new Person();
        babbel.setFirstName("babbel");

        Person horst = new Person();
        horst.setFirstName("horscht");

        persons.add(babbel);
        persons.add(horst);

        Account babbelsAccount = mock(Account.class);
        Account horstsAccount = mock(Account.class);

        Mockito.when(personDAO.getPersonsOrderedByLastName()).thenReturn(persons);
        Mockito.when(accountService.getHolidaysAccount(DateMidnight.now().getYear(), babbel)).thenReturn(
            babbelsAccount);
        Mockito.when(accountService.getHolidaysAccount(DateMidnight.now().getYear(), horst)).thenReturn(horstsAccount);
        Mockito.when(babbelsAccount.isRemainingVacationDaysExpire()).thenReturn(true);
        Mockito.when(horstsAccount.isRemainingVacationDaysExpire()).thenReturn(false);

        List<Person> result = instance.getPersonsWithExpiringRemainingVacationDays();

        assertEquals(1, result.size());
        assertEquals(result.get(0).getFirstName(), "babbel");
    }


    /**
     * Test of getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail method, of class PersonServiceImpl.
     */
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

        Mockito.when(applicationService.getAllowedApplicationsForACertainPeriod(startDate, endDate)).thenReturn(
            applications);

        instance.getAllPersonsOnHolidayForThisWeekAndPutItInAnEmail(startDate, endDate);

        Mockito.verify(mailService).sendWeeklyVacationForecast(Mockito.anyMap());

        Mockito.doAnswer(new Answer() {

                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    List<Person> catchedList = (List<Person>) (args[0]);
                    assertEquals("Hans-Peter", catchedList.get(0).getFirstName());

                    return null;
                }
            }).when(mailService).sendWeeklyVacationForecast(Mockito.anyMap());
    }
}
