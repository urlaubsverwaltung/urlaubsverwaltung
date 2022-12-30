package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.Month.APRIL;
import static java.util.stream.Collectors.toList;

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
        return getHolidaysAccount(year, List.of(person)).stream().findFirst();
    }

    @Override
    public AccountDraft createHolidaysAccountDraft(int year, Person person) {
        final Optional<Account> prevYearAccount = getHolidaysAccount(year - 1, person);

        final boolean expiresGlobally = remainingVacationDaysExpireGlobally();
        final Boolean expiresLocally = prevYearAccount.map(Account::isDoRemainingVacationDaysExpireLocally).orElse(null);

        final LocalDate defaultExpiryDate = LocalDate.of(year, APRIL, 1);

        final LocalDate expiryDate = (expiresLocally != null && expiresLocally) || (expiresLocally == null && expiresGlobally)
                ? prevYearAccount.map(Account::getExpiryDate).map(date -> date.withYear(year)).orElse(defaultExpiryDate)
                : defaultExpiryDate;

        return AccountDraft.builder()
                .person(person)
                .year(Year.of(year))
                .annualVacationDays(prevYearAccount.map(Account::getAnnualVacationDays).orElse(null))
                .doRemainingVacationDaysExpireLocally(expiresLocally)
                .doRemainingVacationDaysExpireGlobally(expiresGlobally)
                .expiryDate(expiryDate)
                .setRemainingVacationDaysNotExpiring(prevYearAccount.map(Account::getRemainingVacationDaysNotExpiring).orElse(null))
                .build();
    }

    @Override
    public List<Account> getHolidaysAccount(int year, List<Person> persons) {
        final CachedSupplier<Boolean> expireGlobally = new CachedSupplier<>(this::remainingVacationDaysExpireGlobally);

        return accountRepository.findAccountByYearAndPersons(year, persons)
            .stream()
            .map(accountEntity -> this.mapToAccount(accountEntity, expireGlobally.get()))
            .collect(toList());
    }

    @Override
    public Account save(Account account) {
        final AccountEntity accountEntity = mapToAccountEntity(account);
        final AccountEntity savedAccountEntity = accountRepository.save(accountEntity);
        return mapToAccount(savedAccountEntity, remainingVacationDaysExpireGlobally());
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

    @Override
    public void deleteAllByPerson(Person person) {
        accountRepository.deleteByPerson(person);
    }

    private boolean remainingVacationDaysExpireGlobally() {
        return settingsService.getSettings().getAccountSettings().isDoRemainingVacationDaysExpireGlobally();
    }

    private static class CachedSupplier<T> implements Supplier<T> {
        private T cachedValue;
        private final Supplier<T> supplier;

        CachedSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (cachedValue == null) {
                cachedValue = supplier.get();
            }
            return cachedValue;
        }
    }
}
