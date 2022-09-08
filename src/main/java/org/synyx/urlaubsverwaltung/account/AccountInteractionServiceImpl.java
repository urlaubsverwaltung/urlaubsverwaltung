package org.synyx.urlaubsverwaltung.account;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import static java.time.Month.APRIL;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of interface {@link AccountInteractionService}.
 */
@Service
@Transactional
class AccountInteractionServiceImpl implements AccountInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final AccountProperties accountProperties;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    AccountInteractionServiceImpl(AccountProperties accountProperties, AccountService accountService,
                                  VacationDaysService vacationDaysService, SettingsService settingsService, Clock clock) {
        this.accountProperties = accountProperties;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public void createDefaultAccount(Person person) {

        final LocalDate today = LocalDate.now(clock);

        final Integer propertiesDefaultVacationDays = accountProperties.getDefaultVacationDays();
        final Integer settingsDefaultVacationDays = settingsService.getSettings().getAccountSettings().getDefaultVacationDays();

        final Integer defaultVacationDays;
        if (propertiesDefaultVacationDays == -1) {
            defaultVacationDays = settingsDefaultVacationDays;
        } else {
            defaultVacationDays = propertiesDefaultVacationDays;
        }

        final BigDecimal remainingVacationDaysForThisYear = getRemainingVacationDaysForThisYear(today.with(firstDayOfMonth()), defaultVacationDays);
        this.updateOrCreateHolidaysAccount(
            person,
            today.with(firstDayOfYear()), //from the first day of year...
            today.with(lastDayOfYear()), //...until end of year
            true,
            today.withMonth(APRIL.getValue()).with(firstDayOfMonth()), // default expiry date on first April
            BigDecimal.valueOf(defaultVacationDays),
            remainingVacationDaysForThisYear,
            ZERO, // For initial user creation there are no remaining vacation days from last year
            ZERO,
            "");
    }

    @Override
    public Account updateOrCreateHolidaysAccount(Person person, LocalDate validFrom, LocalDate validTo,
                                                 Boolean doRemainingVacationDaysExpire, LocalDate expiryDate,
                                                 BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                                 BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring,
                                                 String comment) {

        final Account account;
        final Optional<Account> optionalAccount = accountService.getHolidaysAccount(validFrom.getYear(), person);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
            account.setDoRemainingVacationDaysExpireLocally(doRemainingVacationDaysExpire);
            account.setExpiryDate(expiryDate);
            account.setAnnualVacationDays(annualVacationDays);
            account.setRemainingVacationDays(remainingVacationDays);
            account.setRemainingVacationDaysNotExpiring(remainingVacationDaysNotExpiring);
            account.setComment(comment);
        } else {
            account = new Account(person, validFrom, validTo, doRemainingVacationDaysExpire,
                expiryDate, annualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);
        }

        account.setActualVacationDays(actualVacationDays);

        final Account savedAccount = accountService.save(account);

        LOG.info("Created holidays account: {}", savedAccount);

        return savedAccount;
    }

    @Override
    public Account editHolidaysAccount(Account account, LocalDate validFrom, LocalDate validTo, Boolean doRemainingVacationDaysExpire,
                                       LocalDate expiryDate, BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                       BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring, String comment) {

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setDoRemainingVacationDaysExpireLocally(doRemainingVacationDaysExpire);
        account.setExpiryDate(expiryDate);
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
        final boolean doRemainingVacationDaysExpire = referenceAccount.doRemainigVacationDaysExpire();
        final LocalDate expiryDate = referenceAccount.getExpiryDate().withYear(nextYear);
        final BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(referenceAccount);

        return updateOrCreateHolidaysAccount(
            referenceAccount.getPerson(),
            validFrom,
            validTo,
            doRemainingVacationDaysExpire,
            expiryDate,
            referenceAccount.getAnnualVacationDays(),
            referenceAccount.getAnnualVacationDays(),
            leftVacationDays,
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

        final BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(lastAccount);
        newAccount.setRemainingVacationDays(leftVacationDays);

        // number of not expiring remaining vacation days is greater than remaining vacation days
        if (newAccount.getRemainingVacationDaysNotExpiring().compareTo(leftVacationDays) > 0) {
            newAccount.setRemainingVacationDaysNotExpiring(leftVacationDays);
        }

        accountService.save(newAccount);
    }
}
