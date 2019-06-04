package org.synyx.urlaubsverwaltung.account.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AccountInteractionServiceImplTest {

    private AccountInteractionServiceImpl sut;

    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;

    private Person person;


    @Before
    public void setup() {

        sut = new AccountInteractionServiceImpl(accountService, vacationDaysService);

        person = TestDataCreator.createPerson("horscht");
    }


    @Test
    public void testUpdateRemainingVacationDays() {

        LocalDate startDate = LocalDate.of(2012, JANUARY, 1);
        LocalDate endDate = LocalDate.of(2012, DECEMBER, 31);

        Account account2012 = new Account(person, startDate, endDate, BigDecimal.valueOf(30),
                BigDecimal.valueOf(5), ZERO, null);

        Account account2013 = new Account(person, startDate.withYear(2013), endDate.withYear(2013),
                BigDecimal.valueOf(30), BigDecimal.valueOf(3), ZERO, "comment1");

        Account account2014 = new Account(person, startDate.withYear(2014), endDate.withYear(2014),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), ZERO, "comment2");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));
        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.empty());

        when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));
        when(vacationDaysService.calculateTotalLeftVacationDays(account2013)).thenReturn(BigDecimal.valueOf(2));

        sut.updateRemainingVacationDays(2012, person);

        verify(vacationDaysService).calculateTotalLeftVacationDays(account2012);
        verify(vacationDaysService).calculateTotalLeftVacationDays(account2013);
        verify(vacationDaysService, never()).calculateTotalLeftVacationDays(account2014);

        verify(accountService, never()).save(account2012);
        verify(accountService).save(account2013);
        verify(accountService).save(account2014);

        Assert.assertEquals("Wrong number of remaining vacation days for 2012", BigDecimal.valueOf(5),
                account2012.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2013", BigDecimal.valueOf(6),
                account2013.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2014", BigDecimal.valueOf(2),
                account2014.getRemainingVacationDays());
        Assert.assertEquals("Wrong comment", null, account2012.getComment());
        Assert.assertEquals("Wrong comment", "comment1", account2013.getComment());
        Assert.assertEquals("Wrong comment", "comment2", account2014.getComment());
    }


    @Test
    public void ensureCreatesNewHolidaysAccountIfNotExistsYet() {

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
    public void ensureUpdatesRemainingVacationDaysOfHolidaysAccountIfAlreadyExists() {

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
    public void createHolidaysAccount() {

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
    public void updateHolidaysAccount() {

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
