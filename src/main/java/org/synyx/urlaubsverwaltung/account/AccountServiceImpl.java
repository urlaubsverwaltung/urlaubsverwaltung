package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

/**
 * Implementation of {@link AccountService}.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> getHolidaysAccount(int year, Person person) {
        return accountRepository.getHolidaysAccountByYearAndPerson(year, person).map(this::mapToAccount);
    }

    @Override
    public Account save(Account account) {
        final AccountEntity accountEntity = mapToAccountEntity(account);
        final AccountEntity savedAccountEntity = accountRepository.save(accountEntity);
        return mapToAccount(savedAccountEntity);
    }

    private Account mapToAccount(AccountEntity accountEntity) {
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
        return account;
    }

    private AccountEntity mapToAccountEntity(Account account) {
        final AccountEntity accountEntity = new AccountEntity(
            account.getPerson(),
            account.getValidFrom(),
            account.getValidTo(),
            account.isDoRemainingVacationDaysExpire(),
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
