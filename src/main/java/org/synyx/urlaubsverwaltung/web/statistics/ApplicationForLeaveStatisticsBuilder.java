package org.synyx.urlaubsverwaltung.web.statistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;

/**
 * Builds a
 * {@link org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics}
 * for the given {@link org.synyx.urlaubsverwaltung.core.person.Person} and
 * period.
 *
 * @author Aljona Murygina - murygina@synyx.de
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
                    WorkDaysService calendarService, VacationDaysService vacationDaysService,
                    OvertimeService overtimeService, VacationTypeService vacationTypeService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.vacationDaysService = vacationDaysService;
        this.overtimeService = overtimeService;
        this.vacationTypeService = vacationTypeService;
    }

    public ApplicationForLeaveStatistics build(Person person, DateMidnight from, DateMidnight to) {

        Assert.notNull(person, "Person must be given");
        Assert.notNull(from, "From must be given");
        Assert.notNull(to, "To must be given");

        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        Optional<Account> account = accountService.getHolidaysAccount(from.getYear(), person);

        if (account.isPresent()) {
            BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account.get());
            statistics.setLeftVacationDays(vacationDaysLeft);
            statistics.setEntitlementVacationDays(account.get().getAnnualVacationDays());
        }

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person);

        for (Application application : applications) {
            if (application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
                statistics.addWaitingVacationDays(application.getVacationType(),
                                getVacationDays(application, from.getYear()));
            } else if (application.hasStatus(ApplicationStatus.ALLOWED)) {
                statistics.addAllowedVacationDays(application.getVacationType(),
                                getVacationDays(application, from.getYear()));
            }
        }

        statistics.setLeftOvertime(overtimeService.getLeftOvertimeForPerson(person));

        return statistics;
    }

    private BigDecimal getVacationDays(Application application, int relevantYear) {

        int yearOfStartDate = application.getStartDate().getYear();
        int yearOfEndDate = application.getEndDate().getYear();

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            DateMidnight startDate = getStartDateForCalculation(application, relevantYear);
            DateMidnight endDate = getEndDateForCalculation(application, relevantYear);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }

    private DateMidnight getStartDateForCalculation(Application application, int relevantYear) {

        if (application.getStartDate().getYear() != relevantYear)
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());

        return application.getStartDate();
    }

    private DateMidnight getEndDateForCalculation(Application application, int relevantYear) {

        if (application.getEndDate().getYear() != relevantYear)
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());

        return application.getEndDate();
    }
}
