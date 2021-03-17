package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;


/**
 * Builds a {@link ApplicationForLeaveStatistics} for the given
 * {@link org.synyx.urlaubsverwaltung.person.Person} and period.
 */
@Component
public class ApplicationForLeaveStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final WorkDaysCountService workDaysCountService;
    private final VacationDaysService vacationDaysService;
    private final OvertimeService overtimeService;
    private final VacationTypeService vacationTypeService;

    @Autowired
    public ApplicationForLeaveStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
                                                WorkDaysCountService workDaysCountService, VacationDaysService vacationDaysService, OvertimeService overtimeService,
                                                VacationTypeService vacationTypeService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.workDaysCountService = workDaysCountService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.vacationTypeService = vacationTypeService;
    }

    public ApplicationForLeaveStatistics build(Person person, LocalDate from, LocalDate to) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        final ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);
        final Optional<Account> account = accountService.getHolidaysAccount(from.getYear(), person);
        if (account.isPresent()) {
            final BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account.get());
            statistics.setLeftVacationDays(vacationDaysLeft);
        }

        final List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person);
        for (Application application : applications) {
            if (application.hasStatus(WAITING) || application.hasStatus(TEMPORARY_ALLOWED)) {
                statistics.addWaitingVacationDays(application.getVacationType(), getVacationDays(application, from.getYear()));
            } else if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED)) {
                statistics.addAllowedVacationDays(application.getVacationType(), getVacationDays(application, from.getYear()));
            }
        }

        statistics.setLeftOvertime(overtimeService.getLeftOvertimeForPerson(person));
        return statistics;
    }

    private BigDecimal getVacationDays(Application application, int relevantYear) {

        final int yearOfStartDate = application.getStartDate().getYear();
        final int yearOfEndDate = application.getEndDate().getYear();

        final DayLength dayLength = application.getDayLength();
        final Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            final LocalDate startDate = getStartDateForCalculation(application, relevantYear);
            final LocalDate endDate = getEndDateForCalculation(application, relevantYear);

            return workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
        }

        return workDaysCountService.getWorkDaysCount(dayLength, application.getStartDate(), application.getEndDate(), person);
    }

    private LocalDate getStartDateForCalculation(Application application, int relevantYear) {
        if (application.getStartDate().getYear() != relevantYear) {
            return getFirstDayOfYear(application.getEndDate().getYear());
        }

        return application.getStartDate();
    }

    private LocalDate getEndDateForCalculation(Application application, int relevantYear) {
        if (application.getEndDate().getYear() != relevantYear) {
            return getLastDayOfYear(application.getStartDate().getYear());
        }

        return application.getEndDate();
    }
}
