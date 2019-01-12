package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.*;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Unit test for {@link CalculationService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalculationServiceTest {

    private CalculationService service;
    private VacationDaysService vacationDaysService;
    private AccountInteractionService accountInteractionService;
    private AccountService accountService;
    private WorkDaysService calendarService;

    @Before
    public void setUp() throws IOException {

        vacationDaysService = Mockito.mock(VacationDaysService.class);
        accountService = Mockito.mock(AccountService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        calendarService = new WorkDaysService(new PublicHolidaysService(settingsService), workingTimeService,
                settingsService);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                    Mockito.any(DateMidnight.class)))
            .thenReturn(Optional.of(workingTime));

        service = new CalculationService(vacationDaysService, accountService, accountInteractionService,
                calendarService, new OverlapService(null, null));
    }


    @Test
    public void testCheckApplicationSimple() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Optional<Account> account2012 = Optional.of(new Account());
        Optional<Account> account2013 = Optional.empty();
        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        Mockito.when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.ZERO)
                        .forUsedDaysAfterApril(BigDecimal.ZERO)
                        .get());
        Mockito.when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(BigDecimal.ZERO);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));

        // not enough vacation days for this application for leave
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.ZERO)
                        .forUsedDaysAfterApril(BigDecimal.TEN)
                        .get());

        Assert.assertFalse("Should NOT be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));

        // enough vacation days for this application for leave, but none would be left
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.valueOf(4))
                        .forUsedDaysAfterApril(BigDecimal.valueOf(5))
                        .get());

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));
    }


    /**
     * https://github.com/synyx/urlaubsverwaltung/issues/447
     */
    @Test
    public void testCheckApplicationNextYearUsingRemainingAlready() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        // nine days
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 30));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Optional<Account> account2012 = Optional.of(
                new Account(person, DateUtil.getFirstDayOfYear(2012).toDate(), DateUtil.getLastDayOfYear(2012).toDate(),
                        BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, "")
        );

        Optional<Account> account2013 = Optional.of(
                new Account(person, DateUtil.getFirstDayOfYear(2013).toDate(), DateUtil.getLastDayOfYear(2013).toDate(), BigDecimal.TEN,
                // here we set up 2013 to have 10 days remaining vacation available from 2012,
                // if those have already been used up, we cannot spend them in 2012 as well
                BigDecimal.TEN, BigDecimal.TEN, ""));
        account2013.get().setVacationDays(account2013.get().getAnnualVacationDays());


        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        Mockito.when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // set up 13 days already used next year, i.e. 10 + 3 remaining
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2013.get(), Optional.empty())).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.TEN)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.valueOf(13))
                        .forUsedDaysAfterApril(BigDecimal.ZERO)
                        .get());

        // this year still has all ten days (but 3 of them used up next year, see above)
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.ZERO)
                        .forUsedDaysAfterApril(BigDecimal.ZERO)
                        .withVacationDaysUsedNextYear(BigDecimal.valueOf(3))
                        .get());

        Mockito.when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(BigDecimal.TEN);

        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account2012.get())).thenReturn(BigDecimal.TEN);

        Assert.assertFalse("Should not be enough vacation days to apply for leave, because three already used next year",
                service.checkApplication(applicationForLeaveToCheck));

    }
}
