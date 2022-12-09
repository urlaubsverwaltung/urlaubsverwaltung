package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private AccountServiceImpl sut;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new AccountServiceImpl(accountRepository, settingsService);
    }

    @Test
    void ensureReturnsOptionalWithHolidaysAccountIfExists() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final int year = 2012;
        final LocalDate from = Year.of(year).atDay(1);
        final LocalDate to = DateUtil.getLastDayOfYear(year);
        final LocalDate expiryDate = LocalDate.of(year, Month.APRIL, 1);
        final BigDecimal annualVacationDays = new BigDecimal("30");
        final BigDecimal remainingVacationDays = new BigDecimal("3");
        final BigDecimal remainingVacationDaysNotExpiring = ZERO;
        final boolean doRemainingVacationDaysExpire = true;
        final String comment = "comment";
        final AccountEntity accountEntity = new AccountEntity(person, from, to, doRemainingVacationDaysExpire, expiryDate,
            annualVacationDays, remainingVacationDays, remainingVacationDaysNotExpiring, comment);

        final int id = 1;
        accountEntity.setId(id);

        final LocalDate expiryNotificationSentDate = LocalDate.of(year, Month.APRIL, 1);
        accountEntity.setExpiryNotificationSentDate(expiryNotificationSentDate);

        final BigDecimal actualVacationDays = new BigDecimal("29");
        accountEntity.setActualVacationDays(actualVacationDays);

        when(accountRepository.findAccountByYearAndPersons(year, List.of(person))).thenReturn(List.of(accountEntity));

        final Optional<Account> actual = sut.getHolidaysAccount(year, person);
        assertThat(actual).isPresent();

        final Account holidaysAccount = actual.get();
        assertThat(holidaysAccount.getId()).isEqualTo(id);
        assertThat(holidaysAccount.getPerson()).isEqualTo(person);
        assertThat(holidaysAccount.getValidFrom()).isEqualTo(from);
        assertThat(holidaysAccount.getValidTo()).isEqualTo(to);
        assertThat(holidaysAccount.doRemainigVacationDaysExpire()).isEqualTo(doRemainingVacationDaysExpire);
        assertThat(holidaysAccount.isDoRemainingVacationDaysExpireGlobally()).isTrue();
        assertThat(holidaysAccount.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(holidaysAccount.getExpiryNotificationSentDate()).isEqualTo(expiryNotificationSentDate);
        assertThat(holidaysAccount.getAnnualVacationDays()).isEqualTo(annualVacationDays);
        assertThat(holidaysAccount.getActualVacationDays()).isEqualTo(actualVacationDays);
        assertThat(holidaysAccount.getRemainingVacationDays()).isEqualTo(remainingVacationDays);
        assertThat(holidaysAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(remainingVacationDaysNotExpiring);
        assertThat(holidaysAccount.getComment()).isEqualTo(comment);
    }

    @Test
    void ensureReturnsAbsentOptionalIfNoHolidaysAccountExists() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(accountRepository.findAccountByYearAndPersons(2012, List.of(person))).thenReturn(List.of());

        Optional<Account> optionalHolidaysAccount = sut.getHolidaysAccount(2012, person);
        assertThat(optionalHolidaysAccount).isEmpty();
    }

    @Test
    void ensureReturnsHolidaysAccountIfExists() {

        final AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDoRemainingVacationDaysExpireGlobally(false);

        final Settings settings = new Settings();
        settings.setAccountSettings(accountSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1);

        final Person person2 = new Person("muster2", "Muster2", "Marlene2", "muster2@example.org");
        person2.setId(2);

        final int year = 2012;
        final LocalDate from = Year.of(year).atDay(1);
        final LocalDate to = LocalDate.of(year, 1, 1).with(lastDayOfYear());
        final LocalDate expiryDate = LocalDate.of(year, Month.APRIL, 1);

        final AccountEntity accountEntity1 = new AccountEntity(person, from, to, null, expiryDate,
            new BigDecimal(30), new BigDecimal(3), ZERO, "awesome comment");
        accountEntity1.setId(1);

        final AccountEntity accountEntity2 = new AccountEntity(person2, from, to, true, expiryDate,
            new BigDecimal(30), new BigDecimal(3), ZERO, "awesome comment nummero dueee");
        accountEntity2.setId(2);

        when(accountRepository.findAccountByYearAndPersons(2012, List.of(person, person2)))
            .thenReturn(List.of(accountEntity1, accountEntity2));

        final List<Account> actual = sut.getHolidaysAccount(2012, List.of(person, person2));
        assertThat(actual).hasSize(2);

        assertThat(actual.get(0)).satisfies(account -> {
            assertThat(account.getId()).isEqualTo(1);
            assertThat(account.getPerson()).isEqualTo(person);
            assertThat(account.getValidFrom()).isEqualTo(from);
            assertThat(account.getValidTo()).isEqualTo(to);
            assertThat(account.isDoRemainingVacationDaysExpireGlobally()).isFalse();
            assertThat(account.isDoRemainingVacationDaysExpireLocally()).isNull();
            assertThat(account.getAnnualVacationDays()).isEqualTo(new BigDecimal(30));
            assertThat(account.getRemainingVacationDays()).isEqualTo(new BigDecimal(3));
            assertThat(account.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
            assertThat(account.getComment()).isEqualTo("awesome comment");
        });

        assertThat(actual.get(1)).satisfies(account -> {
            assertThat(account.getId()).isEqualTo(2);
            assertThat(account.getPerson()).isEqualTo(person2);
            assertThat(account.getValidFrom()).isEqualTo(from);
            assertThat(account.getValidTo()).isEqualTo(to);
            assertThat(account.isDoRemainingVacationDaysExpireGlobally()).isFalse();
            assertThat(account.isDoRemainingVacationDaysExpireLocally()).isTrue();
            assertThat(account.getAnnualVacationDays()).isEqualTo(new BigDecimal(30));
            assertThat(account.getRemainingVacationDays()).isEqualTo(new BigDecimal(3));
            assertThat(account.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
            assertThat(account.getComment()).isEqualTo("awesome comment nummero dueee");
        });
    }

    @Test
    void ensureReturnsEmptyListIfNoHolidaysAccountExists() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(accountRepository.findAccountByYearAndPersons(2012, List.of(person))).thenReturn(List.of());

        final List<Account> holidaysAccount = sut.getHolidaysAccount(2012, List.of(person));
        assertThat(holidaysAccount).isEmpty();
    }

    @Test
    void deleteAllDelegatesToRepository() {
        final Person person = new Person();

        sut.deleteAllByPerson(person);

        verify(accountRepository).deleteByPerson(person);
    }
}
