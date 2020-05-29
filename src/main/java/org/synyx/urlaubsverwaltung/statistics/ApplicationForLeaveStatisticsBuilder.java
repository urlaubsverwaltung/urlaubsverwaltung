package org.synyx.urlaubsverwaltung.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCEL_RE;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Builds a {@link ApplicationForLeaveStatistics} for the given
 * {@link org.synyx.urlaubsverwaltung.person.Person} and period.
 */
@Component
public class ApplicationForLeaveStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final WorkDaysService calendarService;
    private final VacationDaysService vacationDaysService;
    private final OvertimeService overtimeService;
    private final VacationTypeService vacationTypeService;

    @Autowired
    public ApplicationForLeaveStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
                                                WorkDaysService calendarService, VacationDaysService vacationDaysService, OvertimeService overtimeService,
                                                VacationTypeService vacationTypeService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.vacationTypeService = vacationTypeService;
    }

    public ApplicationForLeaveStatistics build(Person person, LocalDate from, LocalDate to) {
        Assert.notNull(person, "Person must be given");
        Assert.notNull(from, "From must be given");
        Assert.notNull(to, "To must be given");
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
            } else if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCEL_RE)) {
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

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }

    private LocalDate getStartDateForCalculation(Application application, int relevantYear) {
        if (application.getStartDate().getYear() != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        }

        return application.getStartDate();
    }

    private LocalDate getEndDateForCalculation(Application application, int relevantYear) {
        if (application.getEndDate().getYear() != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        }

        return application.getEndDate();
    }
}
