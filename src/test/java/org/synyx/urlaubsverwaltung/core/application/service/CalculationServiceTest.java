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
import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.Date;
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
    public void setUp() throws IOException {

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


    @Test
    public void testCheckApplication() {

        Person person = new Person();
        person.setLoginName("horscht");

        initCustomService("5", "15");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 21));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setHowLong(DayLength.FULL);

        Account account = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(28),
                BigDecimal.valueOf(5), BigDecimal.ZERO);
        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);

        // vacation days would be left after this application for leave
        account.setVacationDays(BigDecimal.valueOf(28));

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));

        // not enough vacation days for this application for leave
        account.setVacationDays(BigDecimal.valueOf(10));

        Assert.assertFalse("Should NOT be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));

        // enough vacation days for this application for leave, but none would be left
        account.setVacationDays(BigDecimal.valueOf(20));

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));
    }


    @Test
    public void testGetDaysBeforeApril() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 29));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        a1.setStatus(ApplicationStatus.ALLOWED);
        a1.setPerson(person);

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setHowLong(DayLength.FULL);
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setPerson(person);

        // 4 days
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setHowLong(DayLength.FULL);
        a3.setStatus(ApplicationStatus.WAITING);
        a3.setPerson(person);

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        a4.setStatus(ApplicationStatus.WAITING);
        a4.setPerson(person);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(a1, a2, a3, a4));

        BigDecimal days = service.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
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

        // 4 days at all: 2.5 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 27));
        a1.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        a1.setPerson(person);
        a1.setStatus(ApplicationStatus.ALLOWED);

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        a2.setHowLong(DayLength.FULL);
        a2.setPerson(person);
        a2.setStatus(ApplicationStatus.ALLOWED);

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        a4.setPerson(person);
        a4.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(a1, a2, a4));

        BigDecimal days = service.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2.5 + 5 + 4 = 11.5

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("11.5"), days);
    }


    @Test
    public void testGetDaysBetweenMilestonesWithInactiveApplicationsForLeave() {

        Person person = new Person();
        person.setLoginName("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application cancelledApplicationForLeave = new Application();
        cancelledApplicationForLeave.setStatus(ApplicationStatus.CANCELLED);

        Application rejectedApplicationForLeave = new Application();
        rejectedApplicationForLeave.setStatus(ApplicationStatus.REJECTED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(
                cancelledApplicationForLeave, rejectedApplicationForLeave));

        BigDecimal days = service.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);

        Assert.assertNotNull(days);
        Assert.assertEquals(BigDecimal.ZERO, days);
    }


    @Test
    public void testCalculateLeftVacationDaysAfterAprilRemainingVacationDaysEqualsDaysBeforeAprilAndNotExpiring() {

        initCustomService("8", "28");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("8"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("8"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("2"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysEqualsDaysBeforeAprilAndExpiring() {

        initCustomService("8", "28");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("8"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("2"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysLessThanDaysBeforeAprilAndNotExpiring() {

        initCustomService("5", "15");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("23"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysLessThanDaysBeforeAprilAndExpiring() {

        initCustomService("5", "15");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("23"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysGreaterThanDaysBeforeAprilAndNotExpiring() {

        initCustomService("3", "16.5");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("4"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("4"));

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("14.5"), result);
    }


    @Test
    public void testCalculateLeftVacationDaysRemainingVacationDaysGreaterThanDaysBeforeAprilAndExpiring() {

        initCustomService("3", "16.5");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("4"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal result = service.calculateLeftVacationDays(account);

        Assert.assertEquals(new BigDecimal("13.5"), result);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithLessDaysBeforeApril() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2.5"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2.5"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20.5"), vacationDays);
        Assert.assertEquals(BigDecimal.ZERO, remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithEqualsDaysBeforeApril() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithMoreDaysBeforeApril() {

        initCustomService("5", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("17"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithLessDaysBeforeAprilExpiring() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2.5"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0.5"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithEqualsDaysBeforeAprilExpiring() {

        initCustomService("2", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("20"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftRemainingVacationDaysWithMoreDaysBeforeAprilExpiring() {

        initCustomService("5", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("2"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("17"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationDaysWhenNoVacationYetAndRemainingVacationDaysDoNotExpire() {

        initCustomService("0", "0");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("28"));
        account.setVacationDays(new BigDecimal("28"));
        account.setRemainingVacationDays(new BigDecimal("4"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("4"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("28"), vacationDays);
        Assert.assertEquals(new BigDecimal("4"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationsDaysWhenNoVacationAfterAprilAndRemainingVacationDaysDoNotExpire() {

        initCustomService("1", "0");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("25"));
        account.setVacationDays(new BigDecimal("25"));
        account.setRemainingVacationDays(new BigDecimal("12.5"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("12.5"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("25"), vacationDays);
        Assert.assertEquals(new BigDecimal("11.5"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationDaysWhenNoVacationYetAndRemainingVacationDaysDoExpire() {

        initCustomService("0", "0");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("28"));
        account.setVacationDays(new BigDecimal("28"));
        account.setRemainingVacationDays(new BigDecimal("4"));
        account.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("28"), vacationDays);
        Assert.assertEquals(new BigDecimal("4"), remainingVacationDays);
    }


    @Test
    public void testCalculateLeftVacationsDaysWhenRemainingVacationDaysTakenFullyBeforeApril() {

        initCustomService("5", "10");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("25"));
        account.setVacationDays(new BigDecimal("25"));
        account.setRemainingVacationDays(new BigDecimal("5"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        BigDecimal vacationDays = service.calculateLeftVacationDays(account);
        BigDecimal remainingVacationDays = service.calculateLeftRemainingVacationDays(account);

        Assert.assertEquals(new BigDecimal("15"), vacationDays);
        Assert.assertEquals(new BigDecimal("0"), remainingVacationDays);
    }


    private void initCustomService(final String daysBeforeApril, final String daysAfterApril) {

        service = new CalculationService(applicationDAO, accountService, calendarService) {

            @Override
            protected BigDecimal getUsedDaysBeforeApril(Account account) {

                return new BigDecimal(daysBeforeApril);
            }


            @Override
            protected BigDecimal getUsedDaysAfterApril(Account account) {

                return new BigDecimal(daysAfterApril);
            }
        };
    }
}
