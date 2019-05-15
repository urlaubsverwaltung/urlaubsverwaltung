package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.statistics.web.ApplicationForLeaveStatisticsBuilder}.
 */
public class ApplicationForLeaveStatisticsBuilderTest {

    private AccountService accountService;
    private ApplicationService applicationService;
    private WorkDaysService calendarService;
    private VacationDaysService vacationDaysService;
    private OvertimeService overtimeService;
    private VacationTypeService vacationTypeService;

    private ApplicationForLeaveStatisticsBuilder builder;
    private List<VacationType> vacationTypes;

    @Before
    public void setUp() {

        accountService = mock(AccountService.class);
        applicationService = mock(ApplicationService.class);
        calendarService = mock(WorkDaysService.class);
        vacationDaysService = mock(VacationDaysService.class);
        overtimeService = mock(OvertimeService.class);
        vacationTypeService = mock(VacationTypeService.class);

        vacationTypes = TestDataCreator.createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        builder = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, calendarService,
                vacationDaysService, overtimeService, vacationTypeService);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenPersonIsNull() {

        builder.build(null, LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 31));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenFromDateIsNull() {

        builder.build(mock(Person.class), null, LocalDate.of(2015, 12, 31));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenToDateIsNull() {

        builder.build(mock(Person.class), LocalDate.of(2014, 1, 1), null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {

        builder.build(mock(Person.class), LocalDate.of(2014, 1, 1), LocalDate.of(2015, 1, 1));
    }


    @Test
    public void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        LocalDate from = LocalDate.of(2014, 1, 1);
        LocalDate to = LocalDate.of(2014, 12, 31);

        Person person = mock(Person.class);
        Account account = mock(Account.class);

        when(person.getEmail()).thenReturn("muster@firma.test");
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(eq(account)))
            .thenReturn(BigDecimal.TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(new BigDecimal("9"));

        Application holidayWaiting = TestDataCreator.anyFullDayApplication(person);
        holidayWaiting.setVacationType(vacationTypes.get(0));
        holidayWaiting.setStartDate(LocalDate.of(2014, 10, 13));
        holidayWaiting.setEndDate(LocalDate.of(2014, 10, 13));
        holidayWaiting.setStatus(ApplicationStatus.WAITING);

        Application holidayTemporaryAllowed = TestDataCreator.anyFullDayApplication(person);
        holidayTemporaryAllowed.setVacationType(vacationTypes.get(0));
        holidayTemporaryAllowed.setStartDate(LocalDate.of(2014, 10, 12));
        holidayTemporaryAllowed.setEndDate(LocalDate.of(2014, 10, 12));
        holidayTemporaryAllowed.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);

        Application holidayAllowed = TestDataCreator.anyFullDayApplication(person);
        holidayAllowed.setVacationType(vacationTypes.get(0));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application holidayRejected = TestDataCreator.anyFullDayApplication(person);
        holidayRejected.setVacationType(vacationTypes.get(0));
        holidayRejected.setStartDate(LocalDate.of(2014, 11, 6));
        holidayRejected.setEndDate(LocalDate.of(2014, 11, 6));
        holidayRejected.setStatus(ApplicationStatus.REJECTED);

        Application specialLeaveWaiting = TestDataCreator.anyFullDayApplication(person);
        specialLeaveWaiting.setVacationType(vacationTypes.get(1));
        specialLeaveWaiting.setStartDate(LocalDate.of(2014, 10, 15));
        specialLeaveWaiting.setEndDate(LocalDate.of(2014, 10, 15));
        specialLeaveWaiting.setStatus(ApplicationStatus.WAITING);

        Application unpaidLeaveAllowed = TestDataCreator.anyFullDayApplication(person);
        unpaidLeaveAllowed.setVacationType(vacationTypes.get(2));
        unpaidLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 16));
        unpaidLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 16));
        unpaidLeaveAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application overTimeWaiting = TestDataCreator.anyFullDayApplication(person);
        overTimeWaiting.setVacationType(vacationTypes.get(3));
        overTimeWaiting.setStartDate(LocalDate.of(2014, 11, 3));
        overTimeWaiting.setEndDate(LocalDate.of(2014, 11, 3));
        overTimeWaiting.setStatus(ApplicationStatus.WAITING);

        List<Application> applications = Arrays.asList(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
                holidayRejected, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person))
            .thenReturn(applications);

        // just return 1 day for each application for leave
        when(calendarService.getWorkDays(any(DayLength.class), any(LocalDate.class),
                    any(LocalDate.class), eq(person)))
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

        LocalDate from = LocalDate.of(2015, 1, 1);
        LocalDate to = LocalDate.of(2015, 12, 31);

        Person person = mock(Person.class);
        Account account = mock(Account.class);

        when(person.getEmail()).thenReturn("muster@firma.test");
        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(eq(account)))
            .thenReturn(BigDecimal.TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(new BigDecimal("9"));

        Application holidayAllowed = TestDataCreator.anyFullDayApplication(person);
        holidayAllowed.setVacationType(vacationTypes.get(0));
        holidayAllowed.setStartDate(LocalDate.of(2014, 12, 29));
        holidayAllowed.setEndDate(LocalDate.of(2015, 1, 9));
        holidayAllowed.setStatus(ApplicationStatus.ALLOWED);

        Application holidayWaiting = TestDataCreator.anyFullDayApplication(person);
        holidayWaiting.setVacationType(vacationTypes.get(0));
        holidayWaiting.setStartDate(LocalDate.of(2015, 12, 21));
        holidayWaiting.setEndDate(LocalDate.of(2016, 1, 4));
        holidayWaiting.setStatus(ApplicationStatus.WAITING);

        List<Application> applications = Arrays.asList(holidayWaiting, holidayAllowed);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person))
            .thenReturn(applications);

        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2015, 1, 1),
                    LocalDate.of(2015, 1, 9), person))
            .thenReturn(new BigDecimal("5"));
        when(calendarService.getWorkDays(DayLength.FULL, LocalDate.of(2015, 12, 21),
                    LocalDate.of(2015, 12, 31), person))
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

        LocalDate from = LocalDate.of(2015, 1, 1);
        LocalDate to = LocalDate.of(2015, 12, 31);

        Person person = mock(Person.class);
        Account account = mock(Account.class);

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(new BigDecimal("6.5"));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(new BigDecimal("8.5"));

        ApplicationForLeaveStatistics statistics = builder.build(person, from, to);

        Assert.assertEquals("Wrong left overtime", new BigDecimal("6.5"), statistics.getLeftOvertime());
        Assert.assertEquals("Wrong left vacation days", new BigDecimal("8.5"), statistics.getLeftVacationDays());

        verify(overtimeService).getLeftOvertimeForPerson(person);
        verify(vacationDaysService).calculateTotalLeftVacationDays(account);
    }
}
