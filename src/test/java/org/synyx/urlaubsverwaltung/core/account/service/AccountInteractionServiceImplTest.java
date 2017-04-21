package org.synyx.urlaubsverwaltung.core.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionServiceImpl}.
 *
 * @author Aljona Murygina - murygina@synyx.de
 */
public class AccountInteractionServiceImplTest {

    private AccountInteractionServiceImpl service;

    private AccountService accountService;
    private VacationDaysService vacationDaysService;

    private Person person;


    @Before
    public void setup() throws IOException {

        accountService = Mockito.mock(AccountService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        vacationDaysService = Mockito.mock(VacationDaysService.class);

        service = new AccountInteractionServiceImpl(accountService, vacationDaysService);

        person = TestDataCreator.createPerson("horscht");
    }


    @Test
    public void testUpdateRemainingVacationDays() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account2012 = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5), BigDecimal.ZERO, null);

        Account account2013 = new Account(person, startDate.withYear(2013).toDate(), endDate.withYear(2013).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(3), BigDecimal.ZERO, "comment1");

        Account account2014 = new Account(person, startDate.withYear(2014).toDate(), endDate.withYear(2014).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.ZERO, "comment2");

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        Mockito.when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        Mockito.when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));
        Mockito.when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.<Account> empty());

        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account2013)).thenReturn(BigDecimal.valueOf(2));

        service.updateRemainingVacationDays(2012, person);

        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(account2012);
        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(account2013);
        Mockito.verify(vacationDaysService, Mockito.never()).calculateTotalLeftVacationDays(account2014);

        Mockito.verify(accountService, Mockito.never()).save(account2012);
        Mockito.verify(accountService).save(account2013);
        Mockito.verify(accountService).save(account2014);

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

        DateMidnight startDate = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight endDate = new DateMidnight(year, DateTimeConstants.OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.ONE;

        Account referenceHolidaysAccount = new Account(person, startDate.toDate(), endDate.toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        Mockito.when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.empty());
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(referenceHolidaysAccount)).thenReturn(leftDays);

        Account createdHolidaysAccount = service.autoCreateOrUpdateNextYearsHolidaysAccount(referenceHolidaysAccount);

        Assert.assertNotNull("Should not be null", createdHolidaysAccount);

        Assert.assertEquals("Wrong person", person, createdHolidaysAccount.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", referenceHolidaysAccount.getAnnualVacationDays(),
                createdHolidaysAccount.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays,
                createdHolidaysAccount.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", BigDecimal.ZERO,
                createdHolidaysAccount.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", new DateMidnight(nextYear, 1, 1),
                createdHolidaysAccount.getValidFrom());
        Assert.assertEquals("Wrong validity end date", new DateMidnight(nextYear, 12, 31),
                createdHolidaysAccount.getValidTo());

        Mockito.verify(accountService).save(createdHolidaysAccount);

        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(referenceHolidaysAccount);
        Mockito.verify(accountService).getHolidaysAccount(nextYear, person);
    }


    @Test
    public void ensureUpdatesRemainingVacationDaysOfHolidaysAccountIfAlreadyExists() {

        int year = 2014;
        int nextYear = 2015;

        DateMidnight startDate = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight endDate = new DateMidnight(year, DateTimeConstants.OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.valueOf(7);

        Account referenceAccount = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        Account nextYearAccount = new Account(person, new DateMidnight(nextYear, 1, 1).toDate(), new DateMidnight(
                nextYear, 10, 31).toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, BigDecimal.ZERO, "comment");

        Mockito.when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.of(nextYearAccount));
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(referenceAccount)).thenReturn(leftDays);

        Account account = service.autoCreateOrUpdateNextYearsHolidaysAccount(referenceAccount);

        Assert.assertNotNull("Should not be null", account);

        Assert.assertEquals("Wrong person", person, account.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", nextYearAccount.getAnnualVacationDays(),
                account.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays, account.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", BigDecimal.ZERO,
                account.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", nextYearAccount.getValidFrom(), account.getValidFrom());
        Assert.assertEquals("Wrong validity end date", nextYearAccount.getValidTo(), account.getValidTo());

        Mockito.verify(accountService).save(account);

        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(referenceAccount);
        Mockito.verify(accountService).getHolidaysAccount(nextYear, person);
    }
}
