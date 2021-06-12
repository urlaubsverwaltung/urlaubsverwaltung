package org.synyx.urlaubsverwaltung.application.service;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;


/**
 * Unit test for {@link CalculationService}.
 */
@ExtendWith(MockitoExtension.class)
class CalculationServiceTest {

    private CalculationService sut;

    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private AccountService accountService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private WorkingTimeService workingTimeService;

    @BeforeEach
    void setUp() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final HolidayManager holidayManager = getHolidayManager();
        final PublicHolidaysService publicHolidaysService = new PublicHolidaysService(settingsService, holidayManager);
        final WorkDaysCountService workDaysCountService = new WorkDaysCountService(publicHolidaysService, workingTimeService, settingsService);

        // create working time object (MON-FRI)
        final WorkingTime workingTime = new WorkingTime(new Person(), LocalDate.MIN, BADEN_WUERTTEMBERG);
        List<DayOfWeek> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        sut = new CalculationService(vacationDaysService, accountService, accountInteractionService, workDaysCountService,
            new OverlapService(null, null, Clock.systemUTC()));
    }


    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeft() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(new BigDecimal("2"))
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndNotEnoughDaysLeft() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(ZERO)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testCheckApplicationSameYearAndExactEnoughDaysLeft() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.ONE)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }


    @Test
    void testCheckApplicationOneDayIsToMuch() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        // not enough vacation days for this application for leave

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account12 = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        final Optional<Account> account2012 = Optional.of(account12);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Account account13 = new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment");
        final Optional<Account> account2013 = Optional.of(account13);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(0))
                .forUsedDaysAfterApril(BigDecimal.valueOf(10))
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testPersonWithoutHolidayAccount() {
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isFalse();
    }

    @Test
    void testCheckApplicationOneDayIsOkay() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));

        // enough vacation days for this application for leave, but none would be left
        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account12 = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        final Optional<Account> account2012 = Optional.of(account12);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Account account13 = new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment");
        final Optional<Account> account2013 = Optional.of(account13);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(4))
                .forUsedDaysAfterApril(BigDecimal.valueOf(5))
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationLastYear() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());
        when(accountService.getHolidaysAccount(2011, person)).thenReturn(Optional.of(account));
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account)).thenReturn(account);

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.ONE)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsOkayOverAYear() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 2));

        prepareSetupWith10DayAnnualVacation(person, 5, 4);

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsNotOkayOverAYear() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 2));

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account12 = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        final Optional<Account> account2012 = Optional.of(account12);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Account account13 = new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment");
        final Optional<Account> account2013 = Optional.of(account13);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(5))
                .forUsedDaysAfterApril(BigDecimal.valueOf(5))
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isFalse();
    }

    /**
     * https://github.com/synyx/urlaubsverwaltung/issues/447
     */
    @Test
    void testCheckApplicationNextYearUsingRemainingAlready() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        Application applicationForLeaveToCheck = createApplicationStub(person);
        // nine days
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 30));

        Optional<Account> account2012 = Optional.of(
            new Account(person, getFirstDayOfYear(2012), getLastDayOfYear(2012), TEN, ZERO, ZERO, "")
        );

        Optional<Account> account2013 = Optional.of(
            new Account(person, getFirstDayOfYear(2013), getLastDayOfYear(2013), TEN,
                // here we set up 2013 to have 10 days remaining vacation available from 2012,
                // if those have already been used up, we cannot spend them in 2012 as well
                TEN, TEN, ""));
        account2013.get().setVacationDays(account2013.get().getAnnualVacationDays());


        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // this year still has all ten days (but 3 of them used up next year, see above)
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .withVacationDaysUsedNextYear(BigDecimal.valueOf(3))
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(TEN);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    private Application createApplicationStub(Person person) {
        Application template = new Application();
        template.setPerson(person);
        template.setDayLength(FULL);
        return template;
    }

    private void prepareSetupWith10DayAnnualVacation(Person person, int usedDaysBeforeApril, int usedDaysAfterApril) {

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account12 = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        final Optional<Account> account2012 = Optional.of(account12);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Account account13 = new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment");
        final Optional<Account> account2013 = Optional.of(account13);

        final LocalDate validFrom14 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo14 = LocalDate.of(2013, DECEMBER, 31);
        final Account account14 = new Account(person, validFrom14, validTo14, TEN, TEN, TEN, "comment");
        final Optional<Account> account2014 = Optional.of(account14);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(account2014);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                .build());
        when(vacationDaysService.getVacationDaysLeft(account2013.get(), account2014)).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(usedDaysBeforeApril))
                .forUsedDaysAfterApril(BigDecimal.valueOf(usedDaysAfterApril))
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2014)).thenReturn(ZERO);
    }

    private HolidayManager getHolidayManager() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        return HolidayManager.getInstance(managerParameter);
    }
}
