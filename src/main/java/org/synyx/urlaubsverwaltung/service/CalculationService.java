package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.util.List;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;

/**
 * This service calculates if a person may apply for leave, i.e. if he/she has enough vacation days to apply for leave.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class CalculationService {

    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private OwnCalendarService calendarService;

    @Autowired
    public CalculationService(ApplicationDAO applicationDAO, HolidaysAccountService accountService, OwnCalendarService calendarService) {
        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
        this.calendarService = calendarService;
    }

    /**
     * check if application is valid and may be send to boss to be allowed or rejected or if person's leave account has
     * too little residual number of vacation days, so that taking holiday isn't possible
     *
     * @param  application
     *
     * @return  boolean: true if application is okay, false if there are too little residual number of vacation days
     */
    public boolean checkApplication(Application application) {

        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {

            Person person = application.getPerson();
            int startYear = application.getStartDate().getYear();
            int endYear = application.getEndDate().getYear();

            Application tmp1 = new Application();
            tmp1.setStartDate(application.getStartDate());
            tmp1.setEndDate(new DateMidnight(startYear, DateTimeConstants.DECEMBER, 31));
            tmp1.setPerson(person);
            tmp1.setHowLong(application.getHowLong());
            tmp1.setDays(calendarService.getVacationDays(tmp1, application.getStartDate(), new DateMidnight(startYear, DateTimeConstants.DECEMBER, 31)));

            Application tmp2 = new Application();
            tmp2.setStartDate(new DateMidnight(endYear, DateTimeConstants.JANUARY, 1));
            tmp2.setEndDate(application.getEndDate());
            tmp2.setPerson(person);
            tmp2.setHowLong(application.getHowLong());
            tmp2.setDays(calendarService.getVacationDays(application, new DateMidnight(endYear, DateTimeConstants.JANUARY, 1), application.getEndDate()));

            if (accountService.getHolidaysAccount(startYear, person) == null) {
                // this may happen if someone applies for leave for the past year and there is set no account information
                // in this case just check if there are enough vacation days for this year
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

        Person person = application.getPerson();
        int year = application.getStartDate().getYear();

        DateMidnight firstOfJanuary = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastOfMarch = new DateMidnight(year, DateTimeConstants.MARCH, 31);
        DateMidnight firstOfApril = new DateMidnight(year, DateTimeConstants.APRIL, 1);
        DateMidnight lastOfDecember = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        Account account = getOrCreateNewAccount(year, person);

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();

        BigDecimal daysBeforeApril = getDaysBetweenTwoMilestones(person, firstOfJanuary, lastOfMarch);
        BigDecimal daysAfterApril = getDaysBetweenTwoMilestones(person, firstOfApril, lastOfDecember);

        BigDecimal result = remainingVacationDays.subtract(daysBeforeApril);

        if (result.compareTo(BigDecimal.ZERO) == 0) {
            remainingVacationDays = BigDecimal.ZERO;
        } else if (result.compareTo(BigDecimal.ZERO) > 0) {
            remainingVacationDays = result;
        } else if (result.compareTo(BigDecimal.ZERO) < 0) {
            remainingVacationDays = BigDecimal.ZERO;
            vacationDays = vacationDays.add(result); // result is negative so that you add it to vacation days instead of subtract it
        }

        if (account.isRemainingVacationDaysExpire()) {

            vacationDays = vacationDays.subtract(daysAfterApril);

        } else {
            // if the remaining vacation days do not expire, you just can see it as sum
            vacationDays = vacationDays.add(remainingVacationDays);
            vacationDays = vacationDays.subtract(daysAfterApril);

        }

        if (vacationDays.compareTo(application.getDays()) >= 0) {
            return true;
        }

        return false;

    }

    protected Account getOrCreateNewAccount(int year, Person person) {

        Account account = accountService.getHolidaysAccount(year, person);

        if (account == null) {
            Account lastYearsAccount = accountService.getHolidaysAccount(year - 1, person);
            accountService.createHolidaysAccount(person, new DateMidnight(year, DateTimeConstants.JANUARY, 1),
                    new DateMidnight(year, DateTimeConstants.DECEMBER, 31),
                    lastYearsAccount.getAnnualVacationDays(), BigDecimal.ZERO, lastYearsAccount.isRemainingVacationDaysExpire());
            account = accountService.getHolidaysAccount(year, person);
        }

        return account;

    }

    protected BigDecimal getDaysBetweenTwoMilestones(Person person, DateMidnight firstMilestone, DateMidnight lastMilestone) {

        List<Application> applicationsBetweenMilestones =
                applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(), lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED);

        List<Application> applicationsBetweenMilestonesSpanningFirstMilestone =
                applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(), lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED);
        List<Application> applicationsBetweenMilestonesSpanningLastMilestone =
                applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(), lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED);

        BigDecimal days = BigDecimal.ZERO;

        for (Application a : applicationsBetweenMilestones) {
            days = days.add(a.getDays());
        }

        for (Application a : applicationsBetweenMilestonesSpanningFirstMilestone) {
            days = days.add(calendarService.getVacationDays(a, firstMilestone, a.getEndDate()));
        }

        for (Application a : applicationsBetweenMilestonesSpanningLastMilestone) {
            days = days.add(calendarService.getVacationDays(a, a.getStartDate(), lastMilestone));
        }

        return days;

    }
}
