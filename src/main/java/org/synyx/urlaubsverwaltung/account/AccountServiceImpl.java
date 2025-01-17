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

/**
 * Implementation of {@link AccountService}.
 */
@Service
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final SettingsService settingsService;

    @Autowired
    AccountServiceImpl(AccountRepository accountRepository, SettingsService settingsService) {
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

        final LocalDate expiryMonthDay = globallyExpiryDate(Year.of(year));
        final LocalDate expiryDateGlobally = LocalDate.of(year, expiryMonthDay.getMonth(), expiryMonthDay.getDayOfMonth());
        final LocalDate expiryDateLocally = prevYearAccount.map(Account::getExpiryDateLocally).map(date -> date.withYear(year)).orElse(null);

        return AccountDraft.builder()
            .person(person)
            .year(Year.of(year))
            .annualVacationDays(prevYearAccount.map(Account::getAnnualVacationDays).orElse(null))
            .doRemainingVacationDaysExpireGlobally(expiresGlobally)
            .doRemainingVacationDaysExpireLocally(expiresLocally)
            .expiryDateGlobally(expiryDateGlobally)
            .expiryDateLocally(expiryDateLocally)
            .setRemainingVacationDaysNotExpiring(prevYearAccount.map(Account::getRemainingVacationDaysNotExpiring).orElse(null))
            .build();
    }

    @Override
    public List<Account> getHolidaysAccount(int year, List<Person> persons) {
        final CachedSupplier<Boolean> expireGlobally = new CachedSupplier<>(this::remainingVacationDaysExpireGlobally);
        final CachedSupplier<LocalDate> expiryDateGlobally = new CachedSupplier<>(() -> globallyExpiryDate(Year.of(year)));

        return accountRepository.findAccountByYearAndPersons(year, persons)
            .stream()
            .map(accountEntity -> this.mapToAccount(accountEntity, expireGlobally.get(), expiryDateGlobally.get()))
            .toList();
    }

    @Override
    public Account save(Account account) {
        final AccountEntity accountEntity = mapToAccountEntity(account);
        final AccountEntity savedAccountEntity = accountRepository.save(accountEntity);

        final LocalDate expiryDateGlobally = globallyExpiryDate(Year.of(savedAccountEntity.getYear()));
        return mapToAccount(savedAccountEntity, remainingVacationDaysExpireGlobally(), expiryDateGlobally);
    }

    private Account mapToAccount(AccountEntity accountEntity, boolean doRemainingVacationDaysExpireGlobally, LocalDate expiryDateGlobally) {
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
        account.setExpiryDateGlobally(expiryDateGlobally);
        return account;
    }

    private AccountEntity mapToAccountEntity(Account account) {
        final AccountEntity accountEntity = new AccountEntity(
            account.getPerson(),
            account.getValidFrom(),
            account.getValidTo(),
            account.isDoRemainingVacationDaysExpireLocally(),
            account.getExpiryDateLocally(),
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

    @Override
    public List<Account> getHolidaysAccountsByPerson(Person person) {

        final CachedSupplier<Boolean> expireGlobally = new CachedSupplier<>(this::remainingVacationDaysExpireGlobally);

        return accountRepository.findAllByPersonId(person.getId())
            .stream()
            .map(accountEntity -> this.mapToAccount(accountEntity, expireGlobally.get(), globallyExpiryDate(Year.of(accountEntity.getYear())))).toList();
    }

    private boolean remainingVacationDaysExpireGlobally() {
        return settingsService.getSettings().getAccountSettings().isDoRemainingVacationDaysExpireGlobally();
    }

    private LocalDate globallyExpiryDate(Year year) {
        return settingsService.getSettings().getAccountSettings().getExpiryDateForYear(year);
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
