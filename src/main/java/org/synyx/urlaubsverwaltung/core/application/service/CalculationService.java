package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;

import java.util.List;


/**
 * This service calculates if a {@link Person} may apply for leave, i.e. if he/she has enough vacation days to apply for
 * leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class CalculationService {

    private final ApplicationService applicationService;
    private final AccountInteractionService accountInteractionService;
    private final AccountService accountService;
    private final OwnCalendarService calendarService;

    @Autowired
    public CalculationService(ApplicationService applicationService, AccountService accountService,
        AccountInteractionService accountInteractionService, OwnCalendarService calendarService) {

        this.applicationService = applicationService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.calendarService = calendarService;
    }

    /**
     * Checks if applying for leave is possible, i.e. there are enough vacation days left to be used for the given
     * {@link org.synyx.urlaubsverwaltung.core.application.domain.Application} for leave.
     *
     * @param  application  for leave to check
     *
     * @return  {@code true} if the {@link org.synyx.urlaubsverwaltung.core.application.domain.Application} for leave
     *          may be saved because there are enough vacation days left, {@code false} else
     */
    public boolean checkApplication(Application application) {

        Person person = application.getPerson();

        DayLength dayLength = application.getHowLong();

        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        int yearOfStartDate = startDate.getYear();
        int yearOfEndDate = endDate.getYear();

        if (yearOfStartDate == yearOfEndDate) {
            BigDecimal workDays = calendarService.getWorkDays(dayLength, startDate, endDate, person);

            Account holidaysAccount = getHolidaysAccount(yearOfStartDate, person);

            return holidaysAccount != null && calculateTotalLeftVacationDays(holidaysAccount).compareTo(workDays) >= 0;
        } else {
            // ensure that applying for leave for the period in the old year is possible
            BigDecimal workDaysInOldYear = calendarService.getWorkDays(dayLength, startDate,
                    DateUtil.getLastDayOfYear(yearOfStartDate), person);

            // ensure that applying for leave for the period in the new year is possible
            BigDecimal workDaysInNewYear = calendarService.getWorkDays(dayLength,
                    DateUtil.getFirstDayOfYear(yearOfEndDate), endDate, person);

            Account holidaysAccountForOldYear = getHolidaysAccount(yearOfStartDate, person);
            Account holidaysAccountForNewYear = getHolidaysAccount(yearOfEndDate, person);

            return holidaysAccountForOldYear != null && holidaysAccountForNewYear != null
                && calculateTotalLeftVacationDays(holidaysAccountForOldYear).compareTo(workDaysInOldYear) >= 0
                && calculateTotalLeftVacationDays(holidaysAccountForNewYear).compareTo(workDaysInNewYear) >= 0;
        }
    }


    private Account getHolidaysAccount(int year, Person person) {

        Account holidaysAccount = accountService.getHolidaysAccount(year, person);

        if (holidaysAccount == null) {
            Account lastYearsHolidaysAccount = accountService.getHolidaysAccount(year - 1, person);

            holidaysAccount = accountInteractionService.createHolidaysAccount(person, DateUtil.getFirstDayOfYear(year),
                    DateUtil.getLastDayOfYear(year), lastYearsHolidaysAccount.getAnnualVacationDays(), BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }

        return holidaysAccount;
    }


    /**
     * Calculates the total number of days that are left to be used for applying for leave.
     *
     * <p>NOTE: The calculation depends on the current date. If it's before April, the left remaining vacation days are
     * relevant for calculation and if it's after April, only the not expiring remaining vacation days are relevant for
     * calculation.</p>
     *
     * @param  account {@link Account}
     *
     * @return  total number of left vacation days
     */
    public BigDecimal calculateTotalLeftVacationDays(Account account) {

        VacationDaysLeft vacationDaysLeft = getVacationDaysLeft(account);

        // it's before April - the left remaining vacation days must be used
        if (DateUtil.isBeforeApril(DateMidnight.now())) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            // it's after April - only the left not expiring remaining vacation days must be used
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
    }


    public VacationDaysLeft getVacationDaysLeft(Account account) {

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getUsedDaysBeforeApril(account);
        BigDecimal daysAfterApril = getUsedDaysAfterApril(account);

        return VacationDaysLeft.builder().withAnnualVacation(vacationDays).withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring).forUsedDaysBeforeApril(daysBeforeApril)
            .forUsedDaysAfterApril(daysAfterApril).get();
    }


    BigDecimal getUsedDaysBeforeApril(Account account) {

        DateMidnight firstOfJanuary = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.JANUARY);
        DateMidnight lastOfMarch = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.MARCH);

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
    }


    BigDecimal getUsedDaysAfterApril(Account account) {

        DateMidnight firstOfApril = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.APRIL);
        DateMidnight lastOfDecember = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.DECEMBER);

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastOfDecember);
    }


    BigDecimal getUsedDaysBetweenTwoMilestones(Person person, DateMidnight firstMilestone, DateMidnight lastMilestone) {

        // get all applications for leave
        List<Application> allApplicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(
                firstMilestone, lastMilestone, person);

        // filter them since only waiting and allowed applications for leave of type holiday are relevant
        List<Application> applicationsForLeave = FluentIterable.from(allApplicationsForLeave).filter(
                new Predicate<Application>() {

                    @Override
                    public boolean apply(Application input) {

                        return input.getVacationType() == VacationType.HOLIDAY
                            && (input.hasStatus(ApplicationStatus.WAITING)
                                || input.hasStatus(ApplicationStatus.ALLOWED));
                    }
                }).toList();

        BigDecimal usedDays = BigDecimal.ZERO;

        for (Application applicationForLeave : applicationsForLeave) {
            DateMidnight startDate = applicationForLeave.getStartDate();
            DateMidnight endDate = applicationForLeave.getEndDate();

            if (startDate.isBefore(firstMilestone)) {
                startDate = firstMilestone;
            }

            if (endDate.isAfter(lastMilestone)) {
                endDate = lastMilestone;
            }

            usedDays = usedDays.add(calendarService.getWorkDays(applicationForLeave.getHowLong(), startDate, endDate,
                        person));
        }

        return usedDays;
    }
}
