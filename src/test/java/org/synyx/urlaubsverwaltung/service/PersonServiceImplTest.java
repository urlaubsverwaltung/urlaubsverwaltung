/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.mockito.invocation.InvocationOnMock;

import org.mockito.stubbing.Answer;

import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
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
    private HolidayEntitlementDAO holidayEntitlementDAO = mock(HolidayEntitlementDAO.class);
    private ApplicationService applicationService = mock(ApplicationService.class);
    private HolidaysAccountService accountService = mock(HolidaysAccountService.class);
    private MailService mailService = mock(MailService.class);

    public PersonServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new PersonServiceImpl(personDAO, applicationService, holidayEntitlementDAO, mailService,
                accountService);
    }


    @After
    public void tearDown() {
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


    /**
     * Test of updateVacationDays method, of class PersonServiceImpl.
     *
     * @author  Johannes Reuter
     */
    @Test
    public void testUpdateVacationDays() {

        // case 1

        HolidayEntitlement defaultEntitlement = new HolidayEntitlement();
        defaultEntitlement.setVacationDays(BigDecimal.valueOf(20.0));

        HolidaysAccount accountLastYear = mock(HolidaysAccount.class);
        HolidaysAccount accountCurrentYear = mock(HolidaysAccount.class);
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person());

        Mockito.when(personDAO.getPersonsOrderedByLastName()).thenReturn(personList);
        Mockito.when(accountLastYear.getVacationDays()).thenReturn(BigDecimal.TEN);
        Mockito.when(accountCurrentYear.getVacationDays()).thenReturn(BigDecimal.valueOf(15.0));
        Mockito.when(accountService.getHolidayEntitlement(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            defaultEntitlement);
        Mockito.when(accountService.getHolidaysAccount(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(
            accountLastYear);
        Mockito.when(accountService.getHolidaysAccount(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(
            accountCurrentYear);

        instance.updateVacationDays(2001);

        Mockito.verify(accountCurrentYear).setRemainingVacationDays(BigDecimal.valueOf(5.0));
        Mockito.verify(accountCurrentYear).setVacationDays(BigDecimal.valueOf(20.0));

        Mockito.verify(accountService).saveHolidaysAccount(accountCurrentYear);

        // case 2

        defaultEntitlement = new HolidayEntitlement();
        defaultEntitlement.setVacationDays(BigDecimal.valueOf(20.0));

        accountLastYear = mock(HolidaysAccount.class);
        accountCurrentYear = mock(HolidaysAccount.class);

        personList = new ArrayList<Person>();
        personList.add(new Person());

        Mockito.when(personDAO.getPersonsOrderedByLastName()).thenReturn(personList);
        Mockito.when(accountLastYear.getVacationDays()).thenReturn(BigDecimal.TEN);
        Mockito.when(accountCurrentYear.getVacationDays()).thenReturn(BigDecimal.valueOf(5.0));
        Mockito.when(accountService.getHolidayEntitlement(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            defaultEntitlement);
        Mockito.when(accountService.getHolidaysAccount(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(
            accountLastYear);
        Mockito.when(accountService.getHolidaysAccount(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(
            accountCurrentYear);

        instance.updateVacationDays(2001);

        Mockito.verify(accountCurrentYear).setVacationDays(BigDecimal.valueOf(15.0));

        Mockito.verify(accountService).saveHolidaysAccount(accountCurrentYear);
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


    /** Test of getHolidayEntitlementByPersonAndYear method, of class PersonServiceImpl. */
    @Test
    public void testGetHolidayEntitlementByPersonAndYear() {

        Person person = new Person();
        HolidayEntitlement entitlement = new HolidayEntitlement();

        Mockito.when(holidayEntitlementDAO.getHolidayEntitlementByYearAndPerson(2000, person)).thenReturn(entitlement);

        HolidayEntitlement gotByMethod = instance.getHolidayEntitlementByPersonAndYear(person, 2000);

        assertEquals(gotByMethod, entitlement);
    }


    /** Test of getHolidayEntitlementByPersonForAllYears method, of class PersonServiceImpl. */
    @Test
    public void testGetHolidayEntitlementByPersonForAllYears() {

        Person person = new Person();
        List<HolidayEntitlement> entitlements = new ArrayList<HolidayEntitlement>();
        HolidayEntitlement entitlement = new HolidayEntitlement();

        entitlements.add(entitlement);

        Mockito.when(holidayEntitlementDAO.getHolidayEntitlementByPerson(person)).thenReturn(entitlements);

        List<HolidayEntitlement> gotByMethod = instance.getHolidayEntitlementByPersonForAllYears(person);

        assertEquals(gotByMethod.get(0), entitlement);
    }
}
