package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.overtime.LeftOvertime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Duration.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;

/**
 * Builds a {@link ApplicationForLeaveStatistics} for the given
 * {@link org.synyx.urlaubsverwaltung.person.Person} and period.
 */
@Component
class ApplicationForLeaveStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final VacationDaysService vacationDaysService;
    private final OvertimeService overtimeService;
    private final Clock clock;

    ApplicationForLeaveStatisticsBuilder(
        AccountService accountService, ApplicationService applicationService,
        WorkingTimeCalendarService workingTimeCalendarService,
        VacationDaysService vacationDaysService, OvertimeService overtimeService, Clock clock
    ) {
        this.accountService = accountService;
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.clock = clock;
    }

    public Map<Person, Optional<ApplicationForLeaveStatistics>> build(List<Person> persons, LocalDate from, LocalDate to, List<VacationType<?>> vacationTypes) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        final List<Account> holidayAccounts = accountService.getHolidaysAccount(from.getYear(), persons);
        final List<Person> personsWithAccount = holidayAccounts.stream().map(Account::getPerson).toList();

        final LocalDate firstDateOfYear = from.with(firstDayOfYear());
        final LocalDate lastDateOfYear = from.with(lastDayOfYear());
        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndStatus(firstDateOfYear, lastDateOfYear, personsWithAccount, activeStatuses());

        return buildStatistics(new DateRange(from, to), persons, holidayAccounts, applications, vacationTypes);
    }

    /**
     * Same as {@link #build(List, LocalDate, LocalDate, List)}, but for callers that have already fetched the
     * applications themselves.
     *
     * @param applications every application of the requested <em>year</em> - not only of the requested period - with
     *                     an {@link org.synyx.urlaubsverwaltung.application.application.ApplicationStatus#activeStatuses()
     *                     active status}, of any {@link VacationType}, covering at least the given persons. The whole
     *                     year is required because the left overtime of the year is derived from it, and all vacation
     *                     types are required so that types deactivated in the meantime still show up for the persons
     *                     who used them.
     */
    public Map<Person, Optional<ApplicationForLeaveStatistics>> build(List<Person> persons, LocalDate from, LocalDate to, List<VacationType<?>> vacationTypes, List<Application> applications) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        final List<Account> holidayAccounts = accountService.getHolidaysAccount(from.getYear(), persons);

        return buildStatistics(new DateRange(from, to), persons, holidayAccounts, applications, vacationTypes);
    }

    private Map<Person, Optional<ApplicationForLeaveStatistics>> buildStatistics(DateRange dateRange, List<Person> persons, List<Account> holidayAccounts, List<Application> applications, List<VacationType<?>> vacationTypes) {

        final Map<Person, Optional<ApplicationForLeaveStatistics>> statisticsByPerson =
            getStatisticsByPersonWithoutApplicationInfo(dateRange, persons, holidayAccounts, applications, vacationTypes);

        // persons without a holiday account for the requested year get no statistics. Register them before tallying,
        // so that the tally below can rely on every person having an entry.
        addMissingPersonsToStatistics(statisticsByPerson, persons);

        addApplicationInfosToStatistics(dateRange, persons, statisticsByPerson);

        return statisticsByPerson;
    }

    private void addMissingPersonsToStatistics(Map<Person, Optional<ApplicationForLeaveStatistics>> statisticsByPerson, List<Person> persons) {
        persons.forEach(person ->
            statisticsByPerson.computeIfAbsent(person, _ -> Optional.empty())
        );
    }

    private Map<Person, Optional<ApplicationForLeaveStatistics>> getStatisticsByPersonWithoutApplicationInfo(DateRange dateRange, List<Person> persons, List<Account> holidayAccounts, List<Application> applications, List<VacationType<?>> vacationTypes) {

        final LocalDate from = dateRange.startDate();
        final LocalDate to = dateRange.endDate();

        final Map<Person, LeftOvertime> leftOvertimeByPerson = overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);
        final Map<Account, HolidayAccountVacationDays> vacationDaysByAccount = vacationDaysService.getVacationDaysLeft(holidayAccounts, dateRange);

        return holidayAccounts.stream()
            .map(account -> buildStatisticsForAccount(dateRange, account, vacationTypes, vacationDaysByAccount, leftOvertimeByPerson))
            .collect(toMap(ApplicationForLeaveStatistics::getPerson, Optional::of));
    }

    private ApplicationForLeaveStatistics buildStatisticsForAccount(DateRange dateRange, Account account, List<VacationType<?>> vacationTypes, Map<Account, HolidayAccountVacationDays> holidayAccountVacationDaysByAccount, Map<Person, LeftOvertime> leftOvertimeByPerson) {

        final LocalDate today = LocalDate.now(clock);
        final LocalDate to = dateRange.endDate();

        final Person accountPerson = account.getPerson();
        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(accountPerson, vacationTypes);

        if (holidayAccountVacationDaysByAccount.containsKey(account)) {
            final HolidayAccountVacationDays holidayAccountVacationDays = holidayAccountVacationDaysByAccount.get(account);

            final VacationDaysLeft vacationDaysLeftYear = holidayAccountVacationDays.vacationDaysYear();
            statistics.setLeftVacationDaysForYear(vacationDaysLeftYear.getLeftVacationDays(today, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));
            statistics.setLeftRemainingVacationDaysForYear(vacationDaysLeftYear.getRemainingVacationDaysLeft(today, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));

            final VacationDaysLeft vacationDaysLeftPeriod = holidayAccountVacationDays.vacationDaysDateRange();
            statistics.setLeftVacationDaysForPeriod(vacationDaysLeftPeriod.getLeftVacationDays(to, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));
            statistics.setLeftRemainingVacationDaysForPeriod(vacationDaysLeftPeriod.getRemainingVacationDaysLeft(to, account.doRemainingVacationDaysExpire(), account.getExpiryDate()));
        }

        if (leftOvertimeByPerson.containsKey(accountPerson)) {
            final LeftOvertime leftOvertime = leftOvertimeByPerson.get(accountPerson);
            statistics.setLeftOvertimeForYear(leftOvertime.leftOvertimeOverall());
            statistics.setLeftOvertimeForPeriod(leftOvertime.leftOvertimeDateRange());
        } else {
            statistics.setLeftOvertimeForYear(ZERO);
            statistics.setLeftOvertimeForPeriod(ZERO);
        }

        return statistics;
    }

    private void addApplicationInfosToStatistics(DateRange dateRange, List<Person> persons, Map<Person, Optional<ApplicationForLeaveStatistics>> statisticsByPerson) {

        final LocalDate from = dateRange.startDate();
        final LocalDate to = dateRange.endDate();

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson =
            workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(from.getYear()));

        final Map<Person, List<Application>> applicationsByPerson =
            applicationService.getApplicationsForACertainPeriodAndStatus(from, to, persons, activeStatuses())
                .stream()
                .collect(groupingBy(Application::getPerson));

        for (Person person : persons) {
            final Optional<ApplicationForLeaveStatistics> maybeStatistics = statisticsByPerson.get(person);
            if (maybeStatistics.isEmpty()) {
                // no holiday account for the requested year, so there is nothing to tally the applications into
                continue;
            }

            final ApplicationForLeaveStatistics statistics = maybeStatistics.get();
            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarsByPerson.get(person);

            for (Application application : applicationsByPerson.getOrDefault(person, List.of())) {
                final BigDecimal workingTime = workingTimeCalendar.workingTimeInDateRage(application, dateRange);
                if (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED)) {
                    statistics.addWaitingVacationDays(application.getVacationType(), workingTime);
                } else if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                    statistics.addAllowedVacationDays(application.getVacationType(), workingTime);
                }
            }
        }
    }
}
