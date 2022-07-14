package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypesEntities;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
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
            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2015-06-24T16:02:42.00Z"), ZoneOffset.UTC));
    }

    @Test
    void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {
        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
        assertThatIllegalArgumentException().isThrownBy(() -> sut.build(new Person(), null, of(2014, 1, 1), of(2015, 1, 1), List.of(type)));
    }

    @Test
    void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationTypeEntity> vacationTypes = createVacationTypesEntities();

        final Person person = new Person();

        final LocalDate validFrom = of(2014, JANUARY, 1);
        final LocalDate validTo = of(2014, DECEMBER, 31);
        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
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

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account, empty())).thenReturn(vacationDaysLeftYear);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, from, to, List.of(type));
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(4));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(TEN);
    }

    @Test
    void ensureCallsWorkDaysCountServiceToCalculatePartialVacationDaysOfVacationsSpanningOverTheGivenPeriod() {

        final List<VacationTypeEntity> vacationTypes = createVacationTypesEntities();

        final Person person = new Person();
        final LocalDate validFrom = of(2021, 1, 1);
        final LocalDate validTo = of(2021, 12, 31);
        final LocalDate expiryDate = of(2021, APRIL, 1);
        // 20 vacation days (10 anual, 10 remaining, not exp. vacation days)
        final Account account = new Account(person, validFrom, validTo, expiryDate, TEN, TEN, TEN, null);
        when(accountService.getHolidaysAccount(2021, person)).thenReturn(Optional.of(account));

        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofHours(9));

        // application (25.04.2021-30.04.2021) with 3 work days
        final Application applicationSpanningIntoPeriod = new Application();
        applicationSpanningIntoPeriod.setPerson(person);
        applicationSpanningIntoPeriod.setStatus(ALLOWED);
        applicationSpanningIntoPeriod.setDayLength(FULL);
        applicationSpanningIntoPeriod.setVacationType(vacationTypes.get(0));
        applicationSpanningIntoPeriod.setStartDate(of(2021, 4, 25));
        applicationSpanningIntoPeriod.setEndDate(of(2021, 4, 30));
        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 4, 28), of(2021, 4, 30), person))
            .thenReturn(BigDecimal.valueOf(3));

        // application (21.05.2021-10.06.2021) with 8 work days
        final Application applicationSpanningOutOfPeriod = new Application();
        applicationSpanningOutOfPeriod.setPerson(person);
        applicationSpanningOutOfPeriod.setStatus(WAITING);
        applicationSpanningOutOfPeriod.setDayLength(FULL);
        applicationSpanningOutOfPeriod.setVacationType(vacationTypes.get(0));
        applicationSpanningOutOfPeriod.setStartDate(of(2021, 5, 21));
        applicationSpanningOutOfPeriod.setEndDate(of(2021, 6, 10));
        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 5, 21), of(2021, 5, 28), person))
            .thenReturn(BigDecimal.valueOf(8));

        // application (28.04.2021-05.05.2021) with 8 work days
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

        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(TEN)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(19))
            .build();
        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account, empty())).thenReturn(vacationDaysLeftYear);
        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account, empty())).thenReturn(vacationDaysLeftYear);

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, periodFrom, periodTo, List.of(type));
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualByComparingTo(ONE);
    }

    @Test
    void ensureCalculatesLeftVacationDaysAndLeftOvertimeCorrectly() {

        final LocalDate periodFrom = of(2015, 1, 1);
        final LocalDate periodTo = of(2015, 12, 31);

        final Person person = new Person();
        final LocalDate validFrom = of(2015, JANUARY, 1);
        final LocalDate validTo = of(2015, DECEMBER, 31);
        final LocalDate expiryDate = of(2015, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));
        when(overtimeService.getLeftOvertimeForPerson(person)).thenReturn(Duration.ofMinutes(390));

        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(1.5))
            .build();
        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account, empty())).thenReturn(vacationDaysLeftYear);

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, periodFrom, periodTo, List.of(type));
        assertThat(statistics.getLeftOvertimeForYear()).isEqualTo(Duration.ofMinutes(390));
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(8.5));
    }

    @Test
    void ensureCalculatesLeftPeriodVacationDaysCorrectly() {

        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2015-12-31T00:00:00.00Z"), ZoneOffset.UTC));

        final LocalDate periodFrom = of(2015, 1, 1);
        final LocalDate periodTo = of(2015, 12, 31);

        final Person person = new Person();
        final LocalDate validFrom = of(2015, 1, 1);
        final LocalDate validTo = of(2015, 12, 31);
        final LocalDate expiryDate = of(2015, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.of(account));

        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(1.5))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account, empty())).thenReturn(vacationDaysLeftYear);

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, periodFrom, periodTo, List.of(type));
        assertThat(statistics.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(8.5));
    }

    @Test
    void ensureCorrectLeftRemainingVacationDaysAndLeftRemainingVacationDaysPeriod() {

        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2022-12-31T16:02:42.00Z"), ZoneOffset.UTC));

        final Person person = new Person();
        final LocalDate firstDayOfYear = of(2022, 1, 1);
        final LocalDate lastDayOfYear = of(2022, 12, 31);
        final LocalDate expiryDate = of(2022, APRIL, 1);
        final Account account = new Account(person, firstDayOfYear, lastDayOfYear, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));

        final LocalDate periodFrom = of(2022, 7, 1);
        final LocalDate periodTo = of(2022, 12, 31);

        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(BigDecimal.valueOf(5))
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(6))
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(3))
            .build();
        final VacationDaysLeft vacationDaysLeftPeriod = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(BigDecimal.valueOf(5))
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ONE)
            .build();
        when(vacationDaysService.getVacationDaysLeft(firstDayOfYear, lastDayOfYear, account, empty())).thenReturn(vacationDaysLeftYear);
        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account, empty())).thenReturn(vacationDaysLeftPeriod);

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, periodFrom, periodTo, List.of(type));
        // for the hole year:
        // 10 remaining - 1 used day before april = 4
        // 4 remaining, not expiring - 3 used days after april = 1
        assertThat(statistics.getLeftRemainingVacationDaysForYear()).isEqualTo(ONE);
        // for the period (01.07.-31.12.2022):
        // 10 remaining - 0 used day before april = 10
        // 5 remaining, not expiring - 1 used days after april = 4
        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(4));
    }

    @Test
    void ensureCorrectLeftRemainingVacationDaysPeriodbeforeExpiryDate() {

        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2022-12-31T16:02:42.00Z"), ZoneOffset.UTC));

        final Person person = new Person();
        final LocalDate firstDayOfYear = of(2022, 1, 1);
        final LocalDate lastDayOfYear = of(2022, 12, 31);
        final LocalDate expiryDate = of(2022, APRIL, 1);
        final Account account = new Account(person, firstDayOfYear, lastDayOfYear, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account));

        final LocalDate periodFrom = of(2022, 1, 1);
        final LocalDate periodTo = of(2022, 3, 31);

        final VacationDaysLeft vacationDaysLeftPeriod = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(BigDecimal.valueOf(5))
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(4))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(of(2022, 1, 1), of(2022, 12, 31), account, empty())).thenReturn(VacationDaysLeft.builder().build());
        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account, empty())).thenReturn(vacationDaysLeftPeriod);

        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        final ApplicationForLeaveStatistics statistics = sut.build(person, null, periodFrom, periodTo, List.of(type));
        // for the period (01.01.-31.03.2022):
        // 10 remaining - 4 used day before april = 6
        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(6));
    }
}
