package org.synyx.urlaubsverwaltung.application.application;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysServiceImpl;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

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
    @Mock
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {

        final PublicHolidaysService publicHolidaysService = new PublicHolidaysServiceImpl(settingsService, Map.of("de", getHolidayManager()));
        final WorkDaysCountService workDaysCountService = new WorkDaysCountService(publicHolidaysService, workingTimeService);

        sut = new CalculationService(vacationDaysService, accountService, accountInteractionService, workDaysCountService,
            new OverlapService(null, null), applicationService);
    }

    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeftAreNegativeEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, AUGUST, 20);
        final LocalDate endDate = LocalDate.of(2012, AUGUST, 21);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(startDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeftAreNeutralEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, AUGUST, 19);
        final LocalDate endDate = LocalDate.of(2012, AUGUST, 25);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(LocalDate.of(2012, AUGUST, 23));
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2012, AUGUST, 21));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeftArePositiveEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, AUGUST, 20);
        final LocalDate endDate = LocalDate.of(2012, AUGUST, 21);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(startDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(vacationDaysService.getVacationDaysLeft(any(), any())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(BigDecimal.valueOf(20))
                .notExpiring(TEN)
                .forUsedDaysBeforeApril(TEN)
                .forUsedDaysAfterApril(TEN)
                .build());
        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(any())).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationDifferentYearAndEnoughDaysLeftAreNegativeEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, DECEMBER, 28);
        final LocalDate endDate = LocalDate.of(2013, JANUARY, 4);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 29));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 3));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationDifferentYearAndEnoughDaysLeftAreNeutralEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, DECEMBER, 29);
        final LocalDate endDate = LocalDate.of(2013, JANUARY, 5);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(LocalDate.of(2013, JANUARY, 4));
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 30));
        applicationForLeaveToCheck.setEndDate(endDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationDifferentYearAndEnoughDaysLeftArePositiveEditing() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2012, DECEMBER, 28);
        final LocalDate endDate = LocalDate.of(2013, JANUARY, 7);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10);
        applicationForLeaveToCheckSaved.setStartDate(LocalDate.of(2012, DECEMBER, 29));
        applicationForLeaveToCheckSaved.setEndDate(LocalDate.of(2013, JANUARY, 4));
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Optional<Account> account = Optional.of(new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account);

        final LocalDate validFromNextYear = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validToNextYear = LocalDate.of(2013, DECEMBER, 31);
        final Optional<Account> accountNextYear = Optional.of(new Account(person, validFromNextYear, validToNextYear, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(accountNextYear);

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(accountNextYear)).thenReturn(ZERO);
        when(vacationDaysService.getVacationDaysLeft(account.get(), accountNextYear))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(BigDecimal.valueOf(20))
                .notExpiring(TEN)
                .forUsedDaysBeforeApril(TEN)
                .forUsedDaysAfterApril(TEN)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(Optional.empty())).thenReturn(ZERO);
        when(vacationDaysService.getVacationDaysLeft(accountNextYear.get(), Optional.empty()))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(BigDecimal.valueOf(20))
                .notExpiring(TEN)
                .forUsedDaysBeforeApril(TEN)
                .forUsedDaysAfterApril(TEN)
                .build());

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Optional<Account> account2013 = Optional.empty();
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        when(vacationDaysService.getVacationDaysLeft(account, account2013))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(new BigDecimal("2"))
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndNotEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Optional<Account> account2013 = Optional.empty();
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        when(vacationDaysService.getVacationDaysLeft(account, account2013))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(ZERO)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testCheckApplicationSameYearAndExactEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Optional<Account> account2013 = Optional.empty();
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        when(vacationDaysService.getVacationDaysLeft(account, account2013))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(ONE)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsToMuch() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Optional<Account> account2012 = Optional.of(new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Optional<Account> account2013 = Optional.of(new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ZERO)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(TEN)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testCheckApplicationRemainingVacationDaysLeftNotUsedAfterApril() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2012, AUGUST, 6);
        final LocalDate endDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, endDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Optional<Account> account2013 = Optional.empty();
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        when(vacationDaysService.getVacationDaysLeft(account, account2013))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(ONE)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(ZERO)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(account2013)).thenReturn(ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testPersonWithoutHolidayAccount() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isFalse();
    }

    @Test
    void testCheckApplicationOneDayIsOkay() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);

        // enough vacation days for this application for leave, but none would be left
        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Optional<Account> account2012 = Optional.of(new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Optional<Account> account2013 = Optional.of(new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013))
            .thenReturn(VacationDaysLeft.builder()
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
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());
        when(accountService.getHolidaysAccount(2011, person)).thenReturn(Optional.of(account));
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account)).thenReturn(account);

        when(vacationDaysService.getVacationDaysLeft(any(), any()))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(ONE)
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
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2012, DECEMBER, 30);
        final LocalDate endDate = LocalDate.of(2013, JANUARY, 2);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);

        prepareSetupWith10DayAnnualVacation(person, 5, 4);

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsNotOkayOverAYear() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2012, DECEMBER, 30);
        final LocalDate endDate = LocalDate.of(2013, JANUARY, 2);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final Optional<Account> account2012 = Optional.of(new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final Optional<Account> account2013 = Optional.of(new Account(person, validFrom13, validTo13, TEN, TEN, TEN, "comment"));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // vacation days would be left after this application for leave
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013))
            .thenReturn(VacationDaysLeft.builder()
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
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2012, AUGUST, 20);
        final LocalDate endDate = LocalDate.of(2012, AUGUST, 30);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        // nine days
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);

        final Optional<Account> account2012 = Optional.of(new Account(person, getFirstDayOfYear(2012), getLastDayOfYear(2012), TEN, ZERO, ZERO, ""));
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);

        // here we set up 2013 to have 10 days remaining vacation available from 2012,
        // if those have already been used up, we cannot spend them in 2012 as well
        final Optional<Account> account2013 = Optional.of(new Account(person, getFirstDayOfYear(2013), getLastDayOfYear(2013), TEN, TEN, TEN, ""));
        account2013.get().setVacationDays(account2013.get().getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        // this year still has all ten days (but 3 of them used up next year, see above)
        when(vacationDaysService.getVacationDaysLeft(account2012.get(), account2013))
            .thenReturn(VacationDaysLeft.builder()
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

    @Test
    void ensureValidApplicationWithVacationLeftBeforeAprilAndNoVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2022, FEBRUARY, 1);

        final WorkingTime workingTime = new WorkingTime(person, date, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(date);
        applicationForLeaveToCheck.setEndDate(date);

        final Account account2022 = new Account(person, getFirstDayOfYear(2022), getLastDayOfYear(2022), TEN, TEN, ZERO, "");
        account2022.setVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final Account account2023 = new Account(person, getFirstDayOfYear(2023), getLastDayOfYear(2023), TEN, TEN, ZERO, "");
        account2023.setVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        when(vacationDaysService.getVacationDaysLeft(account2022, Optional.of(account2023)))
            .thenReturn(VacationDaysLeft.builder()
                .withAnnualVacation(TEN)
                .withRemainingVacation(TEN)
                .notExpiring(ZERO)
                .forUsedDaysBeforeApril(ZERO)
                .forUsedDaysAfterApril(TEN)
                .withVacationDaysUsedNextYear(ZERO)
                .build());

        when(vacationDaysService.getRemainingVacationDaysAlreadyUsed(Optional.of(account2023))).thenReturn(ZERO);

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isTrue();
    }

    private Application createApplicationStub(Person person) {
        final Application application = new Application();
        application.setPerson(person);
        application.setDayLength(FULL);
        return application;
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
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        final ManagerParameter managerParameter = ManagerParameters.create(url);
        return HolidayManager.getInstance(managerParameter);
    }
}
