package org.synyx.urlaubsverwaltung.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoField.YEAR;


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

    public ApplicationForLeaveStatistics build(Person person, Instant from, Instant to) {

        Assert.notNull(person, "Person must be given");
        Assert.notNull(from, "From must be given");
        Assert.notNull(to, "To must be given");

        Assert.isTrue(from.get(YEAR) == to.get(YEAR), "From and to must be in the same year");

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person, vacationTypeService);

        Optional<Account> account = accountService.getHolidaysAccount(from.get(YEAR), person);

        if (account.isPresent()) {
            BigDecimal vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account.get());
            statistics.setLeftVacationDays(vacationDaysLeft);
        }

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person);

        for (Application application : applications) {
            if (application.hasStatus(ApplicationStatus.WAITING)
                || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
                statistics.addWaitingVacationDays(application.getVacationType(),
                    getVacationDays(application, from.get(YEAR)));
            } else if (application.hasStatus(ApplicationStatus.ALLOWED)) {
                statistics.addAllowedVacationDays(application.getVacationType(),
                    getVacationDays(application, from.get(YEAR)));
            }
        }

        statistics.setLeftOvertime(overtimeService.getLeftOvertimeForPerson(person));

        return statistics;
    }


    private BigDecimal getVacationDays(Application application, int relevantYear) {

        int yearOfStartDate = application.getStartDate().get(YEAR);
        int yearOfEndDate = application.getEndDate().get(YEAR);

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            Instant startDate = getStartDateForCalculation(application, relevantYear);
            Instant endDate = getEndDateForCalculation(application, relevantYear);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private Instant getStartDateForCalculation(Application application, int relevantYear) {

        if (application.getStartDate().get(YEAR) != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().get(YEAR));
        }

        return application.getStartDate();
    }


    private Instant getEndDateForCalculation(Application application, int relevantYear) {

        if (application.getEndDate().get(YEAR) != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().get(YEAR));
        }

        return application.getEndDate();
    }
}
