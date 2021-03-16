package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.anyFullDayApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsBuilderTest {

    private ApplicationForLeaveStatisticsBuilder sut;

    @Mock
    private AccountService accountService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService, vacationTypeService);
    }

    @Test
    void ensureThrowsIfTheGivenPersonIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.build(null, LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 31)));
    }

    @Test
    void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {
        assertThatIllegalArgumentException().isThrownBy(() -> sut.build(mock(Person.class), LocalDate.of(2014, 1, 1), LocalDate.of(2015, 1, 1)));
    }

    @Test
    void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        final LocalDate from = LocalDate.of(2014, 1, 1);
        final LocalDate to = LocalDate.of(2014, 12, 31);

        final Person person = new Person();

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(eq(account))).thenReturn(TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(9));

        final Application holidayWaiting = anyFullDayApplication(person);
        holidayWaiting.setVacationType(vacationTypes.get(0));
        holidayWaiting.setStartDate(LocalDate.of(2014, 10, 13));
        holidayWaiting.setEndDate(LocalDate.of(2014, 10, 13));
        holidayWaiting.setStatus(WAITING);

        final Application holidayTemporaryAllowed = anyFullDayApplication(person);
        holidayTemporaryAllowed.setVacationType(vacationTypes.get(0));
        holidayTemporaryAllowed.setStartDate(LocalDate.of(2014, 10, 12));
        holidayTemporaryAllowed.setEndDate(LocalDate.of(2014, 10, 12));
        holidayTemporaryAllowed.setStatus(TEMPORARY_ALLOWED);

        final Application holidayAllowed = anyFullDayApplication(person);
        holidayAllowed.setVacationType(vacationTypes.get(0));
        holidayAllowed.setStartDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setEndDate(LocalDate.of(2014, 10, 14));
        holidayAllowed.setStatus(ALLOWED);

        final Application holidayAllowedCancellationRequested = anyFullDayApplication(person);
        holidayAllowedCancellationRequested.setVacationType(vacationTypes.get(0));
        holidayAllowedCancellationRequested.setStartDate(LocalDate.of(2014, 10, 15));
        holidayAllowedCancellationRequested.setEndDate(LocalDate.of(2014, 10, 15));
        holidayAllowedCancellationRequested.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Application holidayRejected = anyFullDayApplication(person);
        holidayRejected.setVacationType(vacationTypes.get(0));
        holidayRejected.setStartDate(LocalDate.of(2014, 11, 6));
        holidayRejected.setEndDate(LocalDate.of(2014, 11, 6));
        holidayRejected.setStatus(REJECTED);

        final Application specialLeaveWaiting = anyFullDayApplication(person);
        specialLeaveWaiting.setVacationType(vacationTypes.get(1));
        specialLeaveWaiting.setStartDate(LocalDate.of(2014, 10, 15));
        specialLeaveWaiting.setEndDate(LocalDate.of(2014, 10, 15));
        specialLeaveWaiting.setStatus(WAITING);

        final Application unpaidLeaveAllowed = anyFullDayApplication(person);
        unpaidLeaveAllowed.setVacationType(vacationTypes.get(2));
        unpaidLeaveAllowed.setStartDate(LocalDate.of(2014, 10, 16));
        unpaidLeaveAllowed.setEndDate(LocalDate.of(2014, 10, 16));
        unpaidLeaveAllowed.setStatus(ALLOWED);

        final Application overTimeWaiting = anyFullDayApplication(person);
        overTimeWaiting.setVacationType(vacationTypes.get(3));
        overTimeWaiting.setStartDate(LocalDate.of(2014, 11, 3));
        overTimeWaiting.setEndDate(LocalDate.of(2014, 11, 3));
        overTimeWaiting.setStatus(WAITING);

        List<Application> applications = List.of(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
            holidayAllowedCancellationRequested, holidayRejected, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person)).thenReturn(applications);

        // just return 1 day for each application for leave
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), eq(person)))
            .thenReturn(ONE);

        final ApplicationForLeaveStatistics statistics = sut.build(person, from, to);

        // PERSON
        assertThat(statistics.getPerson()).isEqualTo(person);

        // VACATION DAYS
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(new BigDecimal("4"));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(new BigDecimal("3"));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(TEN);
    }

    @Test
    void ensureCallsCalendarServiceToCalculatePartialVacationDaysOfVacationsSpanningTwoYears() {

        final List<VacationType> vacationTypes = createVacationTypes();
        when(vacationTypeService.getVacationTypes()).thenReturn(vacationTypes);

        LocalDate from = LocalDate.of(2015, 1, 1);
        LocalDate to = LocalDate.of(2015, 12, 31);

        Person person = new Person();
        final LocalDate validFrom = LocalDate.of(2015, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2015, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(eq(account)))
            .thenReturn(TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(9));

        Application holidayAllowed = anyFullDayApplication(person);
        holidayAllowed.setVacationType(vacationTypes.get(0));
        holidayAllowed.setStartDate(LocalDate.of(2014, 12, 29));
        holidayAllowed.setEndDate(LocalDate.of(2015, 1, 9));
        holidayAllowed.setStatus(ALLOWED);

        Application holidayWaiting = anyFullDayApplication(person);
        holidayWaiting.setVacationType(vacationTypes.get(0));
        holidayWaiting.setStartDate(LocalDate.of(2015, 12, 21));
        holidayWaiting.setEndDate(LocalDate.of(2016, 1, 4));
        holidayWaiting.setStatus(WAITING);

        List<Application> applications = Arrays.asList(holidayWaiting, holidayAllowed);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person))
            .thenReturn(applications);

        when(workDaysCountService.getWorkDaysCount(DayLength.FULL, LocalDate.of(2015, 1, 1),
            LocalDate.of(2015, 1, 9), person))
            .thenReturn(new BigDecimal("5"));
        when(workDaysCountService.getWorkDaysCount(DayLength.FULL, LocalDate.of(2015, 12, 21),
            LocalDate.of(2015, 12, 31), person))
            .thenReturn(new BigDecimal("7"));

        ApplicationForLeaveStatistics statistics = sut.build(person, from, to);

        // VACATION DAYS
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(new BigDecimal("7"));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(new BigDecimal("5"));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(TEN);
    }

    @Test
    void ensureCalculatesLeftVacationDaysAndLeftOvertimeCorrectly() {

        LocalDate from = LocalDate.of(2015, 1, 1);
        LocalDate to = LocalDate.of(2015, 12, 31);

        Person person = mock(Person.class);
        Account account = mock(Account.class);

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofMinutes(390));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(new BigDecimal("8.5"));

        ApplicationForLeaveStatistics statistics = sut.build(person, from, to);

        // VACATION DAYS
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ofMinutes(390));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(new BigDecimal("8.5"));

        verify(overtimeService).getLeftOvertimeForPerson(person);
        verify(vacationDaysService).calculateTotalLeftVacationDays(account);
    }
}
