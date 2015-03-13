package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.List;


/**
 * Builds a {@link org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics} for the given
 * {@link org.synyx.urlaubsverwaltung.core.person.Person} and period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
public class ApplicationForLeaveStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final OwnCalendarService calendarService;
    private final CalculationService calculationService;

    @Autowired
    public ApplicationForLeaveStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
        OwnCalendarService calendarService, CalculationService calculationService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.calculationService = calculationService;
    }

    public ApplicationForLeaveStatistics build(Person person, DateMidnight from, DateMidnight to) {

        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        ApplicationForLeaveStatistics statistics = new ApplicationForLeaveStatistics(person);

        Account account = accountService.getHolidaysAccount(from.getYear(), person);

        if (account != null) {
            BigDecimal vacationDaysLeft = calculationService.calculateTotalLeftVacationDays(account);
            statistics.setLeftVacationDays(vacationDaysLeft);
        }

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person);

        BigDecimal waitingVacationDays = BigDecimal.ZERO;
        BigDecimal allowedVacationDays = BigDecimal.ZERO;

        for (Application application : applications) {
            if (application.hasStatus(ApplicationStatus.WAITING)) {
                waitingVacationDays = waitingVacationDays.add(getVacationDays(application, from.getYear()));
            } else if (application.hasStatus(ApplicationStatus.ALLOWED)) {
                allowedVacationDays = allowedVacationDays.add(getVacationDays(application, from.getYear()));
            }
        }

        statistics.setWaitingVacationDays(waitingVacationDays);
        statistics.setAllowedVacationDays(allowedVacationDays);

        return statistics;
    }


    private BigDecimal getVacationDays(Application application, int relevantYear) {

        int yearOfStartDate = application.getStartDate().getYear();
        int yearOfEndDate = application.getEndDate().getYear();

        DayLength dayLength = application.getHowLong();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            DateMidnight startDate = getStartDateForCalculation(application, relevantYear);
            DateMidnight endDate = getEndDateForCalculation(application, relevantYear);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private DateMidnight getStartDateForCalculation(Application application, int relevantYear) {

        if (application.getStartDate().getYear() != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        }

        return application.getStartDate();
    }


    private DateMidnight getEndDateForCalculation(Application application, int relevantYear) {

        if (application.getEndDate().getYear() != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        }

        return application.getEndDate();
    }
}
