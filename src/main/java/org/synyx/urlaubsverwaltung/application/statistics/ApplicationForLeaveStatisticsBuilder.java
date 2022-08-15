package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.overtime.LeftOvertime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Duration.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeServiceImpl.convert;

/**
 * Builds a {@link ApplicationForLeaveStatistics} for the given
 * {@link org.synyx.urlaubsverwaltung.person.Person} and period.
 */
@Component
class ApplicationForLeaveStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final VacationDaysService vacationDaysService;
    private final OvertimeService overtimeService;
    private final Clock clock;

    @Autowired
    ApplicationForLeaveStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
                                         WorkDaysCountService workDaysCountService, VacationDaysService vacationDaysService,
                                         OvertimeService overtimeService, Clock clock) {
        this.accountService = accountService;
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.clock = clock;
    }

    public Map<Person, ApplicationForLeaveStatistics> build(List<Person> persons, LocalDate from, LocalDate to, List<VacationType> vacationTypes) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        final LocalDate today = LocalDate.now(clock);

        final List<Account> holidayAccounts = accountService.getHolidaysAccount(from.getYear(), persons);
        final List<Application> applications = applicationService.getApplicationsForACertainPeriod(from, to, persons);
        final Map<Person, LeftOvertime> leftOvertimeForPersons = overtimeService.getLeftOvertimeTotalAndDateRangeForPersons(persons, applications, from, to);

        final Map<Person, ApplicationForLeaveStatistics> statisticsByPerson = holidayAccounts.stream()
            .map(account -> {
                final Person accountPerson = account.getPerson();
                final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(accountPerson, vacationTypes);

                final LocalDate firstDayOfYear = from.with(firstDayOfYear());
                final LocalDate lastDayOfYear = to.with(lastDayOfYear());
                final VacationDaysLeft vacationDaysLeftYear = vacationDaysService.getVacationDaysLeft(firstDayOfYear, lastDayOfYear, account, Optional.empty());
                statistics.setLeftVacationDaysForYear(vacationDaysLeftYear.getLeftVacationDays(today, account.doRemainigVacationDaysExpire(), account.getExpiryDate()));
                statistics.setLeftRemainingVacationDaysForYear(vacationDaysLeftYear.getRemainingVacationDaysLeft(today, account.doRemainigVacationDaysExpire(), account.getExpiryDate()));

                final VacationDaysLeft vacationDaysLeftPeriod = vacationDaysService.getVacationDaysLeft(from, to, account, Optional.empty());
                statistics.setLeftVacationDaysForPeriod(vacationDaysLeftPeriod.getLeftVacationDays(to, account.doRemainigVacationDaysExpire(), account.getExpiryDate()));
                statistics.setLeftRemainingVacationDaysForPeriod(vacationDaysLeftPeriod.getRemainingVacationDaysLeft(to, account.doRemainigVacationDaysExpire(), account.getExpiryDate()));

                if (leftOvertimeForPersons.containsKey(accountPerson)) {
                    final LeftOvertime leftOvertime = leftOvertimeForPersons.get(accountPerson);
                    statistics.setLeftOvertimeForYear(leftOvertime.getLeftOvertimeOverall().getLeftOvertime());
                    statistics.setLeftOvertimeForPeriod(leftOvertime.getLeftOvertimeDateRange().getLeftOvertime());
                } else {
                    statistics.setLeftOvertimeForYear(ZERO);
                    statistics.setLeftOvertimeForPeriod(ZERO);
                }

                return statistics;
            }).collect(toMap(ApplicationForLeaveStatistics::getPerson, identity()));

        for (Person person : persons) {
            final List<Application> personApplications = applications.stream().filter(application -> application.getPerson().equals(person)).collect(toList());
            for (Application application : personApplications) {
                final ApplicationForLeaveStatistics statistics = statisticsByPerson.get(application.getPerson());
                if (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED)) {
                    statistics.addWaitingVacationDays(convert(application.getVacationType()), getVacationDaysFor(application, from, to));
                } else if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                    statistics.addAllowedVacationDays(convert(application.getVacationType()), getVacationDaysFor(application, from, to));
                }
            }
        }

        return statisticsByPerson;
    }

    private BigDecimal getVacationDaysFor(Application application, LocalDate from, LocalDate to) {

        final DayLength dayLength = application.getDayLength();
        final Person person = application.getPerson();

        final LocalDate startDate = application.getStartDate().isBefore(from) ? from : application.getStartDate();
        final LocalDate endDate = application.getEndDate().isAfter(to) ? to : application.getEndDate();

        return workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
    }
}
