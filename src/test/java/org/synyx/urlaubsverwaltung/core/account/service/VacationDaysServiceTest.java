package org.synyx.urlaubsverwaltung.core.account.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.period.NowService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class VacationDaysServiceTest {

    private VacationDaysService vacationDaysService;

    private ApplicationService applicationService;
    private NowService nowService;

    @Before
    public void setUp() throws IOException {

        applicationService = Mockito.mock(ApplicationService.class);
        nowService = Mockito.mock(NowService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.of(workingTime));

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        WorkDaysService calendarService = new WorkDaysService(new PublicHolidaysService(settingsService),
                workingTimeService, settingsService);

        vacationDaysService = new VacationDaysService(calendarService, nowService, applicationService);
    }


    @Test
    public void testGetDaysBeforeApril() {

        Person person = TestDataCreator.createPerson("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 29));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        a1.setDayLength(DayLength.FULL);
        a1.setStatus(ApplicationStatus.ALLOWED);
        a1.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        a1.setPerson(person);

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setDayLength(DayLength.FULL);
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        a2.setPerson(person);

        // 4 days
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setDayLength(DayLength.FULL);
        a3.setStatus(ApplicationStatus.WAITING);
        a3.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        a3.setPerson(person);

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setDayLength(DayLength.FULL);
        a4.setStatus(ApplicationStatus.WAITING);
        a4.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        a4.setPerson(person);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(a1, a2, a3, a4));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2 + 5 + 4 + 2 = 13

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("13.0"), days);
    }


    @Test
    public void testGetDaysAfterApril() {

        Person person = TestDataCreator.createPerson("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        // 4 days at all: 2.5 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 27));
        a1.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 3));
        a1.setDayLength(DayLength.FULL);
        a1.setPerson(person);
        a1.setStatus(ApplicationStatus.ALLOWED);
        a1.setVacationType(getVacationType(VacationCategory.HOLIDAY));

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        a2.setDayLength(DayLength.FULL);
        a2.setPerson(person);
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setVacationType(getVacationType(VacationCategory.HOLIDAY));

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setDayLength(DayLength.FULL);
        a4.setPerson(person);
        a4.setStatus(ApplicationStatus.WAITING);
        a4.setVacationType(getVacationType(VacationCategory.HOLIDAY));

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(a1, a2, a4));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2.5 + 5 + 4 = 11.5

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("11.5"), days);
    }


    @Test
    public void testGetDaysBetweenMilestonesWithInactiveApplicationsForLeaveAndOfOtherVacationTypeThanHoliday() {

        Person person = TestDataCreator.createPerson("horscht");

        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application cancelledHoliday = new Application();
        cancelledHoliday.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        cancelledHoliday.setStatus(ApplicationStatus.CANCELLED);

        Application rejectedHoliday = new Application();
        rejectedHoliday.setVacationType(getVacationType(VacationCategory.HOLIDAY));
        rejectedHoliday.setStatus(ApplicationStatus.REJECTED);

        Application waitingSpecialLeave = new Application();
        waitingSpecialLeave.setVacationType(getVacationType(VacationCategory.SPECIALLEAVE));
        waitingSpecialLeave.setStatus(ApplicationStatus.WAITING);

        Application allowedSpecialLeave = new Application();
        allowedSpecialLeave.setVacationType(getVacationType(VacationCategory.SPECIALLEAVE));
        allowedSpecialLeave.setStatus(ApplicationStatus.ALLOWED);

        Application waitingUnpaidLeave = new Application();
        waitingUnpaidLeave.setVacationType(getVacationType(VacationCategory.UNPAIDLEAVE));
        waitingUnpaidLeave.setStatus(ApplicationStatus.WAITING);

        Application allowedUnpaidLeave = new Application();
        allowedUnpaidLeave.setVacationType(getVacationType(VacationCategory.UNPAIDLEAVE));
        allowedUnpaidLeave.setStatus(ApplicationStatus.ALLOWED);

        Application waitingOvertime = new Application();
        waitingOvertime.setVacationType(getVacationType(VacationCategory.OVERTIME));
        waitingOvertime.setStatus(ApplicationStatus.WAITING);

        Application allowedOvertime = new Application();
        allowedOvertime.setVacationType(getVacationType(VacationCategory.OVERTIME));
        allowedOvertime.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(cancelledHoliday, rejectedHoliday, waitingSpecialLeave, allowedSpecialLeave,
                    waitingUnpaidLeave, allowedUnpaidLeave, waitingOvertime, allowedOvertime));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);

        Assert.assertNotNull(days);
        Assert.assertEquals(BigDecimal.ZERO, days);
    }


    @Test
    public void testGetVacationDaysLeft() {

        initCustomService("4", "20");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account);

        Assert.assertNotNull("Should not be null", vacationDaysLeft);

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of vacation days", new BigDecimal("12"), vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Number of vacation days already used for next year", BigDecimal.ZERO, vacationDaysLeft.getVacationDaysUsedNextYear());

    }


    @Test
    public void testGetVacationDaysLeft_WithRemainingAlreadyUsed() {

        initCustomService("4", "20");

        // 36 Total, using 24, so 12 left
        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        Account nextYear = new Account();
        nextYear.setAnnualVacationDays(new BigDecimal("12"));
        nextYear.setVacationDays(new BigDecimal("12"));
        nextYear.setRemainingVacationDays(new BigDecimal("20"));
        nextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));


        VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, Optional.of(nextYear));

        Assert.assertEquals("Number of vacation days already used for next year", new BigDecimal("12"), vacationDaysLeft.getVacationDaysUsedNextYear());

        Assert.assertEquals("Wrong number of vacation days", BigDecimal.ZERO, vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", BigDecimal.ZERO,
                vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days that do not expire", BigDecimal.ZERO,
                vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }



    @Test
    public void testGetTotalVacationDaysForPastYear() {

        Mockito.when(nowService.now()).thenReturn(new DateMidnight(2015, 4, 2));

        initCustomService("4", "1");

        Account account = new Account();
        account.setValidFrom(new DateMidnight(2014, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("31"), leftDays);
    }


    @Test
    public void testGetTotalVacationDaysForThisYearBeforeApril() {

        Mockito.when(nowService.now()).thenReturn(new DateMidnight(2015, 3, 2));

        initCustomService("4", "1");

        Account account = new Account();
        account.setValidFrom(new DateMidnight(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("32"), leftDays);
    }


    @Test
    public void testGetTotalVacationDaysForThisYearAfterApril() {

        Mockito.when(nowService.now()).thenReturn(new DateMidnight(2015, 4, 2));

        initCustomService("4", "3");

        Account account = new Account();
        account.setValidFrom(new DateMidnight(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("30"), leftDays);
    }


    private void initCustomService(final String daysBeforeApril, final String daysAfterApril) {

        vacationDaysService = new VacationDaysService(Mockito.mock(WorkDaysService.class), nowService,
                applicationService) {

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


    private VacationType getVacationType(VacationCategory category) {

        return TestDataCreator.createVacationType(category);
    }
}
