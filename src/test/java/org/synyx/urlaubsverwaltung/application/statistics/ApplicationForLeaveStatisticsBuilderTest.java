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
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.of;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

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

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService);
    }

    @Test
    void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {
        final VacationType type = createVacationType(HOLIDAY, "application.data.vacationType.holiday");
        assertThatIllegalArgumentException().isThrownBy(() -> sut.build(new Person(), of(2014, 1, 1), of(2015, 1, 1), List.of(type)));
    }

    @Test
    void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person();

        final LocalDate validFrom = of(2014, JANUARY, 1);
        final LocalDate validTo = of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(9));

        final Application holidayWaiting = new Application();
        holidayWaiting.setPerson(person);
        holidayWaiting.setDayLength(FULL);
        holidayWaiting.setVacationType(vacationTypes.get(0));
        holidayWaiting.setStartDate(of(2014, 10, 13));
        holidayWaiting.setEndDate(of(2014, 10, 13));
        holidayWaiting.setStatus(WAITING);

        final Application holidayTemporaryAllowed = new Application();
        holidayTemporaryAllowed.setPerson(person);
        holidayTemporaryAllowed.setDayLength(FULL);
        holidayTemporaryAllowed.setVacationType(vacationTypes.get(0));
        holidayTemporaryAllowed.setStartDate(of(2014, 10, 12));
        holidayTemporaryAllowed.setEndDate(of(2014, 10, 12));
        holidayTemporaryAllowed.setStatus(TEMPORARY_ALLOWED);

        final Application holidayAllowed = new Application();
        holidayAllowed.setPerson(person);
        holidayAllowed.setDayLength(FULL);
        holidayAllowed.setVacationType(vacationTypes.get(0));
        holidayAllowed.setStartDate(of(2014, 10, 14));
        holidayAllowed.setEndDate(of(2014, 10, 14));
        holidayAllowed.setStatus(ALLOWED);

        final Application holidayAllowedCancellationRequested = new Application();
        holidayAllowedCancellationRequested.setPerson(person);
        holidayAllowedCancellationRequested.setDayLength(FULL);
        holidayAllowedCancellationRequested.setVacationType(vacationTypes.get(0));
        holidayAllowedCancellationRequested.setStartDate(of(2014, 10, 15));
        holidayAllowedCancellationRequested.setEndDate(of(2014, 10, 15));
        holidayAllowedCancellationRequested.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Application holidayRejected = new Application();
        holidayRejected.setPerson(person);
        holidayRejected.setDayLength(FULL);
        holidayRejected.setVacationType(vacationTypes.get(0));
        holidayRejected.setStartDate(of(2014, 11, 6));
        holidayRejected.setEndDate(of(2014, 11, 6));
        holidayRejected.setStatus(REJECTED);

        final Application specialLeaveWaiting = new Application();
        specialLeaveWaiting.setPerson(person);
        specialLeaveWaiting.setDayLength(FULL);
        specialLeaveWaiting.setVacationType(vacationTypes.get(1));
        specialLeaveWaiting.setStartDate(of(2014, 10, 15));
        specialLeaveWaiting.setEndDate(of(2014, 10, 15));
        specialLeaveWaiting.setStatus(WAITING);

        final Application unpaidLeaveAllowed = new Application();
        unpaidLeaveAllowed.setPerson(person);
        unpaidLeaveAllowed.setDayLength(FULL);
        unpaidLeaveAllowed.setVacationType(vacationTypes.get(2));
        unpaidLeaveAllowed.setStartDate(of(2014, 10, 16));
        unpaidLeaveAllowed.setEndDate(of(2014, 10, 16));
        unpaidLeaveAllowed.setStatus(ALLOWED);

        final Application overTimeWaiting = new Application();
        overTimeWaiting.setPerson(person);
        overTimeWaiting.setDayLength(FULL);
        overTimeWaiting.setVacationType(vacationTypes.get(3));
        overTimeWaiting.setStartDate(of(2014, 11, 3));
        overTimeWaiting.setEndDate(of(2014, 11, 3));
        overTimeWaiting.setStatus(WAITING);

        final List<Application> applications = List.of(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
            holidayAllowedCancellationRequested, holidayRejected, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);
        final LocalDate from = of(2014, 1, 1);
        final LocalDate to = of(2014, 12, 31);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person)).thenReturn(applications);

        // just return 1 day for each application for leave
        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), eq(person)))
            .thenReturn(ONE);

        final VacationType type = createVacationType(HOLIDAY, "application.data.vacationType.holiday");

        final ApplicationForLeaveStatistics statistics = sut.build(person, from, to, List.of(type));
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(new BigDecimal("4"));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(new BigDecimal("3"));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(TEN);
    }

    @Test
    void ensureCallsCalendarServiceToCalculatePartialVacationDaysOfVacationsSpanningOverTheGivenPeriod() {

        final List<VacationType> vacationTypes = createVacationTypes();

        final Person person = new Person();
        final LocalDate validFrom = of(2021, 1, 1);
        final LocalDate validTo = of(2021, 12, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2021, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(TEN);
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(9));

        final Application applicationSpanningIntoPeriod = new Application();
        applicationSpanningIntoPeriod.setPerson(person);
        applicationSpanningIntoPeriod.setStatus(ALLOWED);
        applicationSpanningIntoPeriod.setDayLength(FULL);
        applicationSpanningIntoPeriod.setVacationType(vacationTypes.get(0));
        applicationSpanningIntoPeriod.setStartDate(of(2021, 4, 25));
        applicationSpanningIntoPeriod.setEndDate(of(2021, 4, 30));
        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 4, 28), of(2021, 4, 30), person))
            .thenReturn(BigDecimal.valueOf(3));

        final Application applicationSpanningOutOfPeriod = new Application();
        applicationSpanningOutOfPeriod.setPerson(person);
        applicationSpanningOutOfPeriod.setStatus(WAITING);
        applicationSpanningOutOfPeriod.setDayLength(FULL);
        applicationSpanningOutOfPeriod.setVacationType(vacationTypes.get(0));
        applicationSpanningOutOfPeriod.setStartDate(of(2021, 5, 21));
        applicationSpanningOutOfPeriod.setEndDate(of(2021, 6, 10));
        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 5, 21), of(2021, 5, 28), person))
            .thenReturn(BigDecimal.valueOf(8));

        final Application applicationInPeriod = new Application();
        applicationInPeriod.setPerson(person);
        applicationInPeriod.setStatus(WAITING);
        applicationInPeriod.setDayLength(FULL);
        applicationInPeriod.setVacationType(vacationTypes.get(0));
        applicationInPeriod.setStartDate(of(2021, 4, 28));
        applicationInPeriod.setEndDate(of(2021, 5, 5));
        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 4, 28), of(2021, 5, 5), person))
            .thenReturn(BigDecimal.valueOf(8));

        final LocalDate periodFrom = of(2021, 4, 28);
        final LocalDate periodTo = of(2021, 5, 28);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(periodFrom, periodTo, person)).thenReturn(List.of(applicationSpanningIntoPeriod, applicationInPeriod, applicationSpanningOutOfPeriod));

        final VacationType type = createVacationType(HOLIDAY, "application.data.vacationType.holiday");

        final ApplicationForLeaveStatistics statistics = sut.build(person, periodFrom, periodTo, List.of(type));
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(16));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(TEN);
    }

    @Test
    void ensureCalculatesLeftVacationDaysAndLeftOvertimeCorrectly() {

        final LocalDate periodFrom = of(2015, 1, 1);
        final LocalDate periodTo = of(2015, 12, 31);

        final Person person = new Person();
        final LocalDate validFrom = of(2015, JANUARY, 1);
        final LocalDate validTo = of(2015, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofMinutes(390));
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(new BigDecimal("8.5"));

        final VacationType type = createVacationType(HOLIDAY, "application.data.vacationType.holiday");

        final ApplicationForLeaveStatistics statistics = sut.build(person, periodFrom, periodTo, List.of(type));
        assertThat(statistics.getLeftOvertime()).isEqualTo(Duration.ofMinutes(390));
        assertThat(statistics.getLeftVacationDays()).isEqualTo(new BigDecimal("8.5"));
    }
}
