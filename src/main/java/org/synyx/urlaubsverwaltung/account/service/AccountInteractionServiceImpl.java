package org.synyx.urlaubsverwaltung.account.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
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
    private final Clock clock;

    //TODO: extract application property
    private static final int DEFAULT_ANNUAL_VACATION_DAYS = 20;
    private static final double VACATION_DAYS_PER_MONTH = ((double) DEFAULT_ANNUAL_VACATION_DAYS) / 12;

    @Autowired
    AccountInteractionServiceImpl(AccountService accountService, VacationDaysService vacationDaysService, Clock clock) {

        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.clock = clock;
    }

    @Override
    public void createDefaultAccount(Person person) {

        LocalDate today = LocalDate.now(clock);

        BigDecimal remainingVacationDaysForThisYear = new BigDecimal(
            VACATION_DAYS_PER_MONTH * today.getMonthValue(), // calculate remaining vacation days starting from todays month
            new MathContext(0, RoundingMode.CEILING)
        );
        BigDecimal noRemainingVacationDaysForLastYear = BigDecimal.ZERO;

        this.updateOrCreateHolidaysAccount(
            person,
            today, // from today...
            today.with(lastDayOfYear()), //...until end of year
            BigDecimal.valueOf(DEFAULT_ANNUAL_VACATION_DAYS), // assuming germany as default: http://www.gesetze-im-internet.de/burlg/__3.html
            remainingVacationDaysForThisYear,
            noRemainingVacationDaysForLastYear,
            noRemainingVacationDaysForLastYear,
            "");
    }

    @Override
    public Account updateOrCreateHolidaysAccount(Person person, LocalDate validFrom, LocalDate validTo,
                                                 BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                                 BigDecimal remainingDays, BigDecimal remainingDaysNotExpiring,
                                                 String comment) {

        Optional<Account> optionalAccount = accountService.getHolidaysAccount(validFrom.getYear(), person);
        Account account;

        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
            account.setAnnualVacationDays(annualVacationDays);
            account.setRemainingVacationDays(remainingDays);
            account.setRemainingVacationDaysNotExpiring(remainingDaysNotExpiring);
            account.setComment(comment);
        } else {
            account = new Account(person, validFrom, validTo, annualVacationDays, remainingDays,
                remainingDaysNotExpiring, comment);
        }

        account.setVacationDays(actualVacationDays);

        final Account savedAccount = accountService.save(account);

        LOG.info("Created holidays account: {}", savedAccount);

        return savedAccount;
    }

    @Override
    public Account editHolidaysAccount(Account account, LocalDate validFrom, LocalDate validTo,
                                       BigDecimal annualVacationDays, BigDecimal actualVacationDays, BigDecimal remainingDays,
                                       BigDecimal remainingDaysNotExpiring, String comment) {

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(annualVacationDays);
        account.setVacationDays(actualVacationDays);
        account.setRemainingVacationDays(remainingDays);
        account.setRemainingVacationDaysNotExpiring(remainingDaysNotExpiring);
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
            Optional<Account> nextYearsHolidaysAccountOptional = accountService.getHolidaysAccount(nextYear, person);

            if (nextYearsHolidaysAccountOptional.isPresent()) {
                final Optional<Account> holidaysAccount = accountService.getHolidaysAccount(startYear, person);

                if (holidaysAccount.isPresent()) {
                    Account changedHolidaysAccount = holidaysAccount.get();
                    Account nextYearsHolidaysAccount = nextYearsHolidaysAccountOptional.get();

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

    @Override
    public Account autoCreateOrUpdateNextYearsHolidaysAccount(Account referenceAccount) {

        int nextYear = referenceAccount.getYear() + 1;

        Optional<Account> nextYearAccountOptional = accountService.getHolidaysAccount(nextYear,
            referenceAccount.getPerson());

        if (nextYearAccountOptional.isPresent()) {
            Account nextYearAccount = nextYearAccountOptional.get();
            updateRemainingVacationDays(nextYearAccount, referenceAccount);

            LOG.info("Updated existing holidays account for {}: {}", nextYear, nextYearAccount);

            return nextYearAccount;
        }

        BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(referenceAccount);

        return updateOrCreateHolidaysAccount(referenceAccount.getPerson(), DateUtil.getFirstDayOfYear(nextYear),
            DateUtil.getLastDayOfYear(nextYear), referenceAccount.getAnnualVacationDays(),
            referenceAccount.getAnnualVacationDays(), leftVacationDays, BigDecimal.ZERO, referenceAccount.getComment());
    }
}
