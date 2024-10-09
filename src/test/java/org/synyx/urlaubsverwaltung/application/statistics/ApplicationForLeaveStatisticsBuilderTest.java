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
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.of;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypes;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

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
            .isThrownBy(() -> sut.build(List.of(new Person()), of(2014, 1, 1), of(2015, 1, 1), List.of(type)));
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.get(0));
        applicationForLeave.setStartDate(of(2014, 10, 13));
        applicationForLeave.setEndDate(of(2014, 10, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(10)).build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(5)).build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(statistics.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(5));
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.get(0));
        applicationForLeave.setStartDate(of(2014, 10, 13));
        applicationForLeave.setEndDate(of(2014, 10, 13));
        applicationForLeave.setStatus(ALLOWED);

        final List<Application> applications = List.of(applicationForLeave);

        when(applicationService.getApplicationsForACertainPeriodAndStatus(firstDayOfYear, lastDayOfYear, List.of(person), activeStatuses())).thenReturn(applications);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(List.of(person), applications, from, to))
            .thenReturn(Map.of(person, new LeftOvertime(Duration.ofHours(9), Duration.ZERO)));

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(10)).build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().withAnnualVacation(BigDecimal.valueOf(5)).build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(statistics.getLeftVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(5));
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.get(0));
        applicationForLeave.setStartDate(of(2014, 10, 13));
        applicationForLeave.setEndDate(of(2014, 10, 13));
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

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getLeftRemainingVacationDaysForYear()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.valueOf(2));
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setDayLength(FULL);
        applicationForLeave.setVacationType(vacationTypes.get(0));
        applicationForLeave.setStartDate(of(2014, 10, 13));
        applicationForLeave.setEndDate(of(2014, 10, 13));
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

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getLeftRemainingVacationDaysForYear()).isEqualTo(BigDecimal.ZERO);
        assertThat(statistics.getLeftRemainingVacationDaysForPeriod()).isEqualTo(BigDecimal.ZERO);
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

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

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(List.of(person), from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getTotalWaitingVacationDays()).isEqualTo(BigDecimal.valueOf(4));
        assertThat(statistics.getTotalAllowedVacationDays()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(statistics.getLeftVacationDaysForYear()).isEqualTo(TEN);
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

        final Map<LocalDate, WorkingDayInformation> personWorkingTimeByDate = buildWorkingTimeByDate(from, to, date -> fullWorkingDayInformation());
        final WorkingTimeCalendar personWorkingTimeCalendar = new WorkingTimeCalendar(personWorkingTimeByDate);

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarByPerson = Map.of(person, personWorkingTimeCalendar);
        when(workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(2014))).thenReturn(workingTimeCalendarByPerson);

        final List<Application> applications = List.of();
        when(applicationService.getApplicationsForACertainPeriodAndStatus(from, to, persons, activeStatuses())).thenReturn(applications);

        final LeftOvertime personLeftOvertime = new LeftOvertime(Duration.ofHours(9), Duration.ofHours(3));
        final Map<Person, LeftOvertime> leftOvertimeByPerson = Map.of(person, personLeftOvertime);

        when(overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to)).thenReturn(leftOvertimeByPerson);

        final VacationDaysLeft personVacationDaysLeftYear = VacationDaysLeft.builder().build();
        final VacationDaysLeft personVacationDaysLeftPeriod = VacationDaysLeft.builder().build();
        final HolidayAccountVacationDays personVacationDays = new HolidayAccountVacationDays(account, personVacationDaysLeftYear, personVacationDaysLeftPeriod);

        when(vacationDaysService.getVacationDaysLeft(List.of(account), dateRange)).thenReturn(Map.of(account, personVacationDays));

        final VacationType<?> type = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Map<Person, ApplicationForLeaveStatistics> actual = sut.build(persons, from, to, List.of(type));
        assertThat(actual)
            .hasSize(1)
            .containsKey(person);

        final ApplicationForLeaveStatistics statistics = actual.get(person);
        assertThat(statistics.getPerson()).isEqualTo(person);
        assertThat(statistics.getLeftOvertimeForYear()).isEqualTo(Duration.ofHours(9));
        assertThat(statistics.getLeftOvertimeForPeriod()).isEqualTo(Duration.ofHours(3));
    }

    private static WorkingDayInformation fullWorkingDayInformation() {
        return new WorkingDayInformation(FULL, WORKDAY, WORKDAY);
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }
}
