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

    private Application createApplicationStub(Person person) {
        Application template = new Application();
        template.setPerson(person);
        template.setDayLength(DayLength.FULL);
        return template;
    }

    private void prepareSetupWith10DayAnnualVacation(Person person, int usedDaysBeforeApril, int usedDaysAfterApril) {

        Optional<Account> account2012 = Optional.of(new Account());
        Optional<Account> account2013 = Optional.of(new Account());
        Optional<Account> account2014 = Optional.of(new Account());
        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        Mockito.when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);
        Mockito.when(accountService.getHolidaysAccount(2014, person)).thenReturn(account2014);

        // vacation days would be left after this application for leave
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                        .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                        .get());
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2013.get(), account2014)).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                        .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                        .get());
        Mockito.when(vacationDaysService.getVacationDaysLeft(account2014.get(), Optional.empty())).thenReturn(
                VacationDaysLeft.builder()
                        .withAnnualVacation(BigDecimal.TEN)
                        .withRemainingVacation(BigDecimal.ZERO)
                        .notExpiring(BigDecimal.ZERO)
                        .forUsedDaysBeforeApril(BigDecimal.ZERO)
                        .forUsedDaysAfterApril(BigDecimal.ZERO)
                        .get());
        Mockito.when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(BigDecimal.ZERO);
        Mockito.when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2014)).thenReturn(BigDecimal.ZERO);
    }


    @Test
    public void testCheckApplicationOneDayIsOkayNoVacationTakenBefore() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));

        prepareSetupWith10DayAnnualVacation(person, 0, 0);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));
    }


    @Test
    public void testCheckApplicationOneDayIsToMuch() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));

        // not enough vacation days for this application for leave
        prepareSetupWith10DayAnnualVacation(person, 0, 10);

        Assert.assertFalse("Should NOT be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));


    }


    @Test
    public void testCheckApplicationOneDayIsOkay() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));

        // enough vacation days for this application for leave, but none would be left
        prepareSetupWith10DayAnnualVacation(person, 4, 5);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));
    }

    @Test
    public void testCheckApplicationOneDayIsOkayOverAYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 2));

        prepareSetupWith10DayAnnualVacation(person, 5, 4);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));
    }

    @Test
    public void testCheckApplicationOneDayIsNotOkayOverAYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 2));

        prepareSetupWith10DayAnnualVacation(person, 5, 5);

        Assert.assertFalse("Should not be enough vacation days to apply for leave",
                service.checkApplication(applicationForLeaveToCheck));
    }

    /**
     * https://github.com/synyx/urlaubsverwaltung/issues/447
     */
    @Test
    public void testCheckApplicationNextYearUsingRemainingAlready() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        // nine days
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 30));

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
