package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountService;
import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
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

    private final ApplicationDAO applicationDAO;
    private final AccountService accountService;
    private final OwnCalendarService calendarService;

    @Autowired
    public CalculationService(ApplicationDAO applicationDAO, AccountService accountService,
        OwnCalendarService calendarService) {

        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
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

            holidaysAccount = accountService.createHolidaysAccount(person, DateUtil.getFirstDayOfYear(year),
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

        BigDecimal daysBeforeApril = getDaysBeforeApril(account);
        BigDecimal daysAfterApril = getDaysAfterApril(account);

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder().withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays).notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(daysBeforeApril).forUsedDaysAfterApril(daysAfterApril).get();

        // it's before April - the left remaining vacation days must be used
        if (DateUtil.isBeforeApril(DateMidnight.now())) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            // it's after April - only the left not expiring remaining vacation days must be used
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
    }


    /**
     * Returns the number of left vacation days (without remaining vacation days: for displaying)
     *
     * @param  account {@link Account}
     *
     * @return  number of left vacation days (without remaining vacation days)
     */
    public BigDecimal calculateLeftVacationDays(Account account) {

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getDaysBeforeApril(account);
        BigDecimal daysAfterApril = getDaysAfterApril(account);

        VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder().withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays).notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(daysBeforeApril).forUsedDaysAfterApril(daysAfterApril).get();

        return vacationDaysLeft.getVacationDays();
    }


    /**
     * Returns the number of left remaining vacation days.
     *
     * @param  account {@link Account}
     *
     * @return  number of left remaining vacation days
     */
    public BigDecimal calculateLeftRemainingVacationDays(Account account) {

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getDaysBeforeApril(account);
        BigDecimal daysAfterApril = getDaysAfterApril(account);

        VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder().withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays).notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(daysBeforeApril).forUsedDaysAfterApril(daysAfterApril).get();

        return vacationDaysLeft.getRemainingVacationDays();
    }


    protected BigDecimal getDaysBeforeApril(Account account) {

        DateMidnight firstOfJanuary = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.JANUARY);
        DateMidnight lastOfMarch = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.MARCH);

        return getDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
    }


    protected BigDecimal getDaysAfterApril(Account account) {

        DateMidnight firstOfApril = DateUtil.getFirstDayOfMonth(account.getYear(), DateTimeConstants.APRIL);
        DateMidnight lastOfDecember = DateUtil.getLastDayOfMonth(account.getYear(), DateTimeConstants.DECEMBER);

        return getDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastOfDecember);
    }


    protected BigDecimal getDaysBetweenTwoMilestones(Person person, DateMidnight firstMilestone,
        DateMidnight lastMilestone) {

        List<Application> applicationsBetweenMilestones = applicationDAO.getApplicationsBetweenTwoMilestones(person,
                firstMilestone.toDate(), lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.WAITING,
                ApplicationStatus.ALLOWED);

        List<Application> applicationsBetweenMilestonesSpanningFirstMilestone =
            applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(), lastMilestone.toDate(),
                VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED);
        List<Application> applicationsBetweenMilestonesSpanningLastMilestone =
            applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(), lastMilestone.toDate(),
                VacationType.HOLIDAY, ApplicationStatus.WAITING, ApplicationStatus.ALLOWED);

        BigDecimal days = BigDecimal.ZERO;

        for (Application a : applicationsBetweenMilestones) {
            days = days.add(a.getDays());
        }

        for (Application a : applicationsBetweenMilestonesSpanningFirstMilestone) {
            days = days.add(calendarService.getWorkDays(a.getHowLong(), firstMilestone, a.getEndDate(), a.getPerson()));
        }

        for (Application a : applicationsBetweenMilestonesSpanningLastMilestone) {
            days = days.add(calendarService.getWorkDays(a.getHowLong(), a.getStartDate(), lastMilestone,
                        a.getPerson()));
        }

        return days;
    }
}
