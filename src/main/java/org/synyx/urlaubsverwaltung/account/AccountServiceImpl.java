package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Optional;

/**
 * Implementation of {@link AccountService}.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final SettingsService settingsService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, SettingsService settingsService) {
        this.accountRepository = accountRepository;
        this.settingsService = settingsService;
    }

    @Override
    public Optional<Account> getHolidaysAccount(int year, Person person) {
        final boolean doRemainingVacationDaysExpireGlobally = settingsService.getSettings().getAccountSettings().isDoRemainingVacationDaysExpireGlobally();
        return accountRepository.getHolidaysAccountByYearAndPerson(year, person).map(account -> mapToAccount(account, doRemainingVacationDaysExpireGlobally));
    }

    @Override
    public Account save(Account account) {
        final AccountEntity accountEntity = mapToAccountEntity(account);
        final AccountEntity savedAccountEntity = accountRepository.save(accountEntity);
        final boolean doRemainingVacationDaysExpireGlobally = settingsService.getSettings().getAccountSettings().isDoRemainingVacationDaysExpireGlobally();
        return mapToAccount(savedAccountEntity, doRemainingVacationDaysExpireGlobally);
    }

    private Account mapToAccount(AccountEntity accountEntity, boolean doRemainingVacationDaysExpireGlobally) {
        final Account account = new Account(
            accountEntity.getPerson(),
            accountEntity.getValidFrom(),
            accountEntity.getValidTo(),
            accountEntity.isDoRemainingVacationDaysExpire(),
            accountEntity.getExpiryDate(),
            accountEntity.getAnnualVacationDays(),
            accountEntity.getRemainingVacationDays(),
            accountEntity.getRemainingVacationDaysNotExpiring(),
            accountEntity.getComment()
        );
        account.setId(accountEntity.getId());
        account.setExpiryNotificationSentDate(accountEntity.getExpiryNotificationSentDate());
        account.setActualVacationDays(accountEntity.getActualVacationDays());
        account.setDoRemainingVacationDaysExpireGlobally(doRemainingVacationDaysExpireGlobally);
        return account;
    }

    private AccountEntity mapToAccountEntity(Account account) {
        final AccountEntity accountEntity = new AccountEntity(
            account.getPerson(),
            account.getValidFrom(),
            account.getValidTo(),
            account.isDoRemainingVacationDaysExpireLocally(),
            account.getExpiryDate(),
            account.getAnnualVacationDays(),
            account.getRemainingVacationDays(),
            account.getRemainingVacationDaysNotExpiring(),
            account.getComment()
        );
        accountEntity.setId(account.getId());
        accountEntity.setExpiryNotificationSentDate(account.getExpiryNotificationSentDate());
        accountEntity.setActualVacationDays(account.getActualVacationDays());
        return accountEntity;
    }
}
