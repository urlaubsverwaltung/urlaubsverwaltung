package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

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

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class ApplicationServiceImplTest {

    private static final int CURRENT_YEAR = 2011;
    private static final int NEXT_YEAR = 2012;

    private ApplicationServiceImpl instance;
    private Application application;
    private Person person;
    private HolidaysAccount accountOne;
    private HolidaysAccount accountTwo;
    private HolidaysAccount accNew;
    private HolidayEntitlement entitlement;
    private List<HolidaysAccount> accounts;

    private ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
    private HolidaysAccountService accountService = mock(HolidaysAccountService.class);
    private CryptoService cryptoService = new CryptoService();
    private OwnCalendarService calendarService = new OwnCalendarService();
    private CalculationService calculationService = mock(CalculationService.class);
    private MailService mailService = mock(MailService.class);

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
                calculationService, mailService);

        // create person that is needed for tests
        person = new Person();
        person.setLastName("Testperson");

        // create application that is needed for tests
        application = new Application();
        application.setPerson(person);

        // create accounts for person
        accountOne = new HolidaysAccount();
        accountOne.setPerson(person);
        accountOne.setYear(CURRENT_YEAR);

        accountTwo = new HolidaysAccount();
        accountTwo.setPerson(person);
        accountTwo.setYear(NEXT_YEAR);

        accNew = new HolidaysAccount();
        accNew.setPerson(person);
        accNew.setYear(NEXT_YEAR + 1);

        // create entitlement of holiday for person
        entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);

        Mockito.when(accountService.getAccountOrCreateOne(2011, person)).thenReturn(accountOne);
        Mockito.when(accountService.getAccountOrCreateOne(2012, person)).thenReturn(accountTwo);
        Mockito.when(accountService.getAccountOrCreateOne(2013, person)).thenReturn(accNew);
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


    /** Test of getApplicationsByStateAndYear method, of class ApplicationServiceImpl. */
    @Test
    public void testgetApplicationsByStateAndYear() {

        DateMidnight firstDayOfYear = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        instance.getApplicationsByStateAndYear(ApplicationStatus.WAITING, 2012);
        Mockito.verify(applicationDAO).getApplicationsByStateAndYear(ApplicationStatus.WAITING, firstDayOfYear.toDate(),
            lastDayOfYear.toDate());
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
    @Test
    public void testAllow() throws NoSuchAlgorithmException {

        // set private key for boss
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());
        application.setApplicationDate(DateMidnight.now());
        application.setVacationType(VacationType.HOLIDAY);

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
        application.setVacationType(VacationType.HOLIDAY);

        accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountOne);

        Mockito.when(calculationService.subtractVacationDays(application)).thenReturn(accounts);

        instance.save(application);

        assertEquals(ApplicationStatus.WAITING, application.getStatus());
        assertNotNull(application.getDays());
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), application.getDays());

        Mockito.verify(applicationDAO).save(application);

        Mockito.verify(accountService).saveHolidaysAccount(accountOne);

        application.setVacationType(VacationType.SPECIALLEAVE);
        instance.save(application);
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), accountOne.getSpecialLeave());

        application.setVacationType(VacationType.UNPAIDLEAVE);
        instance.save(application);
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), accountOne.getUnpaidLeave());

        application.setVacationType(VacationType.OVERTIME);
        instance.save(application);
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), accountOne.getOvertime());
    }


    /** Test of reject method, of class ApplicationServiceImpl. */
    @Test
    public void testReject() {

        accounts = new ArrayList<HolidaysAccount>();

        accounts.add(accountTwo);
        accounts.add(accNew);

        Person boss = new Person();

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setStatus(ApplicationStatus.WAITING);

        // supplemental applications

        List<Application> sApps = new ArrayList<Application>();

        Application sa1 = new Application();
        sa1.setStatus(ApplicationStatus.WAITING);
        sa1.setStartDate(startDate);
        sa1.setEndDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 31));

        Application sa2 = new Application();
        sa2.setStatus(ApplicationStatus.WAITING);
        sa2.setStartDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(endDate);

        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(applicationDAO.getSupplementalApplicationsForApplication(application.getId())).thenReturn(sApps);

        Mockito.when(calculationService.addDaysOfApplicationSpanningDecemberAndJanuary(application, accountTwo,
                BigDecimal.ZERO, BigDecimal.ZERO)).thenReturn(accounts);

        instance.reject(application, boss);

        assertEquals(ApplicationStatus.REJECTED, application.getStatus());
//        assertEquals(ApplicationStatus.REJECTED, sApps.get(0).getStatus());
//        assertEquals(ApplicationStatus.REJECTED, sApps.get(1).getStatus());

        assertNotNull(application.getBoss());
        assertEquals(boss, application.getBoss());
    }


    /** Test of cancel method, of class ApplicationServiceImpl. */
    @Test
    public void testCancel() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStatus(ApplicationStatus.WAITING);
        application.setStartDate(startDate);
        application.setEndDate(endDate);

        // supplemental applications

        List<Application> sApps = new ArrayList<Application>();

        Application sa1 = new Application();
        sa1.setStatus(ApplicationStatus.WAITING);
        sa1.setStartDate(startDate);
        sa1.setEndDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 31));

        Application sa2 = new Application();
        sa2.setStatus(ApplicationStatus.WAITING);
        sa2.setStartDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(endDate);

        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(applicationDAO.getSupplementalApplicationsForApplication(application.getId())).thenReturn(sApps);

        instance.cancel(application);

        assertEquals(ApplicationStatus.CANCELLED, application.getStatus());
        assertEquals(ApplicationStatus.CANCELLED, sApps.get(0).getStatus());
        assertEquals(ApplicationStatus.CANCELLED, sApps.get(1).getStatus());
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

        // OwnCalendarService wants to be happy, so avoid NullPointerExceptions by setting some stupid values in
        // application
        application.setStartDate(DateMidnight.now());
        application.setEndDate(DateMidnight.now());
        application.setHowLong(DayLength.FULL);

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


    /** Test of getApplicationsForACertainTime method, of class ApplicationServiceImpl. */
    @Test
    public void testGetApplicationsForACertainTime() {

        DateMidnight start = new DateMidnight(2012, 1, 25);
        DateMidnight end = new DateMidnight(2012, 2, 1);

        instance.getApplicationsForACertainTime(start, end);
        Mockito.verify(applicationDAO).getApplicationsForACertainTime(start.toDate(), end.toDate());
    }


    /** Test of getApplicationsByPersonAndYear method, of class ApplicationServiceImpl. */
    @Test
    public void testGetApplicationsByPersonAndYear() {

        DateMidnight firstDayOfYear = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        instance.getApplicationsByPersonAndYear(person, 2012);
        Mockito.verify(applicationDAO).getNotCancelledApplicationsByPersonAndYear(ApplicationStatus.CANCELLED, person,
            firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }


    /** Test of simpleSave method, of class ApplicationServiceImpl. */
    @Test
    public void testSimpleSave() {

        instance.simpleSave(application);
        Mockito.verify(applicationDAO).save(application);
    }


    /** Test of checkOverlap method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlap() {

        // show that right method is used dependant on day length of application

        // full day
        Application app = new Application();
        app.setHowLong(DayLength.FULL);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 30));
        app.setPerson(person);

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getApplicationsByPeriodForEveryDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person);

        // morning
        app.setHowLong(DayLength.MORNING);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 25));

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person, DayLength.MORNING);

        // noon
        app.setHowLong(DayLength.NOON);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 25));

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person, DayLength.NOON);
    }


    /** Test of checkOverlapForFullDay method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForFullDay() {

        // case (1) no overlap at all, with gap
        // a1: 16. - 18. Jan.
        // aNew: 23. - 24. Jan.
        // excepted return value == 1

        Application a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(new ArrayList<Application>());

        OverlapCase returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // case (1) no overlap at all, abuting
        // a1: 16. - 18. Jan.
        // aNew: 19. - 20. Jan.
        // excepted return value == 1

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        // return new and empty list
        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(new ArrayList<Application>());

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // case (2) period of aNew is element of the period of a1
        // a1: 16. - 20. Jan.
        // aNew: 17. - 18. Jan.
        // excepted return value == 2

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        List<Application> list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping end of period of a1
        // a1: 16. - 19. Jan.
        // aNew: 18. - 20. Jan.
        // excepted return value == 3

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping start of period of a1
        // aNew: 16. - 19. Jan.
        // a1: 18. - 20. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping two different periods (a1 and a2)
        // aNew: 17. - 26. Jan.
        // a1: 16. - 18. Jan.
        // a2: 25. - 27. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 26));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application a2 = new Application();
        a2.setHowLong(DayLength.FULL);
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 27));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // periods a1 and a2 abut case (1), aNew is element of both and has no gap case (2)
        // aNew: 17. - 23. Jan.
        // a1: 16. - 18. Jan.
        // a2: 19. - 25. Jan.
        // excepted return value == 2

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        a2 = new Application();
        a2.setHowLong(DayLength.FULL);
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // there is an existent application for a half day
        // new application for full day overlapping this day
        // because there would be gaps, expected value == 3
        a1 = new Application();
        a1.setHowLong(DayLength.MORNING);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // there is an existent application for a half day
        // new application for full day on this day
        // expected value == 2
        a1 = new Application();
        a1.setHowLong(DayLength.MORNING);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);
    }


    /** Test of checkOverlapForMorning method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForMorning() {

        OverlapCase returnValue;
        List<Application> list = new ArrayList<Application>();

        // FIRST CHECK: OVERLAP WITH FULL DAY PERIOD

        // there is a holiday from 23.01.2012 to 27.01.2012
        // try to apply for leave for a half day on 25.01.2012
        // list of existent applications for full day contains one entry (Application a)
        // expected return value == 2
        Application a = new Application();
        a.setHowLong(DayLength.FULL);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 27));
        a.setPerson(person);
        list.add(a);

        Application aNew = new Application();
        aNew.setHowLong(DayLength.MORNING);
        aNew.setStartDate(new DateMidnight(2012, 1, 25));
        aNew.setEndDate(new DateMidnight(2012, 1, 25));
        aNew.setPerson(person);

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at start of existent application
        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at end of existent application

        aNew.setStartDate(new DateMidnight(2012, 1, 27));
        aNew.setEndDate(new DateMidnight(2012, 1, 27));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application has no overlap with full day period
        // i.e. list of existent applications for full day is empty (and for half days too)
        // expected value == 1
        aNew.setStartDate(new DateMidnight(2012, 1, 28));
        aNew.setEndDate(new DateMidnight(2012, 1, 28));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(
            new ArrayList<Application>()); // no overlap because of different dates

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // SECOND CHECK: OVERLAP WITH HALF DAY

        // existent application for morning i.e. list of existent applications for full day is empty, but list of
        // existent applications for morning is not empty; so there is an overlap with morning application.
        // expected value == 2
        list = new ArrayList<Application>();

        a = new Application();
        a.setHowLong(DayLength.MORNING);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 23));
        a.setPerson(person);
        list.add(a);

        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // existent application for noon i.e. there are no existent applications for full day, but for noon (but not for
        // morning!) --> lists with existent applications are empty because there is no overlap! expected value == 1
        a.setHowLong(DayLength.NOON);

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(
            new ArrayList<Application>());

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);
    }


    /** Test of checkOverlapForNoon method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForNoon() {

        OverlapCase returnValue;
        List<Application> list = new ArrayList<Application>();

        // FIRST CHECK: OVERLAP WITH FULL DAY PERIOD

        // there is a holiday from 23.01.2012 to 27.01.2012
        // try to apply for leave for a half day on 25.01.2012
        // list of existent applications for full day contains one entry (Application a)
        // expected return value == 2
        Application a = new Application();
        a.setHowLong(DayLength.FULL);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 27));
        a.setPerson(person);
        list.add(a);

        Application aNew = new Application();
        aNew.setHowLong(DayLength.NOON);
        aNew.setStartDate(new DateMidnight(2012, 1, 25));
        aNew.setEndDate(new DateMidnight(2012, 1, 25));
        aNew.setPerson(person);

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at start of existent application
        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at end of existent application

        aNew.setStartDate(new DateMidnight(2012, 1, 27));
        aNew.setEndDate(new DateMidnight(2012, 1, 27));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application has no overlap with full day period
        // i.e. list of existent applications for full day is empty (and for half days empty too)
        // expected value == 1
        aNew.setStartDate(new DateMidnight(2012, 1, 28));
        aNew.setEndDate(new DateMidnight(2012, 1, 28));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>()); // no existent half day applications
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(new ArrayList<Application>()); // no overlap because of different dates

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // SECOND CHECK: OVERLAP WITH HALF DAY

        // existent application for noon i.e. list of existent applications for full day is empty, but list of
        // existent applications for noon is not empty; so there is an overlap with noon application.
        // expected value == 2
        list = new ArrayList<Application>();

        a = new Application();
        a.setHowLong(DayLength.NOON);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 23));
        a.setPerson(person);
        list.add(a);

        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // existent application for noon i.e. there are no existent applications for full day, but for noon --> lists
        // with existent applications are empty because there is no overlap!
        // expected value == 1
        a.setHowLong(DayLength.MORNING);

        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(new ArrayList<Application>());

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);
    }


    /** Test of getUsedVacationDaysOfPersonForYear method, of class ApplicationServiceImpl. */
    @Test
    public void testGetUsedVacationDaysOfPersonForYear() {

        int year = 2012;

        // expected to be used for calculation : 2 days
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 2));
        a1.setEndDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 4));
        a1.setDays(BigDecimal.valueOf(2));
        a1.setStatus(ApplicationStatus.WAITING);
        a1.setSupplementaryApplication(false);

        // expected to be used for calculation : 3 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(year, DateTimeConstants.APRIL, 3));
        a2.setEndDate(new DateMidnight(year, DateTimeConstants.APRIL, 6));
        a2.setDays(BigDecimal.valueOf(3));
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setSupplementaryApplication(false);

        // expected to be NOT used for calculation : 7 days - status is cancelled
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(year, DateTimeConstants.JUNE, 12));
        a3.setEndDate(new DateMidnight(year, DateTimeConstants.JUNE, 20));
        a3.setDays(BigDecimal.valueOf(7));
        a3.setStatus(ApplicationStatus.CANCELLED);
        a3.setSupplementaryApplication(false);

        // expected to be NOT used for calculation : 6 days - application spans December and January
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 21));
        a4.setEndDate(new DateMidnight(year + 1, DateTimeConstants.JANUARY, 3));
        a4.setDays(BigDecimal.valueOf(6)); // 4 days before 1st January
        a4.setStatus(ApplicationStatus.ALLOWED);
        a4.setSupplementaryApplication(false);

        List<Application> apps = new ArrayList<Application>();
        apps.add(a1);
        apps.add(a2);
        apps.add(a3);
        apps.add(a4);

        Mockito.when(applicationDAO.getNotCancelledApplicationsByPersonAndYear(ApplicationStatus.CANCELLED, person,
                new DateMidnight(year, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(year, DateTimeConstants.DECEMBER, 31).toDate())).thenReturn(apps);

        // expected to be used for calculation : 4 days
        Application sa1 = new Application();
        sa1.setStartDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 21));
        sa1.setEndDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 31));
        sa1.setDays(BigDecimal.valueOf(4));
        sa1.setStatus(ApplicationStatus.ALLOWED);
        sa1.setSupplementaryApplication(true);

        // expected to be NOT used for calculation : 2 days - status is rejected
        Application sa2 = new Application();
        sa2.setStartDate(new DateMidnight(year, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(new DateMidnight(year, DateTimeConstants.JANUARY, 3));
        sa2.setDays(BigDecimal.valueOf(2));
        sa2.setStatus(ApplicationStatus.REJECTED);
        sa2.setSupplementaryApplication(true);

        List<Application> sApps = new ArrayList<Application>();
        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(applicationDAO.getSupplementalApplicationsByPersonAndYear(person,
                new DateMidnight(year, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(year, DateTimeConstants.DECEMBER, 31).toDate())).thenReturn(sApps);

        // so it's ecpected that the calculation occurs following way:
        // a1 : +2
        // a2 : +3
        // ( a3 : 7 ) not used
        // ( a4 : 6 ) not used
        // sa1 : +4
        // ( sa2 : 2 ) not used
        // total number = 2 + 3 + 4 = 9 days

        BigDecimal returnValue = instance.getUsedVacationDaysOfPersonForYear(person, year);
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9), returnValue);
    }


    /** Test of getUsedVacationDaysBeforeAprilOfPerson method, of class ApplicationServiceImpl. */
    @Test
    public void testGetUsedVacationDaysBeforeAprilOfPerson() {

        int year = 2012;

        // expected to be used for calculation : 2 days
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 2));
        a1.setEndDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 4));
        a1.setDays(BigDecimal.valueOf(2));
        a1.setStatus(ApplicationStatus.WAITING);
        a1.setSupplementaryApplication(false);
        a1.setHowLong(DayLength.FULL);

        // expected to be used for calculation : 3 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(year, DateTimeConstants.MARCH, 5));
        a2.setEndDate(new DateMidnight(year, DateTimeConstants.MARCH, 7));
        a2.setDays(BigDecimal.valueOf(3));
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setSupplementaryApplication(false);
        a2.setHowLong(DayLength.FULL);

        // expected to be NOT used for calculation : 7 days - status is cancelled
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(year, DateTimeConstants.JANUARY, 12));
        a3.setEndDate(new DateMidnight(year, DateTimeConstants.JANUARY, 20));
        a3.setDays(BigDecimal.valueOf(7));
        a3.setStatus(ApplicationStatus.CANCELLED);
        a3.setSupplementaryApplication(false);
        a3.setHowLong(DayLength.FULL);

        // expected to be used for calculation : special calculation - application spanning March and April
        // 5 days before April, 3 days after April
        // expected that only days before April are used for calculation: 5 days
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(year, DateTimeConstants.MARCH, 26));
        a4.setEndDate(new DateMidnight(year + 1, DateTimeConstants.APRIL, 4));
        a4.setDays(BigDecimal.valueOf(5 + 3)); // 5 days before April, 3 days after April
        a4.setStatus(ApplicationStatus.ALLOWED);
        a4.setSupplementaryApplication(false);
        a4.setHowLong(DayLength.FULL);

        List<Application> apps = new ArrayList<Application>();
        apps.add(a1);
        apps.add(a2);
        apps.add(a3);
        apps.add(a4);

        Mockito.when(applicationDAO.getApplicationsBeforeAprilByPersonAndYear(person,
                new DateMidnight(year, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(year, DateTimeConstants.MARCH, 31).toDate())).thenReturn(apps);

        // so it's ecpected that the calculation occurs following way:
        // a1 : +2
        // a2 : +3
        // ( a3 : 7 ) not used
        // ( a4 : 5 + 3 ) only 5 used
        // total number = 2 + 3 + 5 = 10 days

        BigDecimal returnValue = instance.getUsedVacationDaysBeforeAprilOfPerson(person, year);
        assertNotNull(returnValue);
        assertEquals(BigDecimal.TEN.setScale(2), returnValue);
    }


    /** Test of rollback method, of class ApplicationServiceImpl. */
    @Test
    public void testRollback() {

        calculationService = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return CURRENT_YEAR;
            }
        };

        entitlement.setVacationDays(BigDecimal.valueOf(24));
        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));

        application.setHowLong(DayLength.FULL);

        // application before April, 11 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 1));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 15));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(BigDecimal.valueOf(20.0));

        // simulate there aren't any applications before April
        instance = new ApplicationServiceImpl(applicationDAO, accountService, cryptoService, calendarService,
                calculationService, mailService) {

            @Override
            public BigDecimal getUsedVacationDaysBeforeAprilOfPerson(Person person, int year) {

                return BigDecimal.ZERO;
            }
        };

        instance.rollback(application);

        assertEquals(BigDecimal.valueOf(5), accountOne.getRemainingVacationDays()); // remaining vacation days
                                                                                    // are filled up to number
                                                                                    // of entitlement's
                                                                                    // remaining vacation days
        assertEquals(BigDecimal.valueOf(26).setScale(2), accountOne.getVacationDays()); // 11 - 5 = 6 days are
                                                                                        // added to account's
                                                                                        // vacation days

        // application after April, 8 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 18));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 28));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(BigDecimal.valueOf(15.0));
        accountOne.setRemainingVacationDaysExpire(true);

        instance.rollback(application);

        assertEquals(BigDecimal.valueOf(15.0 + 8.0).setScale(2), accountOne.getVacationDays());
        assertEquals(BigDecimal.ZERO, accountOne.getRemainingVacationDays()); // not changed

        // application between March and April, 4 plus 7 work days (11.0)
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 28));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.APRIL, 11));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountOne.setRemainingVacationDays(BigDecimal.ZERO);
        accountOne.setVacationDays(BigDecimal.valueOf(17.0));

        instance.rollback(application);

        assertEquals(entitlement.getVacationDays().setScale(2), accountOne.getVacationDays());
        assertEquals(BigDecimal.valueOf(4).setScale(2), accountOne.getRemainingVacationDays());

        // application spans December and January

        HolidayEntitlement ent = new HolidayEntitlement();
        ent.setRemainingVacationDays(BigDecimal.valueOf(5));
        ent.setVacationDays(BigDecimal.valueOf(28));
        ent.setPerson(person);
        ent.setYear(NEXT_YEAR);

        Mockito.when(accountService.getHolidayEntitlement(NEXT_YEAR, person)).thenReturn(ent);

        accountTwo.setRemainingVacationDays(BigDecimal.valueOf(2));
        accountTwo.setVacationDays(BigDecimal.valueOf(15));

        accNew.setRemainingVacationDays(BigDecimal.ZERO);
        accNew.setVacationDays(BigDecimal.valueOf(24));

        application.setHowLong(DayLength.FULL);

        // override of getCurrentYear() doesn't work see test to this case in
        // CalculationServiceTest#testAddDaysOfApplicationSpanningDecemberAndJanuary

        // two cases 1 : cancelling occur before 1st January 2 : cancelling occur after 1st January

        // 1 : cancelling occur before 1st January
        //
        // calculationService = mock(CalculationService.class);
        // Mockito.when(calculationService.getCurrentYear()).thenReturn(2012);
        //
        // ent = new HolidayEntitlement(); ent.setRemainingVacationDays(BigDecimal.valueOf(5));
        // ent.setVacationDays(BigDecimal.valueOf(28)); ent.setPerson(person); ent.setYear(NEXT_YEAR + 1);
        //
        // Mockito.when(accountService.getHolidayEntitlement(NEXT_YEAR + 1, person)).thenReturn(ent);
        //
        // application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.DECEMBER, 25)); // 2.5 days before 1st
        // // January application.setEndDate(new DateMidnight(NEXT_YEAR + 1, DateTimeConstants.JANUARY, 8)); // 5
        // days after 1st // January
        //
        // application.setDays(BigDecimal.valueOf(7.5));
        //
        // remaining vacation days expire accountTwo.setRemainingVacationDaysExpire(true);
        //
        // instance.rollback(application);
        //
        // assertEquals(BigDecimal.valueOf(15 + 2.5).setScale(2), accountTwo.getVacationDays());
        // assertEquals(BigDecimal.valueOf(24 + 5).setScale(2), accNew.getVacationDays());

        // remaining vacation days do not expire accountTwo.setRemainingVacationDaysExpire(false);
        // accountTwo.setRemainingVacationDays(BigDecimal.valueOf(2));
        // accountTwo.setVacationDays(BigDecimal.valueOf(15));
        //
        // accNew.setRemainingVacationDays(BigDecimal.ZERO); accNew.setVacationDays(BigDecimal.valueOf(24));
        //
        // simulate there aren't any applications before April instance = new ApplicationServiceImpl(applicationDAO,
        // accountService, cryptoService, calendarService, calculationService, mailService) {
        //
        // @Override public BigDecimal getUsedVacationDaysBeforeAprilOfPerson(Person person, int year) {
        //
        // return BigDecimal.ZERO; } };
        //
        // instance.rollback(application);
        //
        // assertEquals(BigDecimal.valueOf(2 + 2.5).setScale(2), accountOne.getRemainingVacationDays());
        // assertEquals(BigDecimal.valueOf(15), accountOne.getVacationDays()); assertEquals(BigDecimal.valueOf(24 +
        // 5).setScale(2), accountTwo.getVacationDays());

        // 2 : cancelling occur after 1st January

// calculationService = new CalculationService(calendarService, accountService) {
//
// @Override
// protected int getCurrentYear() {
//
// return NEXT_YEAR;
// }
// };
//
// accountOne.setVacationDays(BigDecimal.valueOf(3));
// accountOne.setRemainingVacationDays(BigDecimal.valueOf(2));
// accountOne.setRemainingVacationDaysExpire(true);
//
// ent.setVacationDays(BigDecimal.valueOf(28));
// ent.setRemainingVacationDays(BigDecimal.valueOf(3)); // new year's entitlement's remaining vacation days == last
//                                                             // year's account's vacation days
//
//        accountTwo.setVacationDays(BigDecimal.valueOf(27));
//        accountTwo.setRemainingVacationDays(BigDecimal.valueOf(0));
//
//        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 28)); // 3 days
//        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 8)); // 4 days
//
//        instance.rollback(application);
//
//        assertEquals(BigDecimal.valueOf(28), ent.getVacationDays()); // unchanged
//        assertEquals(BigDecimal.valueOf(3 + 3).setScale(2), ent.getRemainingVacationDays()); // old value (3) plus days
//                                                                                             // before January (3)
//        assertEquals(BigDecimal.valueOf(3 + 3).setScale(2), accountOne.getVacationDays()); // old value (3) plus days
//                                                                                           // before January (3)
//        assertEquals(BigDecimal.valueOf(2), accountOne.getRemainingVacationDays()); // unchanged
//        assertEquals(BigDecimal.valueOf(27), accountTwo.getVacationDays()); // unchanged because remaining
//                                                                            // vacation days are filled at
//                                                                            // first
//        assertEquals(BigDecimal.valueOf(0 + 4).setScale(2), accountTwo.getRemainingVacationDays()); // old value (0) plus days after January (4)
//
//        // application is in future (next year)
//
//        calculationService = new CalculationService(calendarService, accountService) {
//
//            @Override
//            protected int getCurrentYear() {
//
//                return CURRENT_YEAR;
//            }
//        };
//
//        accountTwo.setRemainingVacationDays(BigDecimal.ZERO); // because it is not yet after 1st January,
//                                                              // automatically set 0
//        accountTwo.setVacationDays(BigDecimal.valueOf(21));
//
//        application.setHowLong(DayLength.FULL);
//
//        // 9 days
//        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 4));
//        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 17));
//        application.setDays(BigDecimal.valueOf(9));
//
//        instance.rollback(application);
//
//        assertEquals(BigDecimal.valueOf(21 + 9), accountOne.getVacationDays()); // old value (21) plus number of days
//                                                                                // (9)
    }
}
