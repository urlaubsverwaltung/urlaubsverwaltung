package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatisticsBuilder}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveStatisticsBuilderTest {

    private AccountService accountService;
    private ApplicationService applicationService;
    private WorkDaysService calendarService;
    private VacationDaysService vacationDaysService;
    private OvertimeService overtimeService;

    private ApplicationForLeaveStatisticsBuilder builder;

    @Before
    public void setUp() {

        accountService = Mockito.mock(AccountService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        calendarService = Mockito.mock(WorkDaysService.class);
        vacationDaysService = Mockito.mock(VacationDaysService.class);
        overtimeService = Mockito.mock(OvertimeService.class);

        builder = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, calendarService,
                vacationDaysService, overtimeService);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenPersonIsNull() {

        builder.build(null, new DateMidnight(2015, 1, 1), new DateMidnight(2015, 12, 31));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenFromDateIsNull() {

        builder.build(Mockito.mock(Person.class), null, new DateMidnight(2015, 12, 31));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenToDateIsNull() {

        builder.build(Mockito.mock(Person.class), new DateMidnight(2014, 1, 1), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {

        builder.build(Mockito.mock(Person.class), new DateMidnight(2014, 1, 1), new DateMidnight(2015, 1, 1));
    }


    @Test
    public void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        DateMidnight from = new DateMidnight(2014, 1, 1);
        DateMidnight to = new DateMidnight(2014, 12, 31);

        Person person = Mockito.mock(Person.class);
        Account account = Mockito.mock(Account.class);

        Mockito.when(person.getEmail()).thenReturn("muster@muster.de");
        Mockito.when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(Mockito.eq(account)))
            .thenReturn(BigDecimal.TEN);
        Mockito.when(overtimeService.getTotalOvertimeForPerson(person)).thenReturn(BigDecimal.TEN);
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        Application holidayWaiting = new Application();
        holidayWaiting.setVacationType(VacationType.HOLIDAY);
        holidayWaiting.setStartDate(new DateMidnight(2014, 10, 13));
        holidayWaiting.setEndDate(new DateMidnight(2014, 10, 13));
        holidayWaiting.setStatus(ApplicationStatus.WAITING);
        holidayWaiting.setPerson(person);

        Application holidayTemporaryAllowed = new Application();
        holidayTemporaryAllowed.setVacationType(VacationType.HOLIDAY);
        holidayTemporaryAllowed.setStartDate(new DateMidnight(2014, 10, 12));
        holidayTemporaryAllowed.setEndDate(new DateMidnight(2014, 10, 12));
        holidayTemporaryAllowed.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        holidayTemporaryAllowed.setPerson(person);

        Application holidayAllowed = new Application();
        holidayAllowed.setVacationType(VacationType.HOLIDAY);
        holidayAllowed.setStartDate(new DateMidnight(2014, 10, 14));
        holidayAllowed.setEndDate(new DateMidnight(2014, 10, 14));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);
        holidayAllowed.setPerson(person);

        Application holidayRejected = new Application();
        holidayRejected.setVacationType(VacationType.HOLIDAY);
        holidayRejected.setStartDate(new DateMidnight(2014, 11, 6));
        holidayRejected.setEndDate(new DateMidnight(2014, 11, 6));
        holidayRejected.setStatus(ApplicationStatus.REJECTED);
        holidayRejected.setPerson(person);

        Application specialLeaveWaiting = new Application();
        specialLeaveWaiting.setVacationType(VacationType.SPECIALLEAVE);
        specialLeaveWaiting.setStartDate(new DateMidnight(2014, 10, 15));
        specialLeaveWaiting.setEndDate(new DateMidnight(2014, 10, 15));
        specialLeaveWaiting.setStatus(ApplicationStatus.WAITING);
        specialLeaveWaiting.setPerson(person);

        Application unpaidLeaveAllowed = new Application();
        unpaidLeaveAllowed.setVacationType(VacationType.UNPAIDLEAVE);
        unpaidLeaveAllowed.setStartDate(new DateMidnight(2014, 10, 16));
        unpaidLeaveAllowed.setEndDate(new DateMidnight(2014, 10, 16));
        unpaidLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);
        unpaidLeaveAllowed.setPerson(person);

        Application overTimeWaiting = new Application();
        overTimeWaiting.setVacationType(VacationType.OVERTIME);
        overTimeWaiting.setStartDate(new DateMidnight(2014, 11, 3));
        overTimeWaiting.setEndDate(new DateMidnight(2014, 11, 3));
        overTimeWaiting.setStatus(ApplicationStatus.WAITING);
        overTimeWaiting.setPerson(person);

        List<Application> applications = Arrays.asList(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
                holidayRejected, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person))
            .thenReturn(applications);

        // just return 1 day for each application for leave
        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.eq(person)))
            .thenReturn(BigDecimal.ONE);

        ApplicationForLeaveStatistics statistics = builder.build(person, from, to);

        // PERSON

        Assert.assertNotNull("Person should not be null", statistics.getPerson());
        Assert.assertEquals("Wrong person", person, statistics.getPerson());

        // VACATION DAYS

        Assert.assertNotNull("Waiting vacation days should not be null", statistics.getTotalWaitingVacationDays());
        Assert.assertNotNull("Allowed vacation days should not be null", statistics.getTotalAllowedVacationDays());
        Assert.assertNotNull("Left vacation days should not be null", statistics.getLeftVacationDays());

        Assert.assertEquals("Wrong number of waiting vacation days", new BigDecimal("4"),
            statistics.getTotalWaitingVacationDays());
        Assert.assertEquals("Wrong number of allowed vacation days", new BigDecimal("2"),
            statistics.getTotalAllowedVacationDays());
        Assert.assertEquals("Wrong number of left vacation days", BigDecimal.TEN, statistics.getLeftVacationDays());
    }


    @Test
    public void ensureCallsCalendarServiceToCalculatePartialVacationDaysOfVacationsSpanningTwoYears() {

        DateMidnight from = new DateMidnight(2015, 1, 1);
        DateMidnight to = new DateMidnight(2015, 12, 31);

        Person person = Mockito.mock(Person.class);
        Account account = Mockito.mock(Account.class);

        Mockito.when(person.getEmail()).thenReturn("muster@muster.de");
        Mockito.when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(Mockito.eq(account)))
            .thenReturn(BigDecimal.TEN);
        Mockito.when(overtimeService.getTotalOvertimeForPerson(person)).thenReturn(BigDecimal.TEN);
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        Application holidayAllowed = new Application();
        holidayAllowed.setVacationType(VacationType.HOLIDAY);
        holidayAllowed.setStartDate(new DateMidnight(2014, 12, 29));
        holidayAllowed.setEndDate(new DateMidnight(2015, 1, 9));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);
        holidayAllowed.setDayLength(DayLength.FULL);
        holidayAllowed.setPerson(person);

        Application holidayWaiting = new Application();
        holidayWaiting.setVacationType(VacationType.HOLIDAY);
        holidayWaiting.setStartDate(new DateMidnight(2015, 12, 21));
        holidayWaiting.setEndDate(new DateMidnight(2016, 1, 4));
        holidayWaiting.setStatus(ApplicationStatus.WAITING);
        holidayWaiting.setDayLength(DayLength.FULL);
        holidayWaiting.setPerson(person);

        List<Application> applications = Arrays.asList(holidayWaiting, holidayAllowed);

        Mockito.when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person))
            .thenReturn(applications);

        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2015, 1, 1),
                    new DateMidnight(2015, 1, 9), person))
            .thenReturn(new BigDecimal("5"));
        Mockito.when(calendarService.getWorkDays(DayLength.FULL, new DateMidnight(2015, 12, 21),
                    new DateMidnight(2015, 12, 31), person))
            .thenReturn(new BigDecimal("7"));

        ApplicationForLeaveStatistics statistics = builder.build(person, from, to);

        // VACATION DAYS

        Assert.assertNotNull("Waiting vacation days should not be null", statistics.getTotalWaitingVacationDays());
        Assert.assertNotNull("Allowed vacation days should not be null", statistics.getTotalAllowedVacationDays());
        Assert.assertNotNull("Left vacation days should not be null", statistics.getLeftVacationDays());

        Assert.assertEquals("Wrong number of waiting vacation days", new BigDecimal("7"),
            statistics.getTotalWaitingVacationDays());
        Assert.assertEquals("Wrong number of allowed vacation days", new BigDecimal("5"),
            statistics.getTotalAllowedVacationDays());
        Assert.assertEquals("Wrong number of left vacation days", BigDecimal.TEN, statistics.getLeftVacationDays());
    }


    @Test
    public void ensureCalculatesLeftVacationDaysAndLeftOvertimeCorrectly() {

        DateMidnight from = new DateMidnight(2015, 1, 1);
        DateMidnight to = new DateMidnight(2015, 12, 31);

        Person person = Mockito.mock(Person.class);
        Account account = Mockito.mock(Account.class);

        Mockito.when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        Mockito.when(applicationService.getTotalOvertimeReductionOfPerson(person)).thenReturn(new BigDecimal("3.5"));
        Mockito.when(overtimeService.getTotalOvertimeForPerson(person)).thenReturn(BigDecimal.TEN);
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(new BigDecimal("8.5"));

        ApplicationForLeaveStatistics statistics = builder.build(person, from, to);

        Assert.assertEquals("Wrong left overtime", new BigDecimal("6.5"), statistics.getLeftOvertime());
        Assert.assertEquals("Wrong left vacation days", new BigDecimal("8.5"), statistics.getLeftVacationDays());

        Mockito.verify(applicationService).getTotalOvertimeReductionOfPerson(person);
        Mockito.verify(overtimeService).getTotalOvertimeForPerson(person);
        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(account);
    }
}
