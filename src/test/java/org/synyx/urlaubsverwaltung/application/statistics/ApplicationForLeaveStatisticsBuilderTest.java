package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsBuilderTest {

    private ApplicationForLeaveStatisticsBuilder sut;

    @Mock
    private AccountService accountService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private WorkingTimeService workingTimeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
                workingTimeService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2015-06-24T16:02:42.00Z"), ZoneOffset.UTC));
    }

    @Test
    void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {
        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.build(List.of(new Person()), of(2014, 1, 1), of(2015, 1, 1), List.of(type)));
    }

//    @Test
//    void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {
//
//        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
//            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));
//
//        final List<VacationTypeEntity> vacationTypes = createVacationTypesEntities();
//
//        final Person person = new Person();
//
//        final LocalDate validFrom = of(2014, JANUARY, 1);
//        final LocalDate validTo = of(2014, DECEMBER, 31);
//        final LocalDate expiryDate = of(2014, APRIL, 1);
//        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, null);
//
//        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));
//
//        final Application holidayWaiting = new Application();
//        holidayWaiting.setPerson(person);
//        holidayWaiting.setDayLength(FULL);
//        holidayWaiting.setVacationType(vacationTypes.get(0));
//        holidayWaiting.setStartDate(of(2014, 10, 13));
//        holidayWaiting.setEndDate(of(2014, 10, 13));
//        holidayWaiting.setStatus(WAITING);
//
//        final Application holidayTemporaryAllowed = new Application();
//        holidayTemporaryAllowed.setPerson(person);
//        holidayTemporaryAllowed.setDayLength(FULL);
//        holidayTemporaryAllowed.setVacationType(vacationTypes.get(0));
//        holidayTemporaryAllowed.setStartDate(of(2014, 10, 12));
//        holidayTemporaryAllowed.setEndDate(of(2014, 10, 12));
//        holidayTemporaryAllowed.setStatus(TEMPORARY_ALLOWED);
//
//        final Application holidayAllowed = new Application();
//        holidayAllowed.setPerson(person);
//        holidayAllowed.setDayLength(FULL);
//        holidayAllowed.setVacationType(vacationTypes.get(0));
//        holidayAllowed.setStartDate(of(2014, 10, 14));
//        holidayAllowed.setEndDate(of(2014, 10, 14));
//        holidayAllowed.setStatus(ALLOWED);
//
//        final Application holidayAllowedCancellationRequested = new Application();
//        holidayAllowedCancellationRequested.setPerson(person);
//        holidayAllowedCancellationRequested.setDayLength(FULL);
//        holidayAllowedCancellationRequested.setVacationType(vacationTypes.get(0));
//        holidayAllowedCancellationRequested.setStartDate(of(2014, 10, 15));
//        holidayAllowedCancellationRequested.setEndDate(of(2014, 10, 15));
//        holidayAllowedCancellationRequested.setStatus(ALLOWED_CANCELLATION_REQUESTED);
//
//        final Application holidayRejected = new Application();
//        holidayRejected.setPerson(person);
//        holidayRejected.setDayLength(FULL);
//        holidayRejected.setVacationType(vacationTypes.get(0));
//        holidayRejected.setStartDate(of(2014, 11, 6));
//        holidayRejected.setEndDate(of(2014, 11, 6));
//        holidayRejected.setStatus(REJECTED);
//
//        final Application specialLeaveWaiting = new Application();
//        specialLeaveWaiting.setPerson(person);
//        specialLeaveWaiting.setDayLength(FULL);
//        specialLeaveWaiting.setVacationType(vacationTypes.get(1));
//        specialLeaveWaiting.setStartDate(of(2014, 10, 15));
//        specialLeaveWaiting.setEndDate(of(2014, 10, 15));
//        specialLeaveWaiting.setStatus(WAITING);
//
//        final Application unpaidLeaveAllowed = new Application();
//        unpaidLeaveAllowed.setPerson(person);
//        unpaidLeaveAllowed.setDayLength(FULL);
//        unpaidLeaveAllowed.setVacationType(vacationTypes.get(2));
//        unpaidLeaveAllowed.setStartDate(of(2014, 10, 16));
//        unpaidLeaveAllowed.setEndDate(of(2014, 10, 16));
//        unpaidLeaveAllowed.setStatus(ALLOWED);
//
//        final Application overTimeWaiting = new Application();
//        overTimeWaiting.setPerson(person);
//        overTimeWaiting.setDayLength(FULL);
//        overTimeWaiting.setVacationType(vacationTypes.get(3));
//        overTimeWaiting.setStartDate(of(2014, 11, 3));
//        overTimeWaiting.setEndDate(of(2014, 11, 3));
//        overTimeWaiting.setStatus(WAITING);
//
//        final List<Application> applications = List.of(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
//            holidayAllowedCancellationRequested, holidayRejected, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);
//        final LocalDate from = of(2014, 1, 1);
//        final LocalDate to = of(2014, 12, 31);
//        when(applicationService.getApplicationsForACertainPeriod(from, to, List.of(person))).thenReturn(applications);
//
//        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
//            .thenReturn(Map.of(person, new LeftOvertime(new LeftOvertimeOverall(Duration.ofHours(9)), new LeftOvertimeDateRange(new DateRange(from, to), Duration.ZERO))));
//
//        // just return 1 day for each application for leave
//        when(workDaysCountService.getWorkDaysCount(any(DayLength.class), any(LocalDate.class), any(LocalDate.class), eq(person)))
//            .thenReturn(ONE);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(ZERO)
//            .notExpiring(ZERO)
//            .forUsedVacationDaysBeforeExpiry(ZERO)
//            .forUsedVacationDaysAfterExpiry(ZERO)
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account)).thenReturn(vacationDaysLeftYear);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        assertThat(statistics.getPerson()).isEqualTo(person);
//        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(4));
//        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(3));
//        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(TEN);
//    }

//    @Test
//    void ensureCallsWorkDaysCountServiceToCalculatePartialVacationDaysOfVacationsSpanningOverTheGivenPeriod() {
//
//        final List<VacationTypeEntity> vacationTypes = createVacationTypesEntities();
//
//        final Person person = new Person();
//        final LocalDate validFrom = of(2021, 1, 1);
//        final LocalDate validTo = of(2021, 12, 31);
//        final LocalDate expiryDate = of(2021, APRIL, 1);
//        // 20 vacation days (10 anual, 10 remaining, not exp. vacation days)
//        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, null);
//        when(accountService.getHolidaysAccount(2021, List.of(person))).thenReturn(List.of(account));
//
//        // application (25.04.2021-30.04.2021) with 3 work days
//        final Application applicationSpanningIntoPeriod = new Application();
//        applicationSpanningIntoPeriod.setPerson(person);
//        applicationSpanningIntoPeriod.setStatus(ALLOWED);
//        applicationSpanningIntoPeriod.setDayLength(FULL);
//        applicationSpanningIntoPeriod.setVacationType(vacationTypes.get(0));
//        applicationSpanningIntoPeriod.setStartDate(of(2021, 4, 25));
//        applicationSpanningIntoPeriod.setEndDate(of(2021, 4, 30));
//        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 4, 28), of(2021, 4, 30), person))
//            .thenReturn(BigDecimal.valueOf(3));
//
//        // application (21.05.2021-10.06.2021) with 8 work days
//        final Application applicationSpanningOutOfPeriod = new Application();
//        applicationSpanningOutOfPeriod.setPerson(person);
//        applicationSpanningOutOfPeriod.setStatus(WAITING);
//        applicationSpanningOutOfPeriod.setDayLength(FULL);
//        applicationSpanningOutOfPeriod.setVacationType(vacationTypes.get(0));
//        applicationSpanningOutOfPeriod.setStartDate(of(2021, 5, 21));
//        applicationSpanningOutOfPeriod.setEndDate(of(2021, 6, 10));
//        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 5, 21), of(2021, 5, 28), person))
//            .thenReturn(BigDecimal.valueOf(8));
//
//        // application (28.04.2021-05.05.2021) with 8 work days
//        final Application applicationInPeriod = new Application();
//        applicationInPeriod.setPerson(person);
//        applicationInPeriod.setStatus(WAITING);
//        applicationInPeriod.setDayLength(FULL);
//        applicationInPeriod.setVacationType(vacationTypes.get(0));
//        applicationInPeriod.setStartDate(of(2021, 4, 28));
//        applicationInPeriod.setEndDate(of(2021, 5, 5));
//        when(workDaysCountService.getWorkDaysCount(FULL, of(2021, 4, 28), of(2021, 5, 5), person))
//            .thenReturn(BigDecimal.valueOf(8));
//
//        final LocalDate periodFrom = of(2021, 4, 28);
//        final LocalDate periodTo = of(2021, 5, 28);
//        final List<Application> applications = List.of(applicationSpanningIntoPeriod, applicationInPeriod, applicationSpanningOutOfPeriod);
//        when(applicationService.getApplicationsForACertainPeriod(periodFrom, periodTo, List.of(person))).thenReturn(applications);
//
//        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, periodFrom, periodTo))
//            .thenReturn(Map.of(person, new LeftOvertime(new LeftOvertimeOverall(Duration.ofHours(9)), new LeftOvertimeDateRange(new DateRange(periodFrom, periodTo), Duration.ZERO))));
//
//        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(TEN)
//            .notExpiring(TEN)
//            .forUsedVacationDaysBeforeExpiry(ZERO)
//            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(19))
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account)).thenReturn(vacationDaysLeftYear);
//        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account)).thenReturn(vacationDaysLeftYear);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), periodFrom, periodTo, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        assertThat(statistics.getTotalWaitingVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(16));
//        assertThat(statistics.getTotalAllowedVacationDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
//        assertThat(statistics.getLeftVacationDaysForYear()).isEqualByComparingTo(ONE);
//    }

//    @Test
//    void ensureCalculatesLeftVacationDaysAndLeftOvertimeCorrectly() {
//
//        final LocalDate periodFrom = of(2015, 1, 1);
//        final LocalDate periodTo = of(2015, 12, 31);
//
//        final Person person = new Person();
//        final LocalDate validFrom = of(2015, JANUARY, 1);
//        final LocalDate validTo = of(2015, DECEMBER, 31);
//        final LocalDate expiryDate = of(2015, APRIL, 1);
//        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, null);
//
//        when(accountService.getHolidaysAccount(2015, List.of(person))).thenReturn(List.of(account));
//
//        final Application application = new Application();
//        application.setId(1);
//        application.setPerson(person);
//
//        final List<Person> persons = List.of(person);
//        final List<Application> applications = List.of(application);
//
//        when(applicationService.getApplicationsForACertainPeriod(periodFrom, periodTo, persons)).thenReturn(applications);
//
//        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, periodFrom, periodTo))
//            .thenReturn(Map.of(person, new LeftOvertime(new LeftOvertimeOverall(Duration.ofMinutes(390)), new LeftOvertimeDateRange(new DateRange(periodFrom, periodTo), Duration.ZERO))));
//
//        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(ZERO)
//            .notExpiring(ZERO)
//            .forUsedVacationDaysBeforeExpiry(ZERO)
//            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(1.5))
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(validFrom, validTo, account)).thenReturn(vacationDaysLeftYear);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), periodFrom, periodTo, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        assertThat(statistics.getLeftOvertimeForYear()).isEqualTo(Duration.ofMinutes(390));
//        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(8.5));
//    }

//    @Test
//    void ensureCalculatesLeftPeriodVacationDaysCorrectly() {
//
//        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
//            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2015-12-31T00:00:00.00Z"), ZoneOffset.UTC));
//
//        final LocalDate periodFrom = of(2015, 1, 1);
//        final LocalDate periodTo = of(2015, 12, 31);
//
//        final Person person = new Person();
//        final LocalDate validFrom = of(2015, 1, 1);
//        final LocalDate validTo = of(2015, 12, 31);
//        final LocalDate expiryDate = of(2015, APRIL, 1);
//        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, null);
//
//        when(accountService.getHolidaysAccount(2015, List.of(person))).thenReturn(List.of(account));
//
//        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(ZERO)
//            .notExpiring(ZERO)
//            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(1.5))
//            .forUsedVacationDaysAfterExpiry(ZERO)
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account)).thenReturn(vacationDaysLeftYear);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), periodFrom, periodTo, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        assertThat(statistics.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(8.5));
//    }

//    @Test
//    void ensureCorrectLeftRemainingVacationDaysAndLeftRemainingVacationDaysPeriod() {
//
//        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
//            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2022-12-31T16:02:42.00Z"), ZoneOffset.UTC));
//
//        final Person person = new Person();
//        final LocalDate firstDayOfYear = of(2022, 1, 1);
//        final LocalDate lastDayOfYear = of(2022, 12, 31);
//        final LocalDate expiryDate = of(2022, APRIL, 1);
//        final Account account = new Account(person, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, null);
//
//        when(accountService.getHolidaysAccount(2022, List.of(person))).thenReturn(List.of(account));
//
//        final LocalDate periodFrom = of(2022, 7, 1);
//        final LocalDate periodTo = of(2022, 12, 31);
//
//        final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(TEN)
//            .notExpiring(BigDecimal.valueOf(5))
//            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(6))
//            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(3))
//            .build();
//        final VacationDaysLeft vacationDaysLeftPeriod = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(TEN)
//            .notExpiring(BigDecimal.valueOf(5))
//            .forUsedVacationDaysBeforeExpiry(ZERO)
//            .forUsedVacationDaysAfterExpiry(ONE)
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(firstDayOfYear, lastDayOfYear, account)).thenReturn(vacationDaysLeftYear);
//        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account)).thenReturn(vacationDaysLeftPeriod);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), periodFrom, periodTo, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        // for the hole year:
//        // 10 remaining - 1 used day before april = 4
//        // 4 remaining, not expiring - 3 used days after april = 1
//        assertThat(statistics.getLeftRemainingVacationDaysForYear()).isEqualTo(ONE);
//        // for the period (01.07.-31.12.2022):
//        // 10 remaining - 0 used day before april = 10
//        // 5 remaining, not expiring - 1 used days after april = 4
//        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(4));
//    }

//    @Test
//    void ensureCorrectLeftRemainingVacationDaysPeriodbeforeExpiryDate() {
//
//        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService, workDaysCountService,
//            vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2022-12-31T16:02:42.00Z"), ZoneOffset.UTC));
//
//        final Person person = new Person();
//        final LocalDate firstDayOfYear = of(2022, 1, 1);
//        final LocalDate lastDayOfYear = of(2022, 12, 31);
//        final LocalDate expiryDate = of(2022, APRIL, 1);
//        final Account account = new Account(person, firstDayOfYear, lastDayOfYear, true, expiryDate, TEN, TEN, TEN, null);
//
//        when(accountService.getHolidaysAccount(2022, List.of(person))).thenReturn(List.of(account));
//
//        final LocalDate periodFrom = of(2022, 1, 1);
//        final LocalDate periodTo = of(2022, 3, 31);
//
//        final VacationDaysLeft vacationDaysLeftPeriod = VacationDaysLeft.builder()
//            .withAnnualVacation(TEN)
//            .withRemainingVacation(TEN)
//            .notExpiring(BigDecimal.valueOf(5))
//            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(4))
//            .forUsedVacationDaysAfterExpiry(ZERO)
//            .build();
//        when(vacationDaysService.getVacationDaysLeft(of(2022, 1, 1), of(2022, 12, 31), account)).thenReturn(VacationDaysLeft.builder().build());
//        when(vacationDaysService.getVacationDaysLeft(periodFrom, periodTo, account)).thenReturn(vacationDaysLeftPeriod);
//
//        final VacationType type = new VacationType(1, true, HOLIDAY, "application.data.vacationType.holiday", true, YELLOW, false);
//
//        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), periodFrom, periodTo, List.of(type));
//        assertThat(actual).hasSize(1);
//        assertThat(actual.containsKey(person)).isTrue();
//
//        final ApplicationForLeaveStatistics statistics = actual.get(person);
//        // for the period (01.01.-31.03.2022):
//        // 10 remaining - 4 used day before april = 6
//        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(6));
//    }
}
