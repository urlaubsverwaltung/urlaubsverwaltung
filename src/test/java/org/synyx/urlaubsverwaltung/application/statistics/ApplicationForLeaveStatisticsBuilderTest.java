package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.overtime.LeftOvertime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

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
    private WorkingTimeCalendarService workingTimeCalendarService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2015-06-24T16:02:42.00Z"), ZoneOffset.UTC));
    }

    @Test
    void ensureThrowsIfTheGivenFromAndToDatesAreNotInTheSameYear() {
        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sut.build(List.of(new Person()), of(2014, JANUARY, 1), of(2015, JANUARY, 1), List.of(type)));
    }

    @Test
    void ensureLeftVacationDays() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person();

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, false, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.getFirst());
        applicationForLeave.setStartDate(of(2014, OCTOBER, 13));
        applicationForLeave.setEndDate(of(2014, OCTOBER, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(10)).build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(5)).build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(10));
            assertThat(value.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(5));
        });
    }

    @Test
    void ensureLeftVacationDaysForDateRange() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person();

        final LocalDate from = of(2014, OCTOBER, 1);
        final LocalDate to = of(2014, OCTOBER, 31);
        final LocalDate firstDayOfYear = from.with(firstDayOfYear());
        final LocalDate lastDayOfYear = from.with(lastDayOfYear());
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, false, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(firstDayOfYear, lastDayOfYear);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.getFirst());
        applicationForLeave.setStartDate(of(2014, OCTOBER, 13));
        applicationForLeave.setEndDate(of(2014, OCTOBER, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(firstDayOfYear, lastDayOfYear, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(10)).build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(5)).build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(10));
            assertThat(value.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(5));
        });
    }

    @Test
    void ensureLeftRemainingVacationDaysAfterExpiryDate() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person();

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, false, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.getFirst());
        applicationForLeave.setStartDate(of(2014, OCTOBER, 13));
        applicationForLeave.setEndDate(of(2014, OCTOBER, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(5))
            .notExpiring(BigDecimal.valueOf(999))
            .build();

        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(10))
            .withRemainingVacation(BigDecimal.valueOf(2))
            .notExpiring(BigDecimal.valueOf(999))
            .build();

        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getLeftRemainingVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(5));
            assertThat(value.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(2));
        });
    }

    @Test
    void ensureLeftRemainingVacationDaysBeforeExpiryDate() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person();

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, true, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.getFirst());
        applicationForLeave.setStartDate(of(2014, OCTOBER, 13));
        applicationForLeave.setEndDate(of(2014, OCTOBER, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(5))
            .build();

        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(10))
            .withRemainingVacation(BigDecimal.valueOf(2))
            .build();

        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getLeftRemainingVacationDaysForYear()).isEqualTo(BigDecimal.ZERO);
            assertThat(value.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.ZERO);
        });
    }

    @Test
    void ensureUsesWaitingAndAllowedVacationOfAllHolidayTypesToBuildStatistics() {

        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final List<VacationType<?>> vacationTypes = createVacationTypes(new StaticMessageSource());

        final Person person = new Person();

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, false, expiryDate, TEN, TEN, TEN, null);

        when(accountService.getHolidaysAccount(2014, List.of(person))).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final Application holidayWaiting = new Application();
        holidayWaiting.setPerson(person);
        holidayWaiting.setDayLength(FULL);
        holidayWaiting.setVacationType(vacationTypes.getFirst());
        holidayWaiting.setStartDate(of(2014, OCTOBER, 13));
        holidayWaiting.setEndDate(of(2014, OCTOBER, 13));
        holidayWaiting.setStatus(WAITING);

        final Application holidayTemporaryAllowed = new Application();
        holidayTemporaryAllowed.setPerson(person);
        holidayTemporaryAllowed.setDayLength(FULL);
        holidayTemporaryAllowed.setVacationType(vacationTypes.getFirst());
        holidayTemporaryAllowed.setStartDate(of(2014, OCTOBER, 12));
        holidayTemporaryAllowed.setEndDate(of(2014, OCTOBER, 12));
        holidayTemporaryAllowed.setStatus(TEMPORARY_ALLOWED);

        final Application holidayAllowed = new Application();
        holidayAllowed.setPerson(person);
        holidayAllowed.setDayLength(FULL);
        holidayAllowed.setVacationType(vacationTypes.getFirst());
        holidayAllowed.setStartDate(of(2014, OCTOBER, 14));
        holidayAllowed.setEndDate(of(2014, OCTOBER, 14));
        holidayAllowed.setStatus(ALLOWED);

        final Application holidayAllowedCancellationRequested = new Application();
        holidayAllowedCancellationRequested.setPerson(person);
        holidayAllowedCancellationRequested.setDayLength(FULL);
        holidayAllowedCancellationRequested.setVacationType(vacationTypes.getFirst());
        holidayAllowedCancellationRequested.setStartDate(of(2014, OCTOBER, 15));
        holidayAllowedCancellationRequested.setEndDate(of(2014, OCTOBER, 15));
        holidayAllowedCancellationRequested.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Application specialLeaveWaiting = new Application();
        specialLeaveWaiting.setPerson(person);
        specialLeaveWaiting.setDayLength(FULL);
        specialLeaveWaiting.setVacationType(vacationTypes.get(1));
        specialLeaveWaiting.setStartDate(of(2014, OCTOBER, 15));
        specialLeaveWaiting.setEndDate(of(2014, OCTOBER, 15));
        specialLeaveWaiting.setStatus(WAITING);

        final Application unpaidLeaveAllowed = new Application();
        unpaidLeaveAllowed.setPerson(person);
        unpaidLeaveAllowed.setDayLength(FULL);
        unpaidLeaveAllowed.setVacationType(vacationTypes.get(2));
        unpaidLeaveAllowed.setStartDate(of(2014, OCTOBER, 16));
        unpaidLeaveAllowed.setEndDate(of(2014, OCTOBER, 16));
        unpaidLeaveAllowed.setStatus(ALLOWED);

        final Application overTimeWaiting = new Application();
        overTimeWaiting.setPerson(person);
        overTimeWaiting.setDayLength(FULL);
        overTimeWaiting.setVacationType(vacationTypes.get(3));
        overTimeWaiting.setStartDate(of(2014, NOVEMBER, 3));
        overTimeWaiting.setEndDate(of(2014, NOVEMBER, 3));
        overTimeWaiting.setStatus(WAITING);

        final List<Application> applications = List.of(holidayWaiting, holidayTemporaryAllowed, holidayAllowed,
            holidayAllowedCancellationRequested, specialLeaveWaiting, unpaidLeaveAllowed, overTimeWaiting);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(BigDecimal.ZERO)
            .notExpiring(BigDecimal.ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.ZERO)
            .build();

        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, VacationDaysLeft.builder().build());

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(4));
            assertThat(value.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(3));
            assertThat(value.getLeftVacationDaysForYear()).isEqualTo(TEN);
        });
    }

    @Test
    void ensureLeftOvertime() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final Person person = new Person();

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);

        final LocalDate expiryDate = of(2014, APRIL, 1);
        final Account account = new Account(person, from, to, false, expiryDate, TEN, TEN, TEN, null);

        final List<Person> persons = List.of(person);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(List.of(account));

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final List<Application> applications = List.of();
        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, persons, activeStatuses())).thenReturn(applications);

        final LeftOvertime personLeftOvertime = new LeftOvertime(Duration.ofHours(9), Duration.ofHours(3));
        final Map<Person, LeftOvertime> leftOvertimeByPerson = Map.of(person, personLeftOvertime);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(leftOvertimeByPerson);

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange, List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(persons, from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final Optional<ApplicationForLeaveStatistics> statistics = actual.get(person);
        assertThat(statistics).hasValueSatisfying(value -> {
            assertThat(value.getPerson()).isEqualTo(person);
            assertThat(value.getLeftOvertimeForYear()).isEqualTo(Duration.ofHours(9));
            assertThat(value.getLeftOvertimeForPeriod()).isEqualTo(Duration.ofHours(3));
        });
    }

    /**
     * A vacation type that has been deactivated is not part of the vacation types the statistics are built for, so it
     * must only show up for the persons who actually took an absence with it - and it must still be counted for them.
     */
    @Test
    void ensureDeactivatedVacationTypeIsOnlyShownForThePersonWhoUsedIt() {

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);
        final LocalDate expiryDate = of(2014, APRIL, 1);

        final Person personWithAbsence = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personWithAbsence.setId(1L);
        final Person personWithoutAbsence = new Person("other", "Other", "Otto", "other@example.org");
        personWithoutAbsence.setId(2L);
        final List<Person> persons = List.of(personWithAbsence, personWithoutAbsence);

        final Account accountWithAbsence = new Account(personWithAbsence, from, to, false, expiryDate, TEN, TEN, TEN, null);
        final Account accountWithoutAbsence = new Account(personWithoutAbsence, from, to, false, expiryDate, TEN, TEN, TEN, null);
        final List<Account> accounts = List.of(accountWithAbsence, accountWithoutAbsence);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(accounts);

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendars =
            Map.of(personWithAbsence, workingTimeCalendar, personWithoutAbsence, workingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(workingTimeCalendars);

        final VacationType<?> deactivatedVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(4711L).active(false).category(HOLIDAY).messageKey("deactivated").build();

        final Application absenceOfDeactivatedType = new Application();
        absenceOfDeactivatedType.setPerson(personWithAbsence);
        absenceOfDeactivatedType.setDayLength(FULL);
        absenceOfDeactivatedType.setVacationType(deactivatedVacationType);
        absenceOfDeactivatedType.setStartDate(of(2014, 10, 13));
        absenceOfDeactivatedType.setEndDate(of(2014, 10, 13));
        absenceOfDeactivatedType.setStatus(ALLOWED);

        final List<Application> applications = List.of(absenceOfDeactivatedType);
        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, persons, activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(Map.of());

        final HolidayAccountVacationDays vacationDaysWithAbsence = new HolidayAccountVacationDays(
            accountWithAbsence, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        final HolidayAccountVacationDays vacationDaysWithoutAbsence = new HolidayAccountVacationDays(
            accountWithoutAbsence, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        when(vacationDaysService.getVacationDaysLeft(accounts, dateRange, List.of(), workingTimeCalendars))
            .thenReturn(Map.of(accountWithAbsence, vacationDaysWithAbsence, accountWithoutAbsence, vacationDaysWithoutAbsence));

        // the deactivated type is NOT among the vacation types the statistics are built for
        final VacationType<?> activeVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L).active(true).category(HOLIDAY).messageKey("active").build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual =
            sut.build(persons, from, to, List.of(activeVacationType));

        assertThat(actual.get(personWithAbsence)).hasValueSatisfying(statistics -> {
            assertThat(statistics.hasVacationType(deactivatedVacationType)).isTrue();
            assertThat(statistics.getAllowedVacationDays(deactivatedVacationType)).isEqualTo(BigDecimal.ONE);
            assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.ONE);
        });

        assertThat(actual.get(personWithoutAbsence)).hasValueSatisfying(statistics ->
            assertThat(statistics.hasVacationType(deactivatedVacationType)).isFalse());
    }

    /**
     * The caller of this overload has already fetched the applications, so the deactivated type must survive that
     * hand-over too - the sort-by-statistics view of the page uses this overload.
     */
    @Test
    void ensureDeactivatedVacationTypeIsOnlyShownForThePersonWhoUsedItWithGivenApplications() {

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);
        final DateRange dateRange = new DateRange(from, to);
        final LocalDate expiryDate = of(2014, APRIL, 1);

        final Person personWithAbsence = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personWithAbsence.setId(1L);
        final Person personWithoutAbsence = new Person("other", "Other", "Otto", "other@example.org");
        personWithoutAbsence.setId(2L);
        final List<Person> persons = List.of(personWithAbsence, personWithoutAbsence);

        final Account accountWithAbsence = new Account(personWithAbsence, from, to, false, expiryDate, TEN, TEN, TEN, null);
        final Account accountWithoutAbsence = new Account(personWithoutAbsence, from, to, false, expiryDate, TEN, TEN, TEN, null);
        final List<Account> accounts = List.of(accountWithAbsence, accountWithoutAbsence);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(accounts);

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendars =
            Map.of(personWithAbsence, workingTimeCalendar, personWithoutAbsence, workingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(workingTimeCalendars);

        final VacationType<?> deactivatedVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(4711L).active(false).category(HOLIDAY).messageKey("deactivated").build();

        final Application absenceOfDeactivatedType = new Application();
        absenceOfDeactivatedType.setPerson(personWithAbsence);
        absenceOfDeactivatedType.setDayLength(FULL);
        absenceOfDeactivatedType.setVacationType(deactivatedVacationType);
        absenceOfDeactivatedType.setStartDate(of(2014, 10, 13));
        absenceOfDeactivatedType.setEndDate(of(2014, 10, 13));
        absenceOfDeactivatedType.setStatus(ALLOWED);

        final List<Application> applications = List.of(absenceOfDeactivatedType);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(Map.of());

        final HolidayAccountVacationDays vacationDaysWithAbsence = new HolidayAccountVacationDays(
            accountWithAbsence, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        final HolidayAccountVacationDays vacationDaysWithoutAbsence = new HolidayAccountVacationDays(
            accountWithoutAbsence, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        when(vacationDaysService.getVacationDaysLeft(accounts, dateRange, List.of(), workingTimeCalendars))
            .thenReturn(Map.of(accountWithAbsence, vacationDaysWithAbsence, accountWithoutAbsence, vacationDaysWithoutAbsence));

        final VacationType<?> activeVacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L).active(true).category(HOLIDAY).messageKey("active").build();

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual =
            sut.build(persons, from, to, List.of(activeVacationType), applications);

        assertThat(actual.get(personWithAbsence)).hasValueSatisfying(statistics -> {
            assertThat(statistics.hasVacationType(deactivatedVacationType)).isTrue();
            assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.ONE);
        });

        assertThat(actual.get(personWithoutAbsence)).hasValueSatisfying(statistics ->
            assertThat(statistics.hasVacationType(deactivatedVacationType)).isFalse());
    }

    /**
     * The applications the caller passes in cover the whole year, the tallies only the requested period. Narrowing
     * that down must happen in memory - asking the database a second time for a subset of what we already hold was
     * the single most expensive part of building these statistics.
     */
    @Test
    void ensureApplicationsAreNotFetchedAgainAndTheCalendarIsComputedOnlyOnce() {

        final LocalDate from = of(2014, OCTOBER, 1);
        final LocalDate to = of(2014, OCTOBER, 31);
        final DateRange dateRange = new DateRange(from, to);
        final LocalDate expiryDate = of(2014, APRIL, 1);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        final List<Person> persons = List.of(person);

        final Account account = new Account(person, of(2014, JANUARY, 1), of(2014, DECEMBER, 31), false, expiryDate, TEN, TEN, TEN, null);
        final List<Account> accounts = List.of(account);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(accounts);

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(of(2014, JANUARY, 1), of(2014, DECEMBER, 31));
        final Map<Person, WorkingTimeCalendar> workingTimeCalendars = Map.of(person, workingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(workingTimeCalendars);

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L).active(true).category(HOLIDAY).messageKey("active").build();

        // one absence inside the requested period, one outside of it but within the same year
        final Application insidePeriod = new Application();
        insidePeriod.setPerson(person);
        insidePeriod.setDayLength(FULL);
        insidePeriod.setVacationType(vacationType);
        insidePeriod.setStartDate(of(2014, 10, 13));
        insidePeriod.setEndDate(of(2014, 10, 13));
        insidePeriod.setStatus(ALLOWED);

        final Application outsidePeriod = new Application();
        outsidePeriod.setPerson(person);
        outsidePeriod.setDayLength(FULL);
        outsidePeriod.setVacationType(vacationType);
        outsidePeriod.setStartDate(of(2014, 3, 13));
        outsidePeriod.setEndDate(of(2014, 3, 13));
        outsidePeriod.setStatus(ALLOWED);

        final List<Application> applications = List.of(insidePeriod, outsidePeriod);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(Map.of());

        final HolidayAccountVacationDays vacationDays = new HolidayAccountVacationDays(
            account, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        when(vacationDaysService.getVacationDaysLeft(accounts, dateRange, List.of(), workingTimeCalendars))
            .thenReturn(Map.of(account, vacationDays));

        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual =
            sut.build(persons, from, to, List.of(vacationType), applications);

        // only the absence within the requested period is tallied ...
        assertThat(actual.get(person)).hasValueSatisfying(statistics ->
            assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.ONE));

        // ... without asking the database again, and without computing the calendar twice
        verifyNoInteractions(applicationService);
        verify(workingTimeCalendarService, times(1)).getWorkingTimesByPersons(persons, Year.of(2014));
        verifyNoMoreInteractions(workingTimeCalendarService);
    }

    @Test
    void ensureThatAllPersonsThatWhereRequestedAreThereWithOptionalEmptyIfNoAccount() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);

        final List<Account> accounts = List.of();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final List<Person> persons = List.of(person);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(accounts);

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(Map.of(person, workingTimeCalendar));

        final List<Application> applications = List.of();
        final List<Person> personsWithAccount = List.of();
        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, personsWithAccount, activeStatuses())).thenReturn(applications);

        final Map<Person, LeftOvertime> leftOvertimeByPerson = Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ofHours(3)));
        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(leftOvertimeByPerson);

        when(vacationDaysService.getVacationDaysLeft(accounts, new DateRange(from, to), List.of(), Map.of(person, workingTimeCalendar))).thenReturn(Map.of());

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();
        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual = sut.build(persons, from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person)
            .containsEntry(person, Optional.empty());
    }

    /**
     * Callers map the returned map back over the persons they asked for, so every requested person needs an entry -
     * also for this overload. Someone who joined this year has no account for last year, and a missing entry made the
     * statistics page fail for the whole company as soon as it was sorted by one of the statistics columns.
     */
    @Test
    void ensureThatAllPersonsThatWhereRequestedAreThereWithOptionalEmptyIfNoAccountWithGivenApplications() {
        final ApplicationForLeaveStatisticsBuilder sut = new ApplicationForLeaveStatisticsBuilder(accountService, applicationService,
            workingTimeCalendarService, vacationDaysService, overtimeService, Clock.fixed(Instant.parse("2014-06-24T16:02:42.00Z"), ZoneOffset.UTC));

        final LocalDate from = of(2014, JANUARY, 1);
        final LocalDate to = of(2014, DECEMBER, 31);

        final Person personWithAccount = new Person("muster", "Muster", "Marlene", "muster@example.org");
        personWithAccount.setId(1L);
        final Person personWithoutAccount = new Person("newbie", "Joiner", "New", "newbie@example.org");
        personWithoutAccount.setId(2L);
        final List<Person> persons = List.of(personWithAccount, personWithoutAccount);

        final Account account = new Account(personWithAccount, from, to, false, of(2014, APRIL, 1), TEN, TEN, TEN, null);
        final List<Account> accounts = List.of(account);
        when(accountService.getHolidaysAccount(2014, persons)).thenReturn(accounts);

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarMondayToSunday(from, to);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendars =
            Map.of(personWithAccount, workingTimeCalendar, personWithoutAccount, workingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(workingTimeCalendars);

        final List<Application> applications = List.of();
        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(Map.of());

        final HolidayAccountVacationDays vacationDays = new HolidayAccountVacationDays(
            account, VacationDaysLeft.builder().build(), VacationDaysLeft.builder().build());
        when(vacationDaysService.getVacationDaysLeft(accounts, new DateRange(from, to), List.of(), workingTimeCalendars))
            .thenReturn(Map.of(account, vacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();
        final Map<Person, Optional<ApplicationForLeaveStatistics>> actual =
            sut.build(persons, from, to, List.of(type), applications);

        assertThat(actual)
            .hasSize(2)
            .containsEntry(personWithoutAccount, Optional.empty());
        assertThat(actual.get(personWithAccount)).isPresent();
    }
}
