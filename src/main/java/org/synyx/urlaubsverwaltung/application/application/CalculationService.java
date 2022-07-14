package org.synyx.urlaubsverwaltung.application.application;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

/**
 * This service calculates if a {@link Person} may apply for leave, i.e. if the person
 * has enough vacation days to apply for leave.
 */
@Service
class CalculationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final VacationDaysService vacationDaysService;
    private final AccountInteractionService accountInteractionService;
    private final AccountService accountService;
    private final WorkDaysCountService workDaysCountService;
    private final OverlapService overlapService;
    private final ApplicationService applicationService;

    @Autowired
    CalculationService(VacationDaysService vacationDaysService, AccountService accountService,
                       AccountInteractionService accountInteractionService, WorkDaysCountService workDaysCountService,
                       OverlapService overlapService, ApplicationService applicationService) {
        this.vacationDaysService = vacationDaysService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.workDaysCountService = workDaysCountService;
        this.overlapService = overlapService;
        this.applicationService = applicationService;
    }

    /**
     * Checks if applying for leave is possible, i.e. there are enough vacation days left to be used for the given
     * {@link Application} for leave.
     *
     * @param application for leave to check
     * @return {@code true} if the {@link Application} for leave
     * may be saved because there are enough vacation days left, {@code false} else
     */
    boolean checkApplication(Application application) {

        final Person person = application.getPerson();
        final DayLength dayLength = application.getDayLength();
        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();
        final int yearOfStartDate = startDate.getYear();
        final int yearOfEndDate = endDate.getYear();

        final Optional<Application> maybeSavedApplication = getSavedApplicationForEditing(application);

        if (yearOfStartDate == yearOfEndDate) {
            final BigDecimal oldWorkDays = maybeSavedApplication.map(savedApplication -> workDaysCountService.getWorkDaysCount(savedApplication.getDayLength(), savedApplication.getStartDate(), savedApplication.getEndDate(), savedApplication.getPerson())).orElse(ZERO);
            final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person).subtract(oldWorkDays);
            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDays, application);
        } else {
            // ensure that applying for leave for the period in the old year is possible
            final BigDecimal oldWorkDaysInOldYear = maybeSavedApplication.map(savedApplication -> workDaysCountService.getWorkDaysCount(savedApplication.getDayLength(), savedApplication.getStartDate(), getLastDayOfYear(savedApplication.getStartDate().getYear()), savedApplication.getPerson())).orElse(ZERO);
            final BigDecimal workDaysInOldYear = workDaysCountService.getWorkDaysCount(dayLength, startDate, getLastDayOfYear(yearOfStartDate), person).subtract(oldWorkDaysInOldYear);

            // ensure that applying for leave for the period in the new year is possible
            final BigDecimal oldWorkDaysInNewYear = maybeSavedApplication.map(savedApplication -> workDaysCountService.getWorkDaysCount(savedApplication.getDayLength(), Year.of(savedApplication.getEndDate().getYear()).atDay(1), savedApplication.getEndDate(), savedApplication.getPerson())).orElse(ZERO);
            final BigDecimal workDaysInNewYear = workDaysCountService.getWorkDaysCount(dayLength, Year.of(yearOfEndDate).atDay(1), endDate, person).subtract(oldWorkDaysInNewYear);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDaysInOldYear, application)
                && accountHasEnoughVacationDaysLeft(person, yearOfEndDate, workDaysInNewYear, application);
        }
    }

    boolean accountHasEnoughVacationDaysLeft(Person person, int year, BigDecimal workDays, Application application) {

        if (workDays.signum() <= 0) {
            return true;
        }

        final Optional<Account> maybeAccount = getHolidaysAccount(year, person);
        if (maybeAccount.isEmpty()) {
            return false;
        }

        // we also need to look at the next year, because "remaining days" from this year may already have been booked then
        // call accountService directly to avoid auto-creating a new account for next year
        final Optional<Account> accountNextYear = accountService.getHolidaysAccount(year + 1, person);
        final BigDecimal vacationDaysAlreadyUsedNextYear = vacationDaysService.getUsedRemainingVacationDays(accountNextYear);

        final Account account = maybeAccount.get();
        final VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, accountNextYear);
        LOG.debug("vacation days left of years {} and {} are {} days", year, year + 1, vacationDaysLeft);

        // now we need to consider which remaining vacation days expire
        final BigDecimal vacationDaysRequestedBeforeExpiryDate = getWorkdaysBeforeExpiryDate(account, application);
        final BigDecimal vacationDaysLeftUntilExpiryDate = vacationDaysLeft.getVacationDays()
            .add(vacationDaysLeft.getRemainingVacationDays())
            .subtract(vacationDaysRequestedBeforeExpiryDate)
            .subtract(vacationDaysAlreadyUsedNextYear);

        final BigDecimal vacationDaysRequestedAfterExpiryDate = workDays.subtract(vacationDaysRequestedBeforeExpiryDate);
        final BigDecimal vacationDaysLeftAfterExpiryDate = getVacationDaysLeftAfterExpiryDate(vacationDaysLeft, vacationDaysLeftUntilExpiryDate, vacationDaysRequestedAfterExpiryDate);

        LOG.debug("vacation days left until expiry {} date are {} and after expiry date are {}", account.getExpiryDate(), vacationDaysLeftUntilExpiryDate, vacationDaysLeftAfterExpiryDate);
        if (vacationDaysLeftUntilExpiryDate.signum() < 0 || vacationDaysLeftAfterExpiryDate.signum() < 0) {
            if (vacationDaysAlreadyUsedNextYear.signum() > 0) {
                LOG.info("Rejecting application by {} for {} days in {} because {} remaining days " +
                    "have already been used in {}", person, workDays, year, vacationDaysAlreadyUsedNextYear, year + 1);
            }
            return false;
        }

        return true;
    }

    private BigDecimal getVacationDaysLeftAfterExpiryDate(VacationDaysLeft vacationDaysLeft, BigDecimal vacationDaysLeftUntilExpiryDate, BigDecimal vacationDaysRequestedAfterExpiryDate) {
        BigDecimal vacationDaysLeftAfterExpiryDate = ZERO;
        if (vacationDaysRequestedAfterExpiryDate.signum() > 0) {
            vacationDaysLeftAfterExpiryDate = vacationDaysLeftUntilExpiryDate
                .subtract(vacationDaysRequestedAfterExpiryDate)
                .subtract(vacationDaysLeft.getRemainingVacationDays())
                .add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
        return vacationDaysLeftAfterExpiryDate;
    }

    private BigDecimal getWorkdaysBeforeExpiryDate(Account account, Application application) {
        final List<DateRange> beforeExpiryDate = overlapService.getListOfOverlaps(
            Year.of(account.getYear()).atDay(1),
            account.getExpiryDate().minusDays(1),
            List.of(application),
            List.of()
        );

        return beforeExpiryDate.isEmpty() ? ZERO : calculateWorkDaysBeforeExpiryDate(application, beforeExpiryDate);
    }

    private BigDecimal calculateWorkDaysBeforeExpiryDate(Application application, List<DateRange> beforeExpiryDate) {
        final LocalDate start = beforeExpiryDate.get(0).getStartDate();
        final LocalDate end = beforeExpiryDate.get(0).getEndDate();
        return workDaysCountService.getWorkDaysCount(
            application.getDayLength(),
            LocalDate.of(start.getYear(), start.getMonth(), start.getDayOfMonth()),
            LocalDate.of(end.getYear(), end.getMonth(), end.getDayOfMonth()),
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

    private Optional<Application> getSavedApplicationForEditing(Application application) {
        Optional<Application> maybeSavedApplication = Optional.empty();
        if (application.getId() != null) {
            maybeSavedApplication = applicationService.getApplicationById(application.getId());
        }
        return maybeSavedApplication;
    }
}
