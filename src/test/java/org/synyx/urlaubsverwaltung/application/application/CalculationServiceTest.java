package org.synyx.urlaubsverwaltung.application.application;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
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
import java.time.LocalDate;
import java.time.Year;
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
import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

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
        final DateRange dateRange = new DateRange(startDate, endDate);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(dateRange, workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
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
        final DateRange dateRange = new DateRange(startDate, endDate);

        final WorkingTime workingTime = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(dateRange, workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(LocalDate.of(2012, AUGUST, 23));
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
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
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(startDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
        applicationForLeaveToCheck.setStartDate(startDate);
        applicationForLeaveToCheck.setEndDate(endDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(BigDecimal.valueOf(20))
            .notExpiring(TEN)
            .forUsedVacationDaysBeforeExpiry(TEN)
            .forUsedVacationDaysAfterExpiry(TEN)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), Year.of(2012), List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

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
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(endDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
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
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(startDate);
        applicationForLeaveToCheckSaved.setEndDate(LocalDate.of(2013, JANUARY, 4));
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
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

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.of(2012, DECEMBER, 28), GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(LocalDate.of(2012, DECEMBER, 28), LocalDate.of(2013, JANUARY, 7)), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(LocalDate.of(2012, DECEMBER, 29));
        applicationForLeaveToCheckSaved.setEndDate(LocalDate.of(2013, JANUARY, 4));
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);

        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
        applicationForLeaveToCheck.setStartDate(LocalDate.of(2012, DECEMBER, 28));
        applicationForLeaveToCheck.setEndDate(LocalDate.of(2013, JANUARY, 7));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom2012 = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo2012 = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate2012 = LocalDate.of(2012, APRIL, 1);
        final Account account2012 = new Account(person, validFrom2012, validTo2012, true, expiryDate2012, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));

        final LocalDate validFromNextYear2013 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validToNextYear2013 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expiryDateNextYear2013 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFromNextYear2013, validToNextYear2013, true, expiryDateNextYear2013, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        final LocalDate validFrom2014 = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo2014 = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate2014 = LocalDate.of(2014, APRIL, 1);
        final Account account2014 = new Account(person, validFrom2014, validTo2014, true, expiryDate2014, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));

        final Year year2012 = Year.of(2012);
        final Year year2013 = Year.of(2013);

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(BigDecimal.valueOf(20))
            .notExpiring(TEN)
            .forUsedVacationDaysBeforeExpiry(TEN)
            .forUsedVacationDaysAfterExpiry(TEN)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2012), year2012, List.of(account2013)))
            .thenReturn(Map.of(account2012, new HolidayAccountVacationDays(account2012, vacationDaysLeft, vacationDaysLeft)));
        when(vacationDaysService.getVacationDaysLeft(List.of(account2013), year2013, List.of(account2014)))
            .thenReturn(Map.of(account2013, new HolidayAccountVacationDays(account2013, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Optional<Account> account2013 = Optional.empty();
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(account2013);

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(new BigDecimal("2"))
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationSameYearAndNotEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.empty());

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testCheckApplicationSameYearAndExactEnoughDaysLeft() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.empty());

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ONE)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsToMuch() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        final Optional<Account> account2012 = Optional.of(account);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(account2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expiryDate13 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFrom13, validTo13, true, expiryDate13, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        final Year year = Year.of(2012);
        // vacation days would be left after this application for leave
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(TEN)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of(account2013)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void testCheckApplicationRemainingVacationDaysLeftNotUsedAfterApril() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationStartDate = LocalDate.of(2012, AUGUST, 6);
        final LocalDate applicationEndDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationEndDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStartDate, applicationEndDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(applicationStartDate);
        applicationForLeaveToCheck.setEndDate(applicationEndDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.empty());

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ONE)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

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
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);

        // enough vacation days for this application for leave, but none would be left
        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account2012 = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expiryDate13 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFrom13, validTo13, true, expiryDate13, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        // vacation days would be left after this application for leave
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(4))
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(5))
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2012), Year.of(2012), List.of(account2013)))
            .thenReturn(Map.of(account2012, new HolidayAccountVacationDays(account2012, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    void testCheckApplicationLastYear() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2012, AUGUST, 20);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());
        when(accountService.getHolidaysAccount(2011, person)).thenReturn(Optional.of(account));
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account)).thenReturn(account);

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ONE)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

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

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expireDate = LocalDate.of(2012, APRIL, 1);
        final Account account2012 = new Account(person, validFrom, validTo, true, expireDate, TEN, TEN, TEN, "comment");
        final Optional<Account> maybeAccount2012 = Optional.of(account2012);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(maybeAccount2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expireDate13 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFrom13, validTo13, true, expireDate13, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        final LocalDate validFrom14 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo14 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expireDate14 = LocalDate.of(2014, APRIL, 1);
        final Account account2014 = new Account(person, validFrom14, validTo14, true, expireDate14, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));

        final Year year = Year.of(2012);

        // vacation days would be left after this application for leave
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(5))
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(4))
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2012), year, List.of(account2013)))
            .thenReturn(Map.of(account2012, new HolidayAccountVacationDays(account2012, vacationDaysLeft, vacationDaysLeft)));

        when(vacationDaysService.getVacationDaysLeft(List.of(account2013), year.plusYears(1), List.of(account2014)))
            .thenReturn(Map.of(account2013, new HolidayAccountVacationDays(account2013, vacationDaysLeft, vacationDaysLeft)));

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isTrue();
    }

    @Test
    void testCheckApplicationOneDayIsNotOkayOverAYear() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationStartDate = LocalDate.of(2012, DECEMBER, 30);
        final LocalDate applicationEndDate = LocalDate.of(2013, JANUARY, 2);

        final WorkingTime workingTime = new WorkingTime(person, applicationStartDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStartDate, applicationEndDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        applicationForLeaveToCheck.setStartDate(applicationStartDate);
        applicationForLeaveToCheck.setEndDate(applicationEndDate);

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, APRIL, 1);
        final Account account2012 = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        final Optional<Account> maybeAccount2012 = Optional.of(account2012);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(maybeAccount2012);

        final LocalDate validFrom13 = LocalDate.of(2013, JANUARY, 1);
        final LocalDate validTo13 = LocalDate.of(2013, DECEMBER, 31);
        final LocalDate expireDate13 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFrom13, validTo13, true, expireDate13, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        final Year year = Year.of(2012);
        // vacation days would be left after this application for leave
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(5))
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(5))
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2012), year, List.of(account2013)))
            .thenReturn(Map.of(account2012, new HolidayAccountVacationDays(account2012, vacationDaysLeft, vacationDaysLeft)));

        assertThat(sut.checkApplication(applicationForLeaveToCheck)).isFalse();
    }

    /**
     * https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues/447
     */
    @Test
    void testCheckApplicationNextYearUsingRemainingAlready() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationStartDate = LocalDate.of(2012, AUGUST, 20);
        final LocalDate applicationEndDate = LocalDate.of(2012, AUGUST, 30);

        final WorkingTime workingTime = new WorkingTime(person, applicationStartDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStartDate, applicationEndDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);
        // nine days
        applicationForLeaveToCheck.setStartDate(applicationStartDate);
        applicationForLeaveToCheck.setEndDate(applicationEndDate);

        final Year year = Year.of(2012);
        final LocalDate validFrom12 = year.atDay(1);
        final LocalDate validTo12 = validFrom12.with(lastDayOfYear());
        final LocalDate expireDate12 = LocalDate.of(2012, APRIL, 1);
        final Account account = new Account(person, validFrom12, validTo12, true, expireDate12, TEN, ZERO, ZERO, "");
        final Optional<Account> maybeAccount2012 = Optional.of(account);
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(maybeAccount2012);

        // here we set up 2013 to have 10 days remaining vacation available from 2012,
        // if those have already been used up, we cannot spend them in 2012 as well
        final LocalDate validFrom13 = Year.of(2013).atDay(1);
        final LocalDate validTo13 = validFrom13.with(lastDayOfYear());
        final LocalDate expireDate13 = LocalDate.of(2013, APRIL, 1);
        final Account account2013 = new Account(person, validFrom13, validTo13, true, expireDate13, TEN, TEN, TEN, "");
        account2013.setActualVacationDays(account2013.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));

        // this year still has all ten days (but 3 of them used up next year, see above)
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(BigDecimal.valueOf(3))
            .withVacationDaysUsedNextYear(TEN)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of(account2013)))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    void ensureApplicationBeforeExpiryDateIsValidWithVacationLeftBeforeExpiryDateAndNoVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2022, FEBRUARY, 1);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(TEN)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureApplicationBeforeExpiryDateIsValidWithNotEnoughRemainingVacationDaysAndOneDayAnnualVacation() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationStart = LocalDate.of(2022, FEBRUARY, 1);
        final LocalDate applicationEnd = LocalDate.of(2022, FEBRUARY, 2);

        final WorkingTime workingTime = new WorkingTime(person, applicationStart, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStart, applicationEnd), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(applicationStart);
        applicationForLeaveToCheck.setEndDate(applicationEnd);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ONE)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureApplicationAfterAprilNotValidWithNoVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2022, APRIL, 1);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(TEN)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureApplicationAfterAprilIsValidWithRemainingNotExpiringVacationDays() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationDate = LocalDate.of(2022, APRIL, 1);

        final WorkingTime workingTime = new WorkingTime(person, applicationDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationDate, applicationDate), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(applicationDate);
        applicationForLeaveToCheck.setEndDate(applicationDate);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, ONE, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(ONE)
            .notExpiring(ONE)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(TEN)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureApplicationBeforeAndAfterAprilIsValidWithVacationLeftBeforeExpiryDateAndVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate applicationStart = LocalDate.of(2022, MARCH, 31);
        final LocalDate applicationEnd = LocalDate.of(2022, APRIL, 1);

        final WorkingTime workingTime = new WorkingTime(person, applicationStart, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStart, applicationEnd), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(applicationStart);
        applicationForLeaveToCheck.setEndDate(applicationEnd);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isTrue();
    }

    @Test
    void ensureApplicationBeforeAndAfterAprilNotInvalidWithVacationLeftBeforeExpiryDateAndNoVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate start = LocalDate.of(2022, MARCH, 31);
        final LocalDate end = LocalDate.of(2022, APRIL, 1);

        final WorkingTime workingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(start, end), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(start);
        applicationForLeaveToCheck.setEndDate(end);

        final Year year2022 = Year.of(2022);
        final LocalDate validFrom2022 = year2022.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, TEN, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(TEN)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year2022, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isFalse();
    }

    @Test
    void ensureApplicationBeforeAndAfterAprilNotValidWithNoVacationLeftBeforeExpiryDateAndVacationLeftAfterApril() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate start = LocalDate.of(2022, MARCH, 31);
        final LocalDate end = LocalDate.of(2022, APRIL, 1);

        final WorkingTime workingTime = new WorkingTime(person, start, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(start, end), workingTime));

        final Application applicationForLeaveToCheck = createApplicationStub(person);

        applicationForLeaveToCheck.setStartDate(start);
        applicationForLeaveToCheck.setEndDate(end);

        final Year year = Year.of(2022);
        final LocalDate validFrom2022 = year.atDay(1);
        final LocalDate validTo2022 = validFrom2022.with(lastDayOfYear());
        final LocalDate expireDate2022 = LocalDate.of(2022, APRIL, 1);
        final Account account2022 = new Account(person, validFrom2022, validTo2022, true, expireDate2022, ZERO, TEN, ZERO, "");
        account2022.setActualVacationDays(account2022.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2022, person)).thenReturn(Optional.of(account2022));

        final LocalDate validFrom2023 = Year.of(2023).atDay(1);
        final LocalDate validTo2023 = validFrom2023.with(lastDayOfYear());
        final LocalDate expireDate2023 = LocalDate.of(2023, APRIL, 1);
        final Account account2023 = new Account(person, validFrom2023, validTo2023, true, expireDate2023, TEN, TEN, ZERO, "");
        account2023.setActualVacationDays(account2023.getAnnualVacationDays());
        when(accountService.getHolidaysAccount(2023, person)).thenReturn(Optional.of(account2023));

        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(ZERO)
            .withRemainingVacation(TEN)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account2022), year, List.of(account2023)))
            .thenReturn(Map.of(account2022, new HolidayAccountVacationDays(account2022, vacationDaysLeft, vacationDaysLeft)));

        final boolean actual = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(actual).isFalse();
    }

    @Test
    void testCheckApplicationSameYearAndWithExpiryDateOnFirstJanuaryIsOk() {
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate applicationStartDate = LocalDate.of(2012, AUGUST, 20);
        final LocalDate applicationEndDate = LocalDate.of(2012, AUGUST, 21);

        final WorkingTime workingTime = new WorkingTime(person, applicationStartDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(applicationStartDate, applicationEndDate), workingTime));

        final Application applicationForLeaveToCheckSaved = new Application();
        applicationForLeaveToCheckSaved.setId(10L);
        applicationForLeaveToCheckSaved.setStartDate(applicationStartDate);
        applicationForLeaveToCheckSaved.setEndDate(applicationStartDate);
        applicationForLeaveToCheckSaved.setPerson(person);
        applicationForLeaveToCheckSaved.setDayLength(FULL);
        when(applicationService.getApplicationById(10L)).thenReturn(Optional.of(applicationForLeaveToCheckSaved));

        final LocalDate validFrom = LocalDate.of(2012, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2012, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2012, JANUARY, 1);
        final Account account = new Account(person, validFrom, validTo, true, expiryDate, TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        final Year year = Year.of(2012);
        final VacationDaysLeft vacationDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(TEN)
            .withRemainingVacation(BigDecimal.valueOf(20))
            .notExpiring(TEN)
            .forUsedVacationDaysBeforeExpiry(TEN)
            .forUsedVacationDaysAfterExpiry(TEN)
            .build();
        when(vacationDaysService.getVacationDaysLeft(List.of(account), year, List.of()))
            .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft, vacationDaysLeft)));

        final Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setId(10L);
        applicationForLeaveToCheck.setStartDate(applicationStartDate);
        applicationForLeaveToCheck.setEndDate(applicationEndDate);
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(FULL);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    private Application createApplicationStub(Person person) {
        final Application application = new Application();
        application.setPerson(person);
        application.setDayLength(FULL);
        return application;
    }

    private HolidayManager getHolidayManager() {
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
    }
}
