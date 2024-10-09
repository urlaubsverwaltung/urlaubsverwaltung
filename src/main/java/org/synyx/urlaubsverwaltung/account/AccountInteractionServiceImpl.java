package org.synyx.urlaubsverwaltung.account;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of interface {@link AccountInteractionService}.
 */
@Service
@Transactional
class AccountInteractionServiceImpl implements AccountInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    AccountInteractionServiceImpl(AccountService accountService, VacationDaysService vacationDaysService,
                                  SettingsService settingsService, Clock clock) {
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public void createDefaultAccount(Person person) {

        final LocalDate today = LocalDate.now(clock);
        final Integer defaultVacationDays = settingsService.getSettings().getAccountSettings().getDefaultVacationDays();
        final BigDecimal remainingVacationDaysForThisYear = getRemainingVacationDaysForThisYear(today.with(firstDayOfMonth()), defaultVacationDays);

        updateOrCreateHolidaysAccount(
            person,
            today.with(firstDayOfYear()), //from the first day of year...
            today.with(lastDayOfYear()), //...until end of year
            null,
            null,
            BigDecimal.valueOf(defaultVacationDays),
            remainingVacationDaysForThisYear,
            ZERO, // For initial user creation there are no remaining vacation days from last year
            ZERO,
            "");
    }

    @Override
    public Account updateOrCreateHolidaysAccount(Person person, LocalDate validFrom, LocalDate validTo,
                                                 Boolean doRemainingVacationDaysExpireLocally, @Nullable LocalDate expiryDate,
                                                 BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                                 BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring,
                                                 String comment) {

        remainingVacationDays = requireNonNullElse(remainingVacationDays, ZERO);
        remainingVacationDaysNotExpiring = requireNonNullElse(remainingVacationDaysNotExpiring, ZERO);

        final Account account;
        final Optional<Account> optionalAccount = accountService.getHolidaysAccount(validFrom.getYear(), person);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
            account.setDoRemainingVacationDaysExpireLocally(doRemainingVacationDaysExpireLocally);
            account.setExpiryDateLocally(expiryDate);
            account.setAnnualVacationDays(annualVacationDays);
            account.setRemainingVacationDays(remainingVacationDays);
            account.setRemainingVacationDaysNotExpiring(remainingVacationDaysNotExpiring);
            account.setComment(comment);
        } else {
            account = new Account(person, validFrom, validTo, doRemainingVacationDaysExpireLocally,
                expiryDate, annualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        }

        account.setActualVacationDays(actualVacationDays);

        final Account savedAccount = accountService.save(account);

        LOG.info("Created holidays account: {}", savedAccount);

        return savedAccount;
    }

    @Override
    public Account editHolidaysAccount(Account account, LocalDate validFrom, LocalDate validTo, Boolean doRemainingVacationDaysExpireLocally,
                                       @Nullable LocalDate expiryDateLocally, BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                       BigDecimal remainingVacationDays, @Nullable BigDecimal remainingVacationDaysNotExpiring, String comment) {

        remainingVacationDaysNotExpiring = requireNonNullElseGet(remainingVacationDaysNotExpiring, account::getRemainingVacationDaysNotExpiring);

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setDoRemainingVacationDaysExpireLocally(doRemainingVacationDaysExpireLocally);
        account.setExpiryDateLocally(expiryDateLocally);
        account.setAnnualVacationDays(annualVacationDays);
        account.setActualVacationDays(actualVacationDays);
        account.setRemainingVacationDays(remainingVacationDays);
        account.setRemainingVacationDaysNotExpiring(remainingVacationDaysNotExpiring);
        account.setComment(comment);

        final Account savedAccount = accountService.save(account);
        LOG.info("Updated holidays account: {}", savedAccount);
        return savedAccount;
    }

    @Override
    public void updateRemainingVacationDays(int year, Person person) {

        int startYear = year;

        boolean hasNextAccount = true;

        while (hasNextAccount) {
            final int nextYear = startYear + 1;
            final Optional<Account> nextYearsHolidaysAccountOptional = accountService.getHolidaysAccount(nextYear, person);

            if (nextYearsHolidaysAccountOptional.isPresent()) {
                final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(startYear, person);

                if (holidaysAccount.isPresent()) {
                    final Account changedHolidaysAccount = holidaysAccount.get();
                    final Account nextYearsHolidaysAccount = nextYearsHolidaysAccountOptional.get();

                    updateRemainingVacationDays(nextYearsHolidaysAccount, changedHolidaysAccount);

                    LOG.info("Updated remaining vacation days of holidays account: {}", nextYearsHolidaysAccount);

                    startYear++;

                } else {
                    hasNextAccount = false;
                }
            } else {
                hasNextAccount = false;
            }
        }
    }

    @Override
    public void deleteAllByPerson(Person person) {
        accountService.deleteAllByPerson(person);
    }

    @Override
    public Account autoCreateOrUpdateNextYearsHolidaysAccount(Account referenceAccount) {

        final int nextYear = referenceAccount.getYear() + 1;

        final Optional<Account> nextYearAccountOptional = accountService.getHolidaysAccount(nextYear, referenceAccount.getPerson());
        if (nextYearAccountOptional.isPresent()) {
            final Account nextYearAccount = nextYearAccountOptional.get();
            updateRemainingVacationDays(nextYearAccount, referenceAccount);

            LOG.info("Updated existing holidays account for {}: {}", nextYear, nextYearAccount);

            return nextYearAccount;
        }

        final LocalDate validFrom = Year.of(nextYear).atDay(1);
        final LocalDate validTo = validFrom.with(lastDayOfYear());
        final Boolean doRemainingVacationDaysExpireLocally = referenceAccount.isDoRemainingVacationDaysExpireLocally();
        final LocalDate expiryDateLocally = referenceAccount.getExpiryDateLocally() == null ? null : referenceAccount.getExpiryDateLocally().withYear(nextYear);
        final BigDecimal remainingVacationDays = vacationDaysService.getTotalLeftVacationDays(referenceAccount);

        return updateOrCreateHolidaysAccount(
            referenceAccount.getPerson(),
            validFrom,
            validTo,
            doRemainingVacationDaysExpireLocally,
            expiryDateLocally,
            referenceAccount.getAnnualVacationDays(),
            referenceAccount.getAnnualVacationDays(),
            remainingVacationDays,
            ZERO,
            referenceAccount.getComment()
        );
    }

    /**
     * calculate remaining vacation days starting from today's month, round to ceiling
     */
    private BigDecimal getRemainingVacationDaysForThisYear(LocalDate today, Integer defaultVacationDays) {
        final double vacationDaysPerMonth = ((double) defaultVacationDays) / 12;
        final int remainingMonthForThisYear = 12 - (today.getMonthValue() - 1);
        return new BigDecimal((int) Math.ceil(vacationDaysPerMonth * remainingMonthForThisYear));
    }

    /**
     * Updates the remaining vacation days of the given new account by using data of the given last account.
     *
     * @param newAccount  to calculate and update remaining vacation days for
     * @param lastAccount as reference to be used for calculation of remaining vacation days
     */
    private void updateRemainingVacationDays(Account newAccount, Account lastAccount) {

        final BigDecimal leftVacationDays = vacationDaysService.getTotalLeftVacationDays(lastAccount);
        newAccount.setRemainingVacationDays(leftVacationDays);

        // number of not expiring remaining vacation days is greater than remaining vacation days
        if (newAccount.getRemainingVacationDaysNotExpiring().compareTo(leftVacationDays) > 0) {
            newAccount.setRemainingVacationDaysNotExpiring(leftVacationDays);
        }

        accountService.save(newAccount);
    }
}
