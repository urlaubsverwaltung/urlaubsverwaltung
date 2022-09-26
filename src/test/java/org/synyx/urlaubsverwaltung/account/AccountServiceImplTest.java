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
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository, settingsService);
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

        when(accountRepository.getHolidaysAccountByYearAndPerson(year, person)).thenReturn(Optional.of(accountEntity));

        final Account holidaysAccount = accountService.getHolidaysAccount(year, person).get();
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

        when(settingsService.getSettings()).thenReturn(new Settings());
        when(accountRepository.getHolidaysAccountByYearAndPerson(anyInt(), any(Person.class))).thenReturn(Optional.empty());

        Optional<Account> optionalHolidaysAccount = accountService.getHolidaysAccount(2012, mock(Person.class));
        assertThat(optionalHolidaysAccount).isEmpty();
    }
}
