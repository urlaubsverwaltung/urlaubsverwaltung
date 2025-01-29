package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class VacationDaysServiceTest {

    private VacationDaysService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    @BeforeEach
    void setUp() {
        sut = new VacationDaysService(workingTimeCalendarService, applicationService, Clock.systemUTC());
    }

    @Test
    void testGetVacationDaysUsedOfZeroRemainingVacationDays() {

        final Account account = anyAccount(anyPerson(), Year.of(2022));
        account.setRemainingVacationDays(ZERO);

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(account);
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfOneRemainingVacationDays() {

        final Person person = anyPerson();

        final Application application20DaysBeforeExpiryDate = anyApplication(person);
        application20DaysBeforeExpiryDate.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application20DaysBeforeExpiryDate.setEndDate(LocalDate.of(2022, JANUARY, 22));
        application20DaysBeforeExpiryDate.setStatus(ALLOWED);

        final Application application20DaysAfterApril = anyApplication(person);
        application20DaysAfterApril.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20DaysAfterApril.setEndDate(LocalDate.of(2022, APRIL, 21));
        application20DaysAfterApril.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application20DaysBeforeExpiryDate, application20DaysAfterApril));

        final Year year = Year.of(2022);
        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("10"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("0"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(account);
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(TEN);
    }

    @Test
    void ensureToGetTotalLeftVacationDaysForPastYear() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);

        final Application application1Day = anyApplication(person);
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application1Day));

        final Year year = Year.of(2022);
        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        final BigDecimal leftDays = sut.getTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("31"));
    }

    @Test
    void ensureToGetTotalLeftVacationDaysForThisYearExpiryDate() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);

        final Application application1Day = anyApplication(person);
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application1Day));

        final Year year = Year.of(2022);
        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        final BigDecimal leftDays = sut.getTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("32"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearAfterApril() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);

        final Application application3Day = anyApplication(person);
        application3Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application3Day.setEndDate(LocalDate.of(2022, MAY, 4));
        application3Day.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application3Day));

        final Year year = Year.of(2022);
        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        final BigDecimal leftDays = sut.getTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("30"));
    }

    @Test
    void testGetVacationDaysLeft() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);

        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 1));
        application20Days.setEndDate(LocalDate.of(2022, APRIL, 20));
        application20Days.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application20Days));

        final Year year = Year.of(2022);
        final Account account = anyAccount(person, year);
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));
        assertThat(actual.get(account).vacationDaysYear().getVacationDays()).isEqualByComparingTo(new BigDecimal(12L));
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
    }

    @Test
    void testGetVacationDaysLeftWithoutExpire() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);

        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 1));
        application20Days.setEndDate(LocalDate.of(2022, APRIL, 20));
        application20Days.setStatus(ALLOWED);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application20Days));

        final Year year = Year.of(2022);
        final Account account = anyAccount(person, year);
        account.setDoRemainingVacationDaysExpireLocally(false);
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), year);
        assertThat(actual.get(account).vacationDaysYear().getVacationDays()).isEqualByComparingTo(new BigDecimal(12L));
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
    }

    @Test
    void testGetVacationDaysLeftWithRemainingAlreadyUsed() {
        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 4));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);
        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 1));
        application20Days.setEndDate(LocalDate.of(2022, APRIL, 20));
        application20Days.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31)))
            .thenReturn(List.of(application4Days, application20Days));

        final Application application4DaysIn2023 = anyApplication(person);
        application4DaysIn2023.setStartDate(LocalDate.of(2023, JANUARY, 4));
        application4DaysIn2023.setEndDate(LocalDate.of(2023, JANUARY, 7));
        application4DaysIn2023.setStatus(ALLOWED);
        final Application application20DaysIn2023 = anyApplication(person);
        application20DaysIn2023.setStartDate(LocalDate.of(2022, APRIL, 1));
        application20DaysIn2023.setEndDate(LocalDate.of(2022, APRIL, 20));
        application20DaysIn2023.setStatus(ALLOWED);
        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)))
            .thenReturn(List.of(application4DaysIn2023, application20DaysIn2023));

        // 36 Total, using 24, so 12 left
        final Year year = Year.of(2022);
        final Account account = anyAccount(person, year);
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        final Account accountNextYear = anyAccount(person, year.plusYears(1));
        accountNextYear.setAnnualVacationDays(new BigDecimal("12"));
        accountNextYear.setActualVacationDays(new BigDecimal("12"));
        accountNextYear.setRemainingVacationDays(new BigDecimal("20"));
        accountNextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        accountNextYear.setDoRemainingVacationDaysExpireLocally(true);

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(
            year.atDay(1),
            year.atDay(1).plusYears(1).with(lastDayOfYear()),
            date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, new WorkingTimeCalendar(workingTimeByDate)));
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year.plusYears(1))).thenReturn(Map.of(person, new WorkingTimeCalendar(workingTimeByDate)));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), year, List.of(accountNextYear));
        assertThat(actual.get(account).vacationDaysYear().getVacationDays()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(actual.get(account).vacationDaysYear().getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(actual.get(account).vacationDaysYear().getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
    }

    @ParameterizedTest
    @EnumSource(value = VacationCategory.class, names = {"SPECIALLEAVE", "UNPAIDLEAVE", "OVERTIME", "OTHER"})
    void ensureGetVacationDaysLeftIgnoresVacationTypeWithYear(VacationCategory category) {
        final Person person = anyPerson();

        final Year year = Year.of(2022);
        final LocalDate firstDayOfYear = LocalDate.of(year.getValue(), 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(10));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year.getValue(), JANUARY, 3));
        application.setEndDate(LocalDate.of(year.getValue(), JANUARY, 28));
        application.setVacationType(createVacationType(1L, category, new StaticMessageSource()));

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year)).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Year.of(2022));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(10))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @ParameterizedTest
    @EnumSource(value = VacationCategory.class, names = {"SPECIALLEAVE", "UNPAIDLEAVE", "OVERTIME", "OTHER"})
    void ensureGetVacationDaysLeftIgnoresVacationType(VacationCategory category) {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(10));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year, JANUARY, 3));
        application.setEndDate(LocalDate.of(year, JANUARY, 28));
        application.setVacationType(createVacationType(1L, category, new StaticMessageSource()));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(10))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithApplicationBeforeExpiryDate() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(10));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year, MARCH, 7));
        application.setEndDate(LocalDate.of(year, MARCH, 18));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(10))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(10))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithApplicationAfterExpiryDate() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(10));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year, JUNE, 6));
        application.setEndDate(LocalDate.of(year, JUNE, 17));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(10))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(10))
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithApplicationOverlappingExpiryDate() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(10));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year, MARCH, 28));
        application.setEndDate(LocalDate.of(year, APRIL, 8));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(6))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(6))
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithApplicationStartingInPreviousYear() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(5));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year - 1, DECEMBER, 27));
        application.setEndDate(LocalDate.of(year, JANUARY, 7));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(BigDecimal.valueOf(5))
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(BigDecimal.valueOf(5))
            .forUsedVacationDaysAfterExpiry(ZERO)
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithApplicationEndingInNextYear() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(2022, 1, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final Account account = anyAccount(person, Year.of(2022));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(0));

        final Application application = anyApplication(person);
        application.setStartDate(LocalDate.of(year, DECEMBER, 26));
        application.setEndDate(LocalDate.of(year + 1, JANUARY, 7));

        final List<ApplicationStatus> applicationStatus = activeStatuses();
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual = sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(5))
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureGetVacationDaysLeftWithHalfDayApplicationOnWorkingDayWithHalfPublicHoliday() {
        final Person person = anyPerson();

        final int year = 2022;
        final LocalDate firstDayOfYear = LocalDate.of(year, JANUARY, 1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final LocalDate christmas = LocalDate.of(year, DECEMBER, 24);

        final Account account = anyAccount(person, Year.of(year));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(BigDecimal.valueOf(0));

        final Application application = anyApplication(person);
        application.setDayLength(MORNING);
        application.setStartDate(christmas);
        application.setEndDate(christmas);

        when(applicationService.getForStatesAndPerson(activeStatuses(), List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(
            Map.of(christmas, new WorkingDayInformation(MORNING, WORKDAY, PUBLIC_HOLIDAY))
        );
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), Year.of(year))).thenReturn(Map.of(person, workingTimeCalendar));

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), new DateRange(firstDayOfYear, lastDayOfYear));

        final VacationDaysLeft expectedDaysLeft = VacationDaysLeft.builder()
            .withAnnualVacation(BigDecimal.valueOf(30))
            .withRemainingVacation(ZERO)
            .notExpiring(ZERO)
            .forUsedVacationDaysBeforeExpiry(ZERO)
            .forUsedVacationDaysAfterExpiry(BigDecimal.valueOf(0.5))
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(account)).satisfies(holidayAccountVacationDays -> {
            assertThat(holidayAccountVacationDays.account()).isEqualTo(account);
            assertThat(holidayAccountVacationDays.vacationDaysYear()).isEqualTo(expectedDaysLeft);
            assertThat(holidayAccountVacationDays.vacationDaysDateRange()).isEqualTo(expectedDaysLeft);
        });
    }

    @Test
    void ensureUsesRemainingVacationDaysWithNegativeRemainingUsedReturnsZero() {
        final Person person = anyPerson();

        final LocalDate today = LocalDate.now();

        final Account account = anyAccount(person, Year.of(today.getYear()));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        assertThat(sut.getUsedRemainingVacationDays(account)).isEqualTo(ZERO);
    }

    private Map<LocalDate, WorkingDayInformation> workingTimeMondayToFriday(LocalDate from, LocalDate to) {
        return buildWorkingTimeByDate(from, to, date ->
            weekend(date)
                ? new WorkingDayInformation(DayLength.ZERO, NO_WORKDAY, NO_WORKDAY)
                : new WorkingDayInformation(FULL, WORKDAY, WORKDAY)
        );
    }

    private Map<LocalDate, WorkingDayInformation> buildWorkingTimeByDate(LocalDate from, LocalDate to, Function<LocalDate, WorkingDayInformation> dayLengthProvider) {
        Map<LocalDate, WorkingDayInformation> map = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            map.put(date, dayLengthProvider.apply(date));
        }
        return map;
    }

    private boolean weekend(LocalDate date) {
        return date.getDayOfWeek().equals(SATURDAY) || date.getDayOfWeek().equals(SUNDAY);
    }

    private Person anyPerson() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        return person;
    }

    private static Account anyAccount(Person person, Year year) {
        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(year.getValue(), JANUARY, 1));
        account.setExpiryDateLocally(LocalDate.of(year.getValue(), APRIL, 1));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(ZERO);
        account.setRemainingVacationDaysNotExpiring(ZERO);
        account.setDoRemainingVacationDaysExpireGlobally(true);
        return account;
    }

    private static Application anyApplication(Person person) {
        final Application application = new Application();
        application.setId(1L);
        application.setPerson(person);
        application.setVacationType(createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setDayLength(FULL);
        return application;
    }
}
