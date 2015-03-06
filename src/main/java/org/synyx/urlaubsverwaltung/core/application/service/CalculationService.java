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
     * Checks if the given {@link Application} is valid and may be send to boss to be allowed or rejected or if
     * {@link Person}'s {@link Account} has too little residual number of vacation days, so that taking holiday isn't
     * possible.
     *
     * @param  application {@link Application}
     *
     * @return  boolean: true if {@link Application} is okay, false if there are too little residual number of vacation
     *          days
     */
    public boolean checkApplication(Application application) {

        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
            Person person = application.getPerson();
            int startYear = application.getStartDate().getYear();
            int endYear = application.getEndDate().getYear();
            DateMidnight lastDayOfOldYear = DateUtil.getLastDayOfYear(application.getStartDate().getYear());
            DateMidnight firstDayOfNewYear = DateUtil.getFirstDayOfYear(endYear);

            Application tmp1 = new Application();
            tmp1.setStartDate(application.getStartDate());
            tmp1.setEndDate(lastDayOfOldYear);
            tmp1.setPerson(person);
            tmp1.setHowLong(application.getHowLong());
            tmp1.setDays(calendarService.getWorkDays(tmp1.getHowLong(), application.getStartDate(), lastDayOfOldYear,
                    application.getPerson()));

            Application tmp2 = new Application();
            tmp2.setStartDate(firstDayOfNewYear);
            tmp2.setEndDate(application.getEndDate());
            tmp2.setPerson(person);
            tmp2.setHowLong(application.getHowLong());
            tmp2.setDays(calendarService.getWorkDays(application.getHowLong(), firstDayOfNewYear,
                    application.getEndDate(), application.getPerson()));

            if (accountService.getHolidaysAccount(startYear, person) == null) {
                /**
                 * NOTE: This may happen if someone applies for leave for the past year and there is no account.
                 * In this case just check if there are enough vacation days for this year.
                 */
                return checkIfThereAreEnoughVacationDays(tmp2);
            } else {
                // this is the normal case: someone applies for leave for the next year
                if (checkIfThereAreEnoughVacationDays(tmp1) && checkIfThereAreEnoughVacationDays(tmp2)) {
                    return true;
                }
            }
        } else {
            return checkIfThereAreEnoughVacationDays(application);
        }

        return false;
    }


    private boolean checkIfThereAreEnoughVacationDays(Application application) {

        Account account = accountService.getOrCreateNewAccount(application.getStartDate().getYear(),
                application.getPerson());

        BigDecimal vacationDays = calculateTotalLeftVacationDays(account);

        BigDecimal workDays = calendarService.getWorkDays(application.getHowLong(), application.getStartDate(),
                application.getEndDate(), application.getPerson());

        return vacationDays.compareTo(workDays) >= 0;
    }


    /**
     * Calculates how many days the {@link Person} may apply for leave, i.e. how many vacation days + remaining vacation
     * days can be used for applying for leave.
     *
     * <p>NOTE: The calculation depends on the current date. If it's before April, the left remaining vacation days are
     * used, if it's after April, only the not expiring remaining vacation days are relevant for calculation.</p>
     *
     * @param  account {@link Account}
     *
     * @return  total left vacation days
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
