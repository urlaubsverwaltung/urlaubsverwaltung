package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountInteractionServiceImplTest {


    private AccountInteractionServiceImpl sut;

    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private Clock clock;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setup() {
        sut = new AccountInteractionServiceImpl(accountService, vacationDaysService, settingsService, clock);
    }

    @Test
    void testDefaultAccountCreation() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2019-08-13T00:00:00.00Z"), ZoneId.of("UTC"));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Settings settings = new Settings();
        settings.getAccountSettings().setDefaultVacationDays(1);
        when(settingsService.getSettings()).thenReturn(settings);

        sut.createDefaultAccount(person);

        final ArgumentCaptor<Account> argument = ArgumentCaptor.forClass(Account.class);
        verify(accountService).save(argument.capture());

        final Account account = argument.getValue();
        assertThat(account.getPerson()).isEqualTo(person);
        assertThat(account.getValidFrom()).isEqualTo(LocalDate.now(clock));
        assertThat(account.getValidTo()).isEqualTo(LocalDate.now(clock).with(lastDayOfYear()));
        assertThat(account.getAnnualVacationDays()).isEqualTo(BigDecimal.valueOf(1));
        assertThat(account.getComment()).isEmpty();
        assertThat(account.getYear()).isEqualTo(LocalDate.now(clock).getYear());
        assertThat(account.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(account.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void testUpdateRemainingVacationDays() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2012, DECEMBER, 31);

        final BigDecimal annualVacationDays = BigDecimal.valueOf(30);

        final Account account2012 = new Account(person, startDate, endDate, annualVacationDays, BigDecimal.valueOf(5), ZERO, null);
        final Account account2013 = new Account(person, startDate.withYear(2013), endDate.withYear(2013), annualVacationDays, BigDecimal.valueOf(3), ZERO, "comment1");
        final Account account2014 = new Account(person, startDate.withYear(2014), endDate.withYear(2014), annualVacationDays, BigDecimal.valueOf(8), ZERO, "comment2");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));
        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.empty());

        when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));
        when(vacationDaysService.calculateTotalLeftVacationDays(account2013)).thenReturn(BigDecimal.valueOf(2));

        sut.updateRemainingVacationDays(2012, person);

        verify(vacationDaysService, never()).calculateTotalLeftVacationDays(account2014);
        verify(accountService, never()).save(account2012);

        assertThat(account2012.getRemainingVacationDays()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(account2013.getRemainingVacationDays()).isEqualTo(BigDecimal.valueOf(6));
        assertThat(account2014.getRemainingVacationDays()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(account2012.getComment()).isNull();
        assertThat(account2013.getComment()).isSameAs("comment1");
        assertThat(account2014.getComment()).isSameAs("comment2");
    }

    @Test
    void testUpdateRemainingVacationDaysAndNotExpiringDaysAreGreaterThenRemaining() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2012, DECEMBER, 31);

        final BigDecimal annualVacationDays = BigDecimal.valueOf(30);

        final Account account2012 = new Account(person, startDate, endDate, annualVacationDays, BigDecimal.valueOf(3), ZERO, null);
        final Account account2013 = new Account(person, startDate.withYear(2013), endDate.withYear(2013), annualVacationDays, ZERO, TEN, "comment1");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.empty());

        when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));

        sut.updateRemainingVacationDays(2012, person);
        assertThat(account2013.getRemainingVacationDays()).isEqualTo(BigDecimal.valueOf(6));
    }

    @Test
    void testUpdateRemainingVacationDaysHasNoThisYearAccount() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2012, DECEMBER, 31);
        final BigDecimal annualVacationDays = BigDecimal.valueOf(30);
        final BigDecimal remainingVacationDays = BigDecimal.valueOf(5);

        final Account nextYearAccount = new Account(person, startDate, endDate, annualVacationDays, remainingVacationDays, ZERO, null);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(nextYearAccount));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());

        sut.updateRemainingVacationDays(2012, person);
        assertThat(nextYearAccount.getRemainingVacationDays()).isEqualTo(remainingVacationDays);

        verify(vacationDaysService, never()).calculateTotalLeftVacationDays(any());
        verify(accountService, never()).save(any());
    }

    @Test
    void ensureCreatesNewHolidaysAccountIfNotExistsYet() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final int year = 2014;
        final int nextYear = 2015;

        final LocalDate startDate = LocalDate.of(year, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(year, OCTOBER, 31);
        final BigDecimal leftDays = BigDecimal.ONE;

        final Account referenceHolidaysAccount = new Account(person, startDate, endDate,
            BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.empty());
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceHolidaysAccount)).thenReturn(leftDays);
        when(accountService.save(any())).then(returnsFirstArg());

        final Account createdHolidaysAccount = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceHolidaysAccount);
        assertThat(createdHolidaysAccount.getPerson()).isEqualTo(person);
        assertThat(createdHolidaysAccount.getAnnualVacationDays()).isEqualTo(referenceHolidaysAccount.getAnnualVacationDays());
        assertThat(createdHolidaysAccount.getRemainingVacationDays()).isEqualTo(leftDays);
        assertThat(createdHolidaysAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
        assertThat(createdHolidaysAccount.getValidFrom()).isEqualTo(LocalDate.of(nextYear, 1, 1));
        assertThat(createdHolidaysAccount.getValidTo()).isEqualTo(LocalDate.of(nextYear, 12, 31));

        verify(accountService).save(createdHolidaysAccount);
        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceHolidaysAccount);
        verify(accountService, times(2)).getHolidaysAccount(nextYear, person);
    }

    @Test
    void ensureUpdatesRemainingVacationDaysOfHolidaysAccountIfAlreadyExists() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final int year = 2014;
        final int nextYear = 2015;

        final LocalDate startDate = LocalDate.of(year, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(year, OCTOBER, 31);
        final BigDecimal leftDays = BigDecimal.valueOf(7);

        final Account referenceAccount = new Account(person, startDate, endDate, BigDecimal.valueOf(30),
            BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        final Account nextYearAccount = new Account(person, LocalDate.of(nextYear, 1, 1), LocalDate.of(
            nextYear, 10, 31), BigDecimal.valueOf(28), ZERO, ZERO, "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.of(nextYearAccount));
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceAccount)).thenReturn(leftDays);

        final Account account = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceAccount);
        assertThat(account).isNotNull();
        assertThat(account.getPerson()).isEqualTo(person);
        assertThat(account.getAnnualVacationDays()).isEqualTo(nextYearAccount.getAnnualVacationDays());
        assertThat(account.getRemainingVacationDays()).isEqualTo(leftDays);
        assertThat(account.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
        assertThat(account.getValidFrom()).isEqualTo(nextYearAccount.getValidFrom());
        assertThat(account.getValidTo()).isEqualTo(nextYearAccount.getValidTo());

        verify(accountService).save(account);
        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceAccount);
        verify(accountService).getHolidaysAccount(nextYear, person);
    }

    @Test
    void createHolidaysAccount() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.empty());
        when(accountService.save(any())).then(returnsFirstArg());

        final Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, TEN, ONE, ZERO, TEN, "comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(TEN);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ZERO);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(TEN);

    }

    @Test
    void updateHolidaysAccount() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        when(accountService.save(any())).then(returnsFirstArg());

        final Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, ONE, ONE, ONE, ONE, "new comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ONE);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(ONE);
    }
}
