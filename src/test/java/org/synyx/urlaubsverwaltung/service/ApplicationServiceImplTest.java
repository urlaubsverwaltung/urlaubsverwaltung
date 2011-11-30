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


    /** Test of getAllApplicationsForPerson method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsForPerson() {

        instance.getAllApplicationsForPerson(person);
        Mockito.verify(applicationDAO).getAllApplicationsForPerson(person);
    }


    /** Test of getAllApplicationsByState method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsByState() {

        instance.getAllApplicationsByState(ApplicationStatus.WAITING);
        Mockito.verify(applicationDAO).getAllApplicationsByState(ApplicationStatus.WAITING);
    }


    /** Test of getAllApplicationsForACertainTime method, of class ApplicationServiceImpl. */
    @Test
    public void testGetAllApplicationsForACertainTime() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = DateMidnight.now();

        instance.getAllApplicationsForACertainTime(start, end);
        Mockito.verify(applicationDAO).getAllApplicationsForACertainTime(start, end);
    }


    /** Test of wait method, of class ApplicationServiceImpl. */
    @Test
    public void testWait() {

        application.setStatus(ApplicationStatus.ALLOWED);
        instance.wait(application);

        assertEquals(ApplicationStatus.WAITING, application.getStatus());
    }


    /** Test of allow method, of class ApplicationServiceImpl. */
    @Test
    public void testAllow() {

        application.setStartDate(new DateMidnight(2011, 12, 17));
        application.setEndDate(new DateMidnight(2011, 12, 27));
        application.setStatus(ApplicationStatus.WAITING);
        application.setHowLong(DayLength.FULL);

        instance.allow(application);

        assertEquals(ApplicationStatus.ALLOWED, application.getStatus());
        assertNotNull(application.getDays());
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), application.getDays());
    }


    /** Test of save method, of class ApplicationServiceImpl. */
    @Test
    public void testSave() {

        application.setStartDate(new DateMidnight(2000, 3, 10));
        application.setEndDate(new DateMidnight(2000, 3, 20));

        entitlement.setVacationDays(BigDecimal.valueOf(10.0));

        Mockito.when(accountService.getHolidayEntitlement(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            entitlement);

        instance.save(application);
        Mockito.verify(applicationDAO).save(application);
        Mockito.verify(calculationService).noticeApril((Application) (Mockito.any()),
            (HolidaysAccount) (Mockito.any()));

        application = new Application();
        application.setStartDate(new DateMidnight(2000, 12, 20));
        application.setEndDate(new DateMidnight(2001, 1, 10));

        entitlement = new HolidayEntitlement();
        entitlement.setVacationDays(BigDecimal.valueOf(12.0));

        Mockito.when(accountService.getHolidayEntitlement(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            entitlement);

        instance.save(application);
        Mockito.verify(applicationDAO).save(application);
        Mockito.verify(calculationService).noticeJanuary((Application) (Mockito.any()),
            (HolidaysAccount) (Mockito.any()), (HolidaysAccount) (Mockito.any()));
    }


    /** Test of reject method, of class ApplicationServiceImpl. */
    @Test
    public void testReject() {

        Person boss = new Person();
        boss.setLastName("Testboss");

        String reason = "Einfach so halt, weil ich Bock drauf hab.";

        application.setStatus(ApplicationStatus.WAITING);

        instance.reject(application, boss, reason);

        assertEquals(ApplicationStatus.REJECTED, application.getStatus());

        assertNotNull(application.getBoss());
        assertEquals(boss, application.getBoss());

        assertNotNull(application.getReasonToReject());
        assertEquals(reason, application.getReasonToReject().getText());
    }


    /** Test of cancel method, of class ApplicationServiceImpl. */
    @Test
    public void testCancel() {

        application.setStatus(ApplicationStatus.WAITING);

        instance.cancel(application);

        assertEquals(ApplicationStatus.CANCELLED, application.getStatus());
    }


    /** Test of addSickDaysOnHolidaysAccount method, of class ApplicationServiceImpl. */
    @Test
    public void testAddSickDaysOnHolidaysAccount() {

        // no special case:
        // newVacDays < entitlement

        BigDecimal sickDays = BigDecimal.valueOf(3.0);
        BigDecimal daysStart = BigDecimal.valueOf(16.0);
        BigDecimal nettoTage = BigDecimal.valueOf(10.0);
        BigDecimal vacentitlement = BigDecimal.valueOf(24.0);

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(daysStart);
        accountOne.setYear(2011);

        application.setDays(nettoTage);
        application.setStartDate(new DateMidnight(2011, 11, 1));
        application.setEndDate(new DateMidnight(2011, 11, 16));

        entitlement.setVacationDays(vacentitlement);

        instance.addSickDaysOnHolidaysAccount(application, sickDays.doubleValue());

        assertEquals(sickDays, application.getSickDays());
        assertEquals((nettoTage.subtract(sickDays)), application.getDays());

        assertEquals((daysStart.add(sickDays)), accountOne.getVacationDays());

        // special case:
        // newVacDays > entitlement
        // AND
        // it is before April

        sickDays = BigDecimal.valueOf(10.0);
        nettoTage = BigDecimal.valueOf(15.0);
        daysStart = BigDecimal.valueOf(20.0);
        vacentitlement = BigDecimal.valueOf(23.0);

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(daysStart);
        accountOne.setYear(2011);

        application.setDays(nettoTage);
        application.setStartDate(new DateMidnight(2011, 2, 1));
        application.setEndDate(new DateMidnight(2011, 2, 20));

        entitlement.setVacationDays(vacentitlement);

        instance.addSickDaysOnHolidaysAccount(application, sickDays.doubleValue());

        assertEquals(sickDays, application.getSickDays());
        assertEquals((nettoTage.subtract(sickDays)), application.getDays());

        assertEquals((sickDays.add(daysStart)).subtract(vacentitlement), accountOne.getRemainingVacationDays());
        assertEquals((vacentitlement), accountOne.getVacationDays());

        // special case:
        // newVacDays > entitlement
        // AND
        // it is after April

        sickDays = BigDecimal.valueOf(10.0);
        nettoTage = BigDecimal.valueOf(12.0);
        daysStart = BigDecimal.valueOf(20.0);
        vacentitlement = BigDecimal.valueOf(23.0);

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(daysStart);
        accountOne.setYear(2011);

        application.setDays(nettoTage);
        application.setStartDate(new DateMidnight(2011, 4, 5));
        application.setEndDate(new DateMidnight(2011, 4, 23));

        entitlement.setVacationDays(vacentitlement);

        instance.addSickDaysOnHolidaysAccount(application, sickDays.doubleValue());

        assertEquals(sickDays, application.getSickDays());
        assertEquals((nettoTage.subtract(sickDays)), application.getDays());

        assertEquals(BigDecimal.ZERO, accountOne.getRemainingVacationDays());
        assertEquals((vacentitlement), accountOne.getVacationDays());
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

        application.setHowLong(DayLength.FULL);

        // Possible cases:
        // 1. between December and January
        // 2. between March and April
        // 3. before 1st April
        // 4. after 1st April
        // 5. enough days mean: check == true
        // 6. not enough days mean: check == false

        // TEST 1 - no special case, enough days
        double remainingDays = 5.0;
        double vacationDays = 26.0;
        int year = 2011;
        accountOne.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountOne.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountOne.setYear(year);

        application.setStartDate(new DateMidnight(2011, 1, 12));
        application.setEndDate(new DateMidnight(2011, 1, 30));

        boolean returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 2 - no special case, not enough days
        remainingDays = 0.0;
        vacationDays = 5.0;
        year = 2011;
        accountOne.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountOne.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountOne.setYear(year);

        application.setStartDate(new DateMidnight(2011, 12, 12));
        application.setEndDate(new DateMidnight(2011, 12, 23));

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);

        // TEST 3 - special case April, enough days
        remainingDays = 10.0;
        vacationDays = 26.0;
        year = 2011;
        accountOne.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountOne.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountOne.setYear(year);

        application.setStartDate(new DateMidnight(2011, 3, 28));
        application.setEndDate(new DateMidnight(2011, 4, 23));

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 4 - special case January, enough days
        remainingDays = 0.0;
        vacationDays = 10.0;
        year = 2011;
        accountOne.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountOne.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountOne.setYear(year);

        remainingDays = 2.0;
        vacationDays = 26.0;
        year = 2012;
        accountTwo.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountTwo.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountTwo.setYear(year);

        application.setStartDate(new DateMidnight(2011, 12, 19));
        application.setEndDate(new DateMidnight(2012, 1, 5));

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 5 - special case January, not enough days
        remainingDays = 0.0;
        vacationDays = 5.0;
        year = 2011;
        accountOne.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountOne.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountOne.setYear(year);

        remainingDays = 0.0;
        vacationDays = 26.0;
        year = 2012;
        accountTwo.setRemainingVacationDays(BigDecimal.valueOf(remainingDays));
        accountTwo.setVacationDays(BigDecimal.valueOf(vacationDays));
        accountTwo.setYear(year);

        application.setStartDate(new DateMidnight(2011, 12, 19));
        application.setEndDate(new DateMidnight(2012, 1, 5));

        returnValue = instance.checkApplication(application);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);
    }
}
