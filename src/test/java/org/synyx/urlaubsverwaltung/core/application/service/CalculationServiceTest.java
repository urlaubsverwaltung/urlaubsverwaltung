package org.synyx.urlaubsverwaltung.core.application.service;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Unit test for {@link CalculationService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalculationServiceTest {

    private CalculationService service;
    private ApplicationDAO applicationDAO;
    private AccountService accountService;
    private OwnCalendarService calendarService;

    @Before
    public void setUp() {

        applicationDAO = Mockito.mock(ApplicationDAO.class);
        accountService = Mockito.mock(AccountService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        calendarService = new OwnCalendarService(new JollydayCalendar(), workingTimeService);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                Mockito.any(DateMidnight.class))).thenReturn(workingTime);

        service = new CalculationService(applicationDAO, accountService, calendarService);
    }


    /**
     * Test of checkApplication method, of class CalculationService.
     */
    @Test
    public void testCheckApplication() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 29));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2 before January + 2 after January

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setHowLong(DayLength.FULL);
        a3.setDays(BigDecimal.valueOf(4));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a2, a3));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a1));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a4));

        DateMidnight firstMilestone2 = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone2 = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application b1 = new Application();
        b1.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 27));
        b1.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 3));
        b1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2.5 before January + 2 after January

        Application b2 = new Application();
        b2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        b2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        b2.setHowLong(DayLength.FULL);
        b2.setDays(BigDecimal.valueOf(5));

        Application b4 = new Application();
        b4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        b4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        b4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(b2));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(b4));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(b1));

        Account account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(28),
                BigDecimal.valueOf(5), true);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(28));
        account.setVacationDays(BigDecimal.valueOf(28));

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Application n = new Application();
        n.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        n.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 21));
        n.setDays(BigDecimal.valueOf(2));
        n.setPerson(person);
        n.setHowLong(DayLength.FULL);

        Assert.assertTrue("Should be enough vacation days to apply for leave", service.checkApplication(n));

        // set days to little so it can't be true
        account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(20),
                BigDecimal.valueOf(4.5), true);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(20));
        account.setVacationDays(BigDecimal.valueOf(20));

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Assert.assertFalse("Should NOT be enough vacation days to apply for leave", service.checkApplication(n));

        // set days so that sum is exact zero - so check is true
        account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(20),
                BigDecimal.valueOf(6.5), true);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(20));
        account.setVacationDays(BigDecimal.valueOf(20));

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Assert.assertTrue("Should be enough vacation days to apply for leave", service.checkApplication(n));

        // remaining vacation days do not expire so it can be done
        account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(5),
                BigDecimal.valueOf(22), false);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(5));
        account.setVacationDays(BigDecimal.valueOf(5));

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Assert.assertTrue("Should be enough vacation days to apply for leave", service.checkApplication(n));
    }


    @Test
    public void testCheckApplicationForNextYear() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setHowLong(DayLength.FULL);
        a3.setDays(BigDecimal.valueOf(4));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a2, a3));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(new ArrayList<Application>());

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a4));

        DateMidnight firstMilestone2 = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone2 = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application b2 = new Application();
        b2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        b2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        b2.setHowLong(DayLength.FULL);
        b2.setDays(BigDecimal.valueOf(5));

        Application b4 = new Application();
        b4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        b4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        b4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(b2));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(b4));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone2.toDate(),
                lastMilestone2.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(new ArrayList<Application>());

        Account account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(28),
                BigDecimal.valueOf(5), true);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(28));
        account.setVacationDays(BigDecimal.valueOf(28));

        Application n = new Application();
        n.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 20));
        n.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        n.setHowLong(DayLength.FULL);
        n.setPerson(person);
        n.setHowLong(DayLength.FULL);
        // at all there are 8 + 2 days (but only the 2 days of the new year are part of the calculation)

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Assert.assertTrue(service.checkApplication(n));

        account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(10),
                BigDecimal.valueOf(5), true);

        Mockito.when(accountService.calculateActualVacationDays(account)).thenReturn(BigDecimal.valueOf(10));
        account.setVacationDays(BigDecimal.valueOf(10));

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);
        Mockito.when(accountService.getOrCreateNewAccount(2012, person)).thenReturn(account);

        Assert.assertFalse(service.checkApplication(n));
    }


    @Test
    public void testGetDaysBeforeApril() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 29));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2 before January + 2 after January

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setHowLong(DayLength.FULL);
        a3.setDays(BigDecimal.valueOf(4));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a2, a3));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a1));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a4));

        BigDecimal days = service.getDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2 + 5 + 4 + 2 = 13

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("13.0"), days);
    }


    @Test
    public void testGetDaysAfterApril() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 27));
        a1.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2.5 before January + 2 after January

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a2));

        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a4));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(),
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED))
            .thenReturn(Arrays.asList(a1));

        BigDecimal days = service.getDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2.5 + 5 + 4 = 11.5

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("11.5"), days);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysEqualsDaysBeforeAprilAndNotExpiring() {

        initCustomService("8", "28");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("8"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("2"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysEqualsDaysBeforeAprilAndExpiring() {

        initCustomService("8", "28");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("8"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("2"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysLessThanDaysBeforeAprilAndNotExpiring() {

        initCustomService("5", "15");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("23"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysLessThanDaysBeforeAprilAndExpiring() {

        initCustomService("5", "15");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("23"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysGreaterThanDaysBeforeAprilAndNotExpiring() {

        initCustomService("3", "16.5");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("4"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("14.5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysGreaterThanDaysBeforeAprilAndExpiring() {

        initCustomService("3", "16.5");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("4"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("13.5"), result);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithLessDaysBeforeApril() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2.5"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20.5"), vacationDays);
        Assert.assertEquals(BigDecimal.ZERO, remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithEqualsDaysBeforeApril() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithMoreDaysBeforeApril() {

        initCustomService("5", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("17"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithLessDaysBeforeAprilExpiring() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2.5"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0.5"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithEqualsDaysBeforeAprilExpiring() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithMoreDaysBeforeAprilExpiring() {

        initCustomService("5", "10");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("17"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationDaysWhenNoVacationYetAndRemainingVacationDaysDoNotExpire() {

        initCustomService("0", "0");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("28"));
        account.setVacationDays(new BigDecimal("28"));
        account.setRemainingVacationDays(new BigDecimal("4"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("28"), vacationDays);
        Assert.assertEquals(new BigDecimal("4"), remainingVacationDays);
    }

    @Test
    public void testCalculateLeftVacationsDaysWhenNoVacationAfterAprilAndRemainingVacationDaysDoNotExpire() {

        initCustomService("1", "0");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(false);
        account.setAnnualVacationDays(new BigDecimal("25"));
        account.setVacationDays(new BigDecimal("25"));
        account.setRemainingVacationDays(new BigDecimal("12.5"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("25"), vacationDays);
        Assert.assertEquals(new BigDecimal("11.5"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationDaysWhenNoVacationYetAndRemainingVacationDaysDoExpire() {

        initCustomService("0", "0");

        Account account = new Account();
        account.setRemainingVacationDaysExpire(true);
        account.setAnnualVacationDays(new BigDecimal("28"));
        account.setVacationDays(new BigDecimal("28"));
        account.setRemainingVacationDays(new BigDecimal("4"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("28"), vacationDays);
        Assert.assertEquals(new BigDecimal("4"), remainingVacationDays);
    }


    private void initCustomService(final String daysBeforeApril, final String daysAfterApril) {

        service = new CalculationService(applicationDAO, accountService, calendarService) {

            @Override
            protected BigDecimal getDaysBeforeApril(Account account) {

                return new BigDecimal(daysBeforeApril);
            }


            @Override
            protected BigDecimal getDaysAfterApril(Account account) {

                return new BigDecimal(daysAfterApril);
            }
        };
    }
}
