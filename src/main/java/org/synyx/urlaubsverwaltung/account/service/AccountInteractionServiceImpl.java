package org.synyx.urlaubsverwaltung.account.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
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

    @Autowired
    AccountInteractionServiceImpl(AccountService accountService, VacationDaysService vacationDaysService) {

        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
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

        accountService.save(account);

        LOG.info("Created holidays account: {}", account);

        return account;
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

        accountService.save(account);

        LOG.info("Updated holidays account: {}", account);

        return account;
    }

    @Override
    public void updateRemainingVacationDays(int year, Person person) {

        int startYear = year;

        boolean hasNextAccount = true;

        while (hasNextAccount) {
            Optional<Account> nextYearsHolidaysAccountOptional = accountService.getHolidaysAccount(startYear + 1,
                person);

            if (nextYearsHolidaysAccountOptional.isPresent()) {
                Account changedHolidaysAccount = accountService.getHolidaysAccount(startYear, person).get();
                Account nextYearsHolidaysAccount = nextYearsHolidaysAccountOptional.get();

                updateRemainingVacationDays(nextYearsHolidaysAccount, changedHolidaysAccount);

                LOG.info("Updated remaining vacation days of holidays account: {}", nextYearsHolidaysAccount);

                startYear++;
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

        BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(lastAccount);

        newAccount.setRemainingVacationDays(leftVacationDays);

        // number of not expiring remaining vacation days is greater than remaining vacation days
        if (newAccount.getRemainingVacationDaysNotExpiring().compareTo(leftVacationDays) == 1) {
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
