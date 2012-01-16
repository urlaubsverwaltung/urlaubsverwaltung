/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class ApplicationServiceImplTest {

    private ApplicationServiceImpl instance;
    private Application application;
    private Person person;
    private HolidaysAccount accountOne;
    private HolidaysAccount accountTwo;
    private HolidayEntitlement entitlement;
    private List<HolidaysAccount> accounts;

    private ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
    private HolidaysAccountService accountService = mock(HolidaysAccountService.class);
    private CryptoService cryptoService = new CryptoService();
    private OwnCalendarService calendarService = new OwnCalendarService();
    private MailService mailService = mock(MailService.class);
    private CalculationService calculationService = mock(CalculationService.class);

    public ApplicationServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new ApplicationServiceImpl(applicationDAO, accountService, cryptoService, calendarService,
                mailService, calculationService);

        // create person that is needed for tests
        person = new Person();
        person.setLastName("Testperson");

        // create application that is needed for tests
        application = new Application();
        application.setPerson(person);

        // create accounts for person
        accountOne = new HolidaysAccount();
        accountOne.setPerson(person);

        accountTwo = new HolidaysAccount();
        accountTwo.setPerson(person);

        // create entitlement of holiday for person
        entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);

        Mockito.when(accountService.getAccountOrCreateOne(2011, person)).thenReturn(accountOne);
        Mockito.when(accountService.getAccountOrCreateOne(2012, person)).thenReturn(accountTwo);
        Mockito.when(accountService.getHolidaysAccount(2011, person)).thenReturn(accountOne);
        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(accountTwo);
        Mockito.when(accountService.getHolidayEntitlement(2011, person)).thenReturn(entitlement);
    }


    @After
    public void tearDown() {
    }


    /** Test of getApplicationById method, of class ApplicationServiceImpl. */
    @Test
    public void testGetApplicationById() {

        instance.getApplicationById(1234);
        Mockito.verify(applicationDAO).findOne(1234);
    }


    /** Test of getApplicationsByPerson method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsForPerson() {

        instance.getApplicationsByPerson(person);
        Mockito.verify(applicationDAO).getApplicationsByPerson(person);
    }


    /** Test of getApplicationsByState method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsByState() {

        instance.getApplicationsByState(ApplicationStatus.WAITING);
        Mockito.verify(applicationDAO).getApplicationsByState(ApplicationStatus.WAITING);
    }


    /** Test of getApplicationsForACertainTime method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsForACertainTime() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = DateMidnight.now();

        instance.getApplicationsForACertainTime(start, end);
        Mockito.verify(applicationDAO).getApplicationsForACertainTime(start.toDate(), end.toDate());
    }


    /** Test of allow method, of class ApplicationServiceImpl. */
    @Ignore
    @Test
    public void testAllow() {

        application.setStatus(ApplicationStatus.WAITING);

        instance.allow(application, person);

        assertEquals(ApplicationStatus.ALLOWED, application.getStatus());
        assertEquals(person, application.getBoss());
    }


    /** Test of save method, of class ApplicationServiceImpl. */
    @Test
    public void testSave() {

        application.setStartDate(new DateMidnight(2011, 12, 17));
        application.setEndDate(new DateMidnight(2011, 12, 27));
        application.setStatus(null);
        application.setHowLong(DayLength.FULL);

        accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountOne);

        Mockito.when(calculationService.subtractVacationDays(application)).thenReturn(accounts);

        instance.save(application);

        assertEquals(ApplicationStatus.WAITING, application.getStatus());
        assertNotNull(application.getDays());
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), application.getDays());

        Mockito.verify(applicationDAO).save(application);

        Mockito.verify(accountService).saveHolidaysAccount(accountOne);
    }


    /** Test of reject method, of class ApplicationServiceImpl. */
    @Test
    public void testReject() {

        Person boss = new Person();

        application.setStatus(ApplicationStatus.WAITING);

        accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountOne);

        Mockito.when(calculationService.addVacationDays(application)).thenReturn(accounts);

        instance.reject(application, boss);

        assertEquals(ApplicationStatus.REJECTED, application.getStatus());

        assertNotNull(application.getBoss());
        assertEquals(boss, application.getBoss());
    }


    /** Test of cancel method, of class ApplicationServiceImpl. */
    @Test
    public void testCancel() {

        application.setStatus(ApplicationStatus.WAITING);

        accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountOne);

        Mockito.when(calculationService.addVacationDays(application)).thenReturn(accounts);

        instance.cancel(application);

        assertEquals(ApplicationStatus.CANCELLED, application.getStatus());
    }


    /** Test of addSickDaysOnHolidaysAccount method, of class ApplicationServiceImpl. */
    @Test
    public void testAddSickDaysOnHolidaysAccount() {

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(BigDecimal.valueOf(16.0));
        accountOne.setYear(2011);

        application.setDays(BigDecimal.valueOf(10.0));
        application.setDateOfAddingSickDays(new DateMidnight(2011, DateTimeConstants.NOVEMBER, 11));
        application.setSickDays(BigDecimal.valueOf(3.0));

        entitlement.setVacationDays(BigDecimal.valueOf(24.0));

        instance.addSickDaysOnHolidaysAccount(application);

        assertEquals(BigDecimal.valueOf(3.0), application.getSickDays());
        assertEquals((BigDecimal.valueOf(10.0).subtract(BigDecimal.valueOf(3.0))), application.getDays());

        Mockito.verify(calculationService).addSickDaysOnHolidaysAccount(application, accountOne);
    }


    /** Test of signApplicationByUser method, of class ApplicationServiceImpl. */
    @Test
    public void testSignApplicationByUser() throws Exception {

        // person needs some info: private key, last name
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(VacationType.SPECIALLEAVE);
        application.setApplicationDate(new DateMidnight(2011, 11, 1));

        // execute method
        instance.signApplicationByUser(application, person);

        // signature of person should be filled, signature of boss not
        assertNotNull(application.getSignaturePerson());
        assertEquals(null, application.getSignatureBoss());
    }


    /** Test of signApplicationByBoss method, of class ApplicationServiceImpl. */
    @Test
    public void testSignApplicationByBoss() throws Exception {

        // person needs some info: private key, last name
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setApplicationDate(new DateMidnight(2011, 12, 21));

        // execute method
        instance.signApplicationByBoss(application, person);

        // signature of boss should be filled, signature of person not
        assertNotNull(application.getSignatureBoss());
        assertEquals(null, application.getSignaturePerson());
    }


    /** Test of checkApplication method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckApplication() {

        // subtractVacationDays of CalculationService makes the real calculation
        // checkApplication only checks if account's vacation days after calculation are greater or equal than zero
        // if this case application is valid (return true)
        // if account's vacation days are less than zero the application is not valid (return false)

        accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountOne);

        Mockito.when(calculationService.subtractForCheck(application)).thenReturn(accounts);

        accountOne.setVacationDays(BigDecimal.ZERO);

        boolean returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        accountOne.setVacationDays(BigDecimal.TEN);

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        accountOne.setVacationDays(BigDecimal.valueOf(-5.0));

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);

        accountOne.setVacationDays(BigDecimal.TEN);
        accountTwo.setVacationDays(BigDecimal.valueOf(-5.0));
        accounts.add(accountTwo);

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);
    }


    /** Test of checkOverlap method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlap() {

        // case (1) no overlap at all, with gap
        // a1: 16. - 18. Jan.
        // aNew: 23. - 24. Jan.
        // excepted return value == 1

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(new ArrayList<Application>());

        int returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(1, returnValue);

        // case (1) no overlap at all, abuting
        // a1: 16. - 18. Jan.
        // aNew: 19. - 20. Jan.
        // excepted return value == 1

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        // return new and empty list
        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(new ArrayList<Application>());

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(1, returnValue);

        // case (2) period of aNew is element of the period of a1
        // a1: 16. - 20. Jan.
        // aNew: 17. - 18. Jan.
        // excepted return value == 2

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        List<Application> list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(2, returnValue);

        // case (3) period of aNew is overlapping end of period of a1
        // a1: 16. - 19. Jan.
        // aNew: 18. - 20. Jan.
        // excepted return value == 3

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(3, returnValue);

        // case (3) period of aNew is overlapping start of period of a1
        // aNew: 16. - 19. Jan.
        // a1: 18. - 20. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(3, returnValue);

        // case (3) period of aNew is overlapping two different periods (a1 and a2)
        // aNew: 17. - 26. Jan.
        // a1: 16. - 18. Jan.
        // a2: 25. - 27. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 26));

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 27));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(3, returnValue);

        // periods a1 and a2 abut case (1), aNew is element of both and has no gap case (2)
        // aNew: 17. - 23. Jan.
        // a1: 16. - 18. Jan.
        // a2: 19. - 25. Jan.
        // excepted return value == 2

        aNew = new Application();
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));

        a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(instance.getApplicationsByPersonForACertainTime(aNew)).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(2, returnValue);
    }
}
