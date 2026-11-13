package org.synyx.urlaubsverwaltung.application.application;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.slf4j.LoggerFactory.getLogger;

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

    @Autowired
    CalculationService(
        VacationDaysService vacationDaysService, AccountService accountService,
        AccountInteractionService accountInteractionService, WorkDaysCountService workDaysCountService
    ) {
        this.vacationDaysService = vacationDaysService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.workDaysCountService = workDaysCountService;
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

        if (yearOfStartDate == yearOfEndDate) {
            final BigDecimal workDays = workDaysCountService.getWorkDaysCount(dayLength, startDate, endDate, person);
            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDays, application);
        } else {
            // ensure that applying for leave for the period in the old year is possible
            final BigDecimal workDaysInOldYear = workDaysCountService.getWorkDaysCount(dayLength, startDate, startDate.with(lastDayOfYear()), person);

            // ensure that applying for leave for the period in the new year is possible
            final BigDecimal workDaysInNewYear = workDaysCountService.getWorkDaysCount(dayLength, Year.of(yearOfEndDate).atDay(1), endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDaysInOldYear, application)
                && accountHasEnoughVacationDaysLeft(person, yearOfEndDate, workDaysInNewYear, application);
        }
    }

    private boolean accountHasEnoughVacationDaysLeft(Person person, int year, BigDecimal requestedVacationDays, Application application) {

        if (requestedVacationDays.signum() <= 0) {
            return true;
        }

        final Optional<Account> maybeAccount = getHolidaysAccount(year, person);
        if (maybeAccount.isEmpty()) {
            return false;
        }

        final Account account = maybeAccount.get();
        final List<Account> holidayAccountsNextYear = accountService.getHolidaysAccount(year + 1, person).map(List::of).orElseGet(List::of);
        // an edited application is checked like a new one, so its saved version must not count as used
        final Map<Account, HolidayAccountVacationDays> accountHolidayAccountVacationDaysMap = vacationDaysService.getVacationDaysLeftIgnoring(application.getId(), List.of(account), Year.of(year), holidayAccountsNextYear);

        final VacationDaysLeft vacationDaysLeft = accountHolidayAccountVacationDaysMap.get(account).vacationDaysYear();
        final BigDecimal vacationDaysAlreadyUsedNextYear = vacationDaysLeft.getVacationDaysUsedNextYear();
        LOG.debug("vacation days left of years {} and {} are {} days", year, year + 1, vacationDaysLeft);

        // now we calculate the vacation days left before and after the expiry date
        final BigDecimal vacationDaysRequestedBeforeExpiryDate = getWorkdaysBeforeExpiryDate(account, application);
        final BigDecimal vacationDaysLeftBeforeExpiryDate = getVacationDaysLeftBeforeExpiryDate(vacationDaysLeft, vacationDaysRequestedBeforeExpiryDate);

        final BigDecimal vacationDaysRequestedAfterExpiryDate = requestedVacationDays.subtract(vacationDaysRequestedBeforeExpiryDate);
        final BigDecimal vacationDaysLeftAfterExpiryDate = getVacationDaysLeftAfterExpiryDate(account, vacationDaysLeft, vacationDaysRequestedBeforeExpiryDate, vacationDaysRequestedAfterExpiryDate);

        if (vacationDaysLeftBeforeExpiryDate.signum() < 0 || vacationDaysLeftAfterExpiryDate.signum() < 0) {
            LOG.info("Person with id {} does not have enough vacation days left to add/edit application with id {} from {} to {} because vacationDaysLeftBeforeExpiryDate {} is negative or vacationDaysLeftAfterExpiryDate {} is negative", person.getId(), application.getId(), application.getStartDate(), application.getEndDate(), vacationDaysLeftBeforeExpiryDate, vacationDaysLeftAfterExpiryDate);
            if (vacationDaysAlreadyUsedNextYear.signum() > 0) {
                LOG.info("Rejecting application by person with id {} for {} days in {} because {} remaining days have already been used in {}", person.getId(), requestedVacationDays, year, vacationDaysAlreadyUsedNextYear, year + 1);
            }
            return false;
        }

        return true;
    }

    // vacationDaysLeft.getVacationDays() already excludes the remaining vacation days used next year
    private static BigDecimal getVacationDaysLeftBeforeExpiryDate(VacationDaysLeft vacationDaysLeft, BigDecimal vacationDaysRequestedBeforeExpiryDate) {
        return vacationDaysLeft.getVacationDays()
            .add(vacationDaysLeft.getRemainingVacationDays())
            .subtract(vacationDaysRequestedBeforeExpiryDate);
    }

    private static BigDecimal getVacationDaysLeftAfterExpiryDate(Account account, VacationDaysLeft vacationDaysLeft, BigDecimal vacationDaysRequestedBeforeExpiryDate, BigDecimal vacationDaysRequestedAfterExpiryDate) {
        if (!account.doRemainingVacationDaysExpire() || vacationDaysRequestedAfterExpiryDate.signum() <= 0) {
            return ZERO;
        }

        return vacationDaysLeft.getVacationDays()
            .add(vacationDaysLeft.getRemainingVacationDaysNotExpiring())
            .subtract(vacationDaysRequestedBeforeExpiryDate)
            .subtract(vacationDaysRequestedAfterExpiryDate);
    }

    private BigDecimal getWorkdaysBeforeExpiryDate(Account account, Application application) {
        final LocalDate firstDayOfPeriod = Year.of(account.getYear()).atDay(1);
        final LocalDate lastDayOfPeriod = account.doRemainingVacationDaysExpire()
            ? account.getExpiryDate().minusDays(1)
            : firstDayOfPeriod.with(lastDayOfYear());

        if (lastDayOfPeriod.isBefore(firstDayOfPeriod)) {
            return ZERO;
        }

        final DateRange applicationDateRange = new DateRange(application.getStartDate(), application.getEndDate());
        final DateRange periodDateRange = new DateRange(firstDayOfPeriod, lastDayOfPeriod);

        return periodDateRange.overlap(applicationDateRange)
            .map(
                overlap -> workDaysCountService.getWorkDaysCount(
                    application.getDayLength(),
                    overlap.startDate(),
                    overlap.endDate(),
                    application.getPerson()
                )
            )
            .orElse(ZERO);
    }

    private Optional<Account> getHolidaysAccount(int year, Person person) {
        return accountService.getHolidaysAccount(year, person)
            .or(getHolidayAccountFromLastYear(year - 1, person));
    }

    private Supplier<Optional<? extends Account>> getHolidayAccountFromLastYear(int lastYear, Person person) {
        return () -> accountService.getHolidaysAccount(lastYear, person)
            .map(accountInteractionService::autoCreateOrUpdateNextYearsHolidaysAccount);
    }
}
