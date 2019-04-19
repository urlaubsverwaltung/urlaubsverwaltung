package org.synyx.urlaubsverwaltung.application.service;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


/**
 * Unit test for {@link CalculationService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculationServiceTest {

    private CalculationService sut;

    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private AccountService accountService;
    @Mock
    private WorkDaysService calendarService;

    @Before
    public void setUp() {
        WorkingTimeService workingTimeService = mock(WorkingTimeService.class);
        SettingsService settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(new Settings());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        HolidayManager holidayManager = HolidayManager.getInstance(managerParameter);

        calendarService = new WorkDaysService(new PublicHolidaysService(settingsService, holidayManager), workingTimeService,
            settingsService);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = asList(MONDAY.getValue(), TUESDAY.getValue(), WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());
        workingTime.setWorkingDays(workingDays, FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        sut = new CalculationService(vacationDaysService, accountService, accountInteractionService, calendarService, new OverlapService(null, null));
    }

    private Application createApplicationStub(Person person) {
        Application template = new Application();
        template.setPerson(person);
        template.setDayLength(FULL);
        return template;
    }

    private void prepareSetupWith10DayAnnualVacation(Person person, int usedDaysBeforeApril, int usedDaysAfterApril) {

        Optional<Account> account2012 = Optional.of(new Account());
        Optional<Account> account2013 = Optional.of(new Account());
        Optional<Account> account2014 = Optional.of(new Account());
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(account2014);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                .get());
        when(vacationDaysService.getVacationDaysLeft(account2013.get(), account2014)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                .get());
/*        when(vacationDaysService.getVacationDaysLeft(account2014.get(), Optional.empty())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());*/
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(BigDecimal.ZERO);
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2014)).thenReturn(BigDecimal.ZERO);
    }

    @Test
    public void testCheckApplicationSameYearAndEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        // vacation days would be left after this application for leave
        //when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.TEN);

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(new BigDecimal("2"))
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(BigDecimal.ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    public void testCheckApplicationSameYearAndNotEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.ZERO)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(BigDecimal.ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    public void testCheckApplicationSameYearAndExactEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.ONE)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(BigDecimal.ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }


    @Test
    public void testCheckApplicationOneDayIsToMuch() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        // not enough vacation days for this application for leave
        prepareSetupWith10DayAnnualVacation(person, 0, 10);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    public void testPersonWithoutHolidayAccount() {
        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        Assert.assertFalse("Person is not allowed to leave",
            sut.checkApplication(applicationForLeaveToCheck));
    }

    @Test
    public void testCheckApplicationOneDayIsOkay() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        // enough vacation days for this application for leave, but none would be left
        prepareSetupWith10DayAnnualVacation(person, 4, 5);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    public void testCheckApplicationLastYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());
        when(accountService.getHolidaysAccount(2011, person)).thenReturn(Optional.of(account));
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account)).thenReturn(account);

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.ONE)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(BigDecimal.ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    public void testCheckApplicationOneDayIsOkayOverAYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 2));

        prepareSetupWith10DayAnnualVacation(person, 5, 4);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            sut.checkApplication(applicationForLeaveToCheck));
    }

    @Test
    public void testCheckApplicationOneDayIsNotOkayOverAYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 2));

        prepareSetupWith10DayAnnualVacation(person, 5, 5);

        Assert.assertFalse("Should not be enough vacation days to apply for leave",
            sut.checkApplication(applicationForLeaveToCheck));
    }

    /**
     * https://github.com/synyx/urlaubsverwaltung/issues/447
     */
    @Test
    public void testCheckApplicationNextYearUsingRemainingAlready() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        // nine days
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 30));

        Optional<Account> account2012 = Optional.of(
            new Account(person, DateUtil.getFirstDayOfYear(2012), DateUtil.getLastDayOfYear(2012),
                BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, "")
        );

        Optional<Account> account2013 = Optional.of(
            new Account(person, DateUtil.getFirstDayOfYear(2013), DateUtil.getLastDayOfYear(2013), BigDecimal.TEN,
                // here we set up 2013 to have 10 days remaining vacation available from 2012,
                // if those have already been used up, we cannot spend them in 2012 as well
                BigDecimal.TEN, BigDecimal.TEN, ""));
        account2013.get().setVacationDays(account2013.get().getAnnualVacationDays());


        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // set up 13 days already used next year, i.e. 10 + 3 remaining
/*        when(vacationDaysService.getVacationDaysLeft(account2013.get(), Optional.empty())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.TEN)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(13))
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());*/

        // this year still has all ten days (but 3 of them used up next year, see above)
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.ZERO)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.ZERO)
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .withVacationDaysUsedNextYear(BigDecimal.valueOf(3))
                .get());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(BigDecimal.TEN);

//        when(vacationDaysService.calculateTotalLeftVacationDays(account2012.get())).thenReturn(BigDecimal.TEN);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }
}
