package org.synyx.urlaubsverwaltung.account.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.config.AccountProperties;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
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

    private final static LocalDate LOCAL_DATE = LocalDate.of(2019, 8, 13);

    private AccountInteractionServiceImpl sut;

    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private Clock clock;
    @Mock
    private AccountProperties accountProperties;

    @BeforeEach
    void setup() {
        sut = new AccountInteractionServiceImpl(accountProperties, accountService, vacationDaysService, clock);
    }

    @Test
    void testDefaultAccountCreation() {

        final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(accountProperties.getDefaultVacationDays()).thenReturn(20);

        sut.createDefaultAccount(person);

        Account expectedAccount = new Account(person, LocalDate.now(clock), LocalDate.now(clock).with(lastDayOfYear()), valueOf(20), ZERO, ZERO, "");
        expectedAccount.setVacationDays(valueOf(7));

        ArgumentCaptor<Account> argument = ArgumentCaptor.forClass(Account.class);
        verify(accountService).save(argument.capture());
        AssertionsForClassTypes.assertThat(argument.getValue()).isEqualToComparingFieldByField(expectedAccount);
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

        int year = 2014;
        int nextYear = 2015;

        LocalDate startDate = LocalDate.of(year, JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.ONE;

        Account referenceHolidaysAccount = new Account(person, startDate, endDate,
            BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.empty());
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceHolidaysAccount)).thenReturn(leftDays);
        when(accountService.save(any())).then(returnsFirstArg());

        Account createdHolidaysAccount = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceHolidaysAccount);

        Assert.assertNotNull("Should not be null", createdHolidaysAccount);

        Assert.assertEquals("Wrong person", person, createdHolidaysAccount.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", referenceHolidaysAccount.getAnnualVacationDays(),
            createdHolidaysAccount.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays,
            createdHolidaysAccount.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", ZERO,
            createdHolidaysAccount.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", LocalDate.of(nextYear, 1, 1),
            createdHolidaysAccount.getValidFrom());
        Assert.assertEquals("Wrong validity end date", LocalDate.of(nextYear, 12, 31),
            createdHolidaysAccount.getValidTo());

        verify(accountService).save(createdHolidaysAccount);

        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceHolidaysAccount);
        verify(accountService, times(2)).getHolidaysAccount(nextYear, person);
    }


    @Test
    void ensureUpdatesRemainingVacationDaysOfHolidaysAccountIfAlreadyExists() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        int year = 2014;
        int nextYear = 2015;

        LocalDate startDate = LocalDate.of(year, JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.valueOf(7);

        Account referenceAccount = new Account(person, startDate, endDate, BigDecimal.valueOf(30),
            BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        Account nextYearAccount = new Account(person, LocalDate.of(nextYear, 1, 1), LocalDate.of(
            nextYear, 10, 31), BigDecimal.valueOf(28), ZERO, ZERO, "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.of(nextYearAccount));
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceAccount)).thenReturn(leftDays);

        Account account = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceAccount);

        Assert.assertNotNull("Should not be null", account);

        Assert.assertEquals("Wrong person", person, account.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", nextYearAccount.getAnnualVacationDays(),
            account.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays, account.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", ZERO,
            account.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", nextYearAccount.getValidFrom(), account.getValidFrom());
        Assert.assertEquals("Wrong validity end date", nextYearAccount.getValidTo(), account.getValidTo());

        verify(accountService).save(account);

        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceAccount);
        verify(accountService).getHolidaysAccount(nextYear, person);
    }

    @Test
    void createHolidaysAccount() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.empty());
        when(accountService.save(any())).then(returnsFirstArg());

        Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, TEN, ONE, ZERO, TEN, "comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(TEN);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ZERO);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(TEN);

    }

    @Test
    void updateHolidaysAccount() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));
        when(accountService.save(any())).then(returnsFirstArg());

        Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, ONE, ONE, ONE, ONE, "new comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ONE);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(ONE);
    }
}
