package org.synyx.urlaubsverwaltung.application.service;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.MARCH;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfMonth;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

/**
 * This service calculates if a {@link Person} may apply for leave, i.e. if he/she has enough vacation days to apply for
 * leave.
 */
@Service
public class CalculationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationDaysService vacationDaysService;
    private final AccountInteractionService accountInteractionService;
    private final AccountService accountService;
    private final WorkDaysCountService workDaysCountService;
    private final OverlapService overlapService;

    @Autowired
    public CalculationService(VacationDaysService vacationDaysService, AccountService accountService,
                              AccountInteractionService accountInteractionService, WorkDaysCountService workDaysCountService,
                              OverlapService overlapService) {
        this.vacationDaysService = vacationDaysService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.workDaysCountService = workDaysCountService;
        this.overlapService = overlapService;
    }

    /**
     * Checks if applying for leave is possible, i.e. there are enough vacation days left to be used for the given
     * {@link org.synyx.urlaubsverwaltung.application.domain.Application} for leave.
     *
     * @param application for leave to check
     * @return {@code true} if the {@link org.synyx.urlaubsverwaltung.application.domain.Application} for leave
     * may be saved because there are enough vacation days left, {@code false} else
     */
    public boolean checkApplication(Application application) {

        final Person person = application.getPerson();
        final DayLength dayLength = application.getDayLength();
        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();
        final int yearOfStartDate = startDate.getYear();
        final int yearOfEndDate = endDate.getYear();

        if (yearOfStartDate == yearOfEndDate) {
            final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDays, application);
        } else {
            // ensure that applying for leave for the period in the old year is possible
            final BigDecimal workDaysInOldYear = workDaysCountService.getWorkDaysCount(dayLength, startDate, getLastDayOfYear(yearOfStartDate), person);

            // ensure that applying for leave for the period in the new year is possible
            final BigDecimal workDaysInNewYear = workDaysCountService.getWorkDaysCount(dayLength, getFirstDayOfYear(yearOfEndDate), endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDaysInOldYear, application)
                && accountHasEnoughVacationDaysLeft(person, yearOfEndDate, workDaysInNewYear, application);
        }
    }

    private boolean accountHasEnoughVacationDaysLeft(Person person, int year, BigDecimal workDays, Application application) {

        final Optional<Account> account = getHolidaysAccount(year, person);
        if (account.isEmpty()) {
            return false;
        }

        // we also need to look at the next year, because "remaining days" from this year may already have been booked then
        // call accountService directly to avoid auto-creating a new account for next year
        final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
        final BigDecimal vacationDaysAlreadyUsedNextYear = vacationDaysService.getRemainingVacationDaysAlreadyUsed(accountNextYear);

        final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account.get(), accountNextYear);
        LOG.debug("vacation days left of years {} and {} are {} days", year, year + 1, vacationDaysLeft);

        // now we need to consider which remaining vacation days expire
        final BigDecimal vacationDaysRequestedBeforeApril = getWorkdaysBeforeApril(year, application);
        final BigDecimal vacationDaysLeftUntilApril = vacationDaysLeft.getVacationDays()
            .add(vacationDaysLeft.getRemainingVacationDays())
            .subtract(vacationDaysRequestedBeforeApril)
            .subtract(vacationDaysAlreadyUsedNextYear);

        final BigDecimal vacationDaysRequestedAfterApril = workDays.subtract(vacationDaysRequestedBeforeApril);
        final BigDecimal vacationDaysLeftAfterApril = vacationDaysLeftUntilApril
            .subtract(vacationDaysRequestedAfterApril)
            .subtract(vacationDaysLeft.getRemainingVacationDays())
            .add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        LOG.debug("vacation days left until april are {} and after april are {}", vacationDaysLeftUntilApril, vacationDaysLeftAfterApril);

        if (vacationDaysLeftUntilApril.signum() < 0 || vacationDaysLeftAfterApril.signum() < 0) {
            if (vacationDaysAlreadyUsedNextYear.signum() > 0) {
                LOG.info("Rejecting application by {} for {} days in {} because {} remaining days " +
                    "have already been used in {}", person, workDays, year, vacationDaysAlreadyUsedNextYear, year + 1);
            }
            return false;
        }

        return true;
    }

    private BigDecimal getWorkdaysBeforeApril(int year, Application application) {
        final List<Interval> beforeApril = overlapService.getListOfOverlaps(
            getFirstDayOfYear(year),
            getLastDayOfMonth(year, MARCH.getValue()),
            List.of(application),
            List.of()
        );

        return beforeApril.isEmpty() ? ZERO : calculateWorkDaysBeforeApril(application, beforeApril);
    }

    private BigDecimal calculateWorkDaysBeforeApril(Application application, List<Interval> beforeApril) {
        final DateTime start = beforeApril.get(0).getStart();
        final DateTime end = beforeApril.get(0).getEnd();
        return workDaysCountService.getWorkDaysCount(
            application.getDayLength(),
            LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()),
            LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()),
            application.getPerson());
    }

    private Optional<Account> getHolidaysAccount(int year, Person person) {

        final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(year, person);
        if (holidaysAccount.isPresent()) {
            return holidaysAccount;
        }

        final Optional<Account> lastYearsHolidaysAccount = accountService.getHolidaysAccount(year - 1, person);
        return lastYearsHolidaysAccount.map(accountInteractionService::autoCreateOrUpdateNextYearsHolidaysAccount);
    }
}
