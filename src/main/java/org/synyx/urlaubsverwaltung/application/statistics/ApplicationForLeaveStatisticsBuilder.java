package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
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

import static java.time.Duration.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.function.Function.identity;
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

    @Autowired
    ApplicationForLeaveStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
                                         WorkingTimeCalendarService workingTimeCalendarService,
                                         VacationDaysService vacationDaysService, OvertimeService overtimeService, Clock clock) {
        this.accountService = accountService;
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.clock = clock;
    }

    public Map<Person, ApplicationForLeaveStatistics> build(List<Person> persons, LocalDate from, LocalDate to, List<VacationType<?>> vacationTypes) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        final LocalDate today = LocalDate.now(clock);
        final DateRange dateRange = new DateRange(from, to);

        final List<Account> holidayAccounts = accountService.getHolidaysAccount(from.getYear(), persons);

        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndStatus(from.with(firstDayOfYear()), from.with(lastDayOfYear()), persons, activeStatuses());
        final Map<Person, LeftOvertime> leftOvertimeForPersons = overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);
        final Map<Account, HolidayAccountVacationDays> holidayAccountVacationDaysByAccount = vacationDaysService.getVacationDaysLeft(holidayAccounts, dateRange);

        final Map<Person, ApplicationForLeaveStatistics> statisticsByPerson = holidayAccounts.stream()
            .map(account -> {
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

                if (leftOvertimeForPersons.containsKey(accountPerson)) {
                    final LeftOvertime leftOvertime = leftOvertimeForPersons.get(accountPerson);
                    statistics.setLeftOvertimeForYear(leftOvertime.leftOvertimeOverall());
                    statistics.setLeftOvertimeForPeriod(leftOvertime.leftOvertimeDateRange());
                } else {
                    statistics.setLeftOvertimeForYear(ZERO);
                    statistics.setLeftOvertimeForPeriod(ZERO);
                }

                return statistics;
            }).collect(toMap(ApplicationForLeaveStatistics::getPerson, identity()));

        final Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson = workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(from.getYear()));
        final Map<Person, List<Application>> applicationsByPerson =
            applicationService.getApplicationsForACertainPeriodAndStatus(from, to, persons, activeStatuses())
                .stream()
                .collect(groupingBy(Application::getPerson));
        for (Person person : persons) {
            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarsByPerson.get(person);
            final List<Application> personApplications = applicationsByPerson.getOrDefault(person, List.of());
            for (Application application : personApplications) {

                final BigDecimal workingTime = workingTimeCalendar.workingTimeInDateRage(application, dateRange);
                final ApplicationForLeaveStatistics statistics = statisticsByPerson.get(application.getPerson());

                if (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED)) {
                    statistics.addWaitingVacationDays(application.getVacationType(), workingTime);
                } else if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                    statistics.addAllowedVacationDays(application.getVacationType(), workingTime);
                }
            }
        }

        return statisticsByPerson;
    }
}
