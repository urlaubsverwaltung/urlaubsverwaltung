package org.synyx.urlaubsverwaltung.core.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;

import java.math.BigDecimal;

import java.util.Optional;


/**
 * This service calculates if a {@link Person} may apply for leave, i.e. if he/she has enough vacation days to apply for
 * leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class CalculationService {

    private static final Logger LOG = LoggerFactory.getLogger(CalculationService.class);


    private final VacationDaysService vacationDaysService;
    private final AccountInteractionService accountInteractionService;
    private final AccountService accountService;
    private final WorkDaysService calendarService;

    @Autowired
    public CalculationService(VacationDaysService vacationDaysService, AccountService accountService,
        AccountInteractionService accountInteractionService, WorkDaysService calendarService) {

        this.vacationDaysService = vacationDaysService;
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

        DayLength dayLength = application.getDayLength();

        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        int yearOfStartDate = startDate.getYear();
        int yearOfEndDate = endDate.getYear();

        if (yearOfStartDate == yearOfEndDate) {
            BigDecimal workDays = calendarService.getWorkDays(dayLength, startDate, endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDays);
        } else {
            // ensure that applying for leave for the period in the old year is possible
            BigDecimal workDaysInOldYear = calendarService.getWorkDays(dayLength, startDate,
                    DateUtil.getLastDayOfYear(yearOfStartDate), person);

            // ensure that applying for leave for the period in the new year is possible
            BigDecimal workDaysInNewYear = calendarService.getWorkDays(dayLength,
                    DateUtil.getFirstDayOfYear(yearOfEndDate), endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDaysInOldYear)
                && accountHasEnoughVacationDaysLeft(person, yearOfEndDate, workDaysInNewYear);
        }
    }


    private boolean accountHasEnoughVacationDaysLeft(Person person, int year, BigDecimal workDays) {
        Optional<Account> account = getHolidaysAccount(year, person);
        if (!account.isPresent()){
            return false;
        }



        // we also need to look at the next year, because "remaining days" from this year
        // may already have been booked then

        BigDecimal alreadyUsedNextYear = BigDecimal.ZERO;
        // call accountService directly to avoid auto-creating a new account for next year
        Optional<Account> nextYear = accountService.getHolidaysAccount(year+1, person);
        if (nextYear.isPresent()){
            Account nextAccount = nextYear.get();
            if (nextAccount.getRemainingVacationDays().signum() > 0){
                VacationDaysLeft nextYearLeft = vacationDaysService.getVacationDaysLeft(nextAccount, Optional.empty());
                BigDecimal totalUsedNextYear = nextAccount.getVacationDays()
                        .add(nextAccount.getRemainingVacationDays())
                        .subtract(nextYearLeft.getVacationDays())
                        .subtract(nextYearLeft.getRemainingVacationDays());
                BigDecimal remainingUsedNextYear = totalUsedNextYear.subtract(nextAccount.getVacationDays());
                if (remainingUsedNextYear.signum() > 0){
                    alreadyUsedNextYear = remainingUsedNextYear;
                }
            }
        }


        if(vacationDaysService.calculateTotalLeftVacationDays(account.get()).subtract(workDays).compareTo(alreadyUsedNextYear) >= 0) {
            return true;
        } else {
            if (alreadyUsedNextYear.signum() > 0) {
                LOG.info("Rejecting application by {} for {} days in {} because {} remaining days have already been used in {}",
                        person, workDays, year, alreadyUsedNextYear, year+1);
            }
            return false;
        }
    }


    private Optional<Account> getHolidaysAccount(int year, Person person) {

        Optional<Account> holidaysAccount = accountService.getHolidaysAccount(year, person);

        if (holidaysAccount.isPresent()) {
            return holidaysAccount;
        }

        Optional<Account> lastYearsHolidaysAccount = accountService.getHolidaysAccount(year - 1, person);

        if (!lastYearsHolidaysAccount.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(
                    lastYearsHolidaysAccount.get()));
    }
}
