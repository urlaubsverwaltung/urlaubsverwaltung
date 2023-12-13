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
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.SEPTEMBER;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@ExtendWith(MockitoExtension.class)
class VacationDaysServiceTest {

    private VacationDaysService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysCountService workDaysCountService;

    @BeforeEach
    void setUp() {
        sut = new VacationDaysService(workDaysCountService, applicationService, Clock.systemUTC());
    }

    @Test
    void testGetDaysExpiryDate() {

        final Person person = anyPerson();

        final LocalDate firstMilestone = LocalDate.of(2012, JANUARY, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        final Application a1 = anyApplication(person);
        a1.setStartDate(LocalDate.of(2011, DECEMBER, 29));
        a1.setEndDate(LocalDate.of(2012, JANUARY, 3));
        a1.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(a1.getDayLength(), firstMilestone, a1.getEndDate(), a1.getPerson())).thenReturn(BigDecimal.valueOf(2L));

        // 1 day
        final Application a11 = anyApplication(person);
        a11.setStartDate(LocalDate.of(2012, MARCH, 1));
        a11.setEndDate(LocalDate.of(2012, MARCH, 1));
        a11.setStatus(TEMPORARY_ALLOWED);
        when(workDaysCountService.getWorkDaysCount(a11.getDayLength(), a11.getStartDate(), a11.getEndDate(), a11.getPerson())).thenReturn(BigDecimal.valueOf(1L));

        // 5 days
        final Application a2 = anyApplication(person);
        a2.setStartDate(LocalDate.of(2012, MARCH, 12));
        a2.setEndDate(LocalDate.of(2012, MARCH, 16));
        a2.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(a2.getDayLength(), a2.getStartDate(), a2.getEndDate(), a2.getPerson())).thenReturn(BigDecimal.valueOf(5L));

        // 4 days
        final Application a3 = anyApplication(person);
        a3.setStartDate(LocalDate.of(2012, FEBRUARY, 6));
        a3.setEndDate(LocalDate.of(2012, FEBRUARY, 9));
        a3.setStatus(WAITING);
        when(workDaysCountService.getWorkDaysCount(a3.getDayLength(), a3.getStartDate(), a3.getEndDate(), a3.getPerson())).thenReturn(BigDecimal.valueOf(4L));

        // 1 day
        final Application a4 = anyApplication(person);
        a4.setStartDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setEndDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        when(workDaysCountService.getWorkDaysCount(a4.getDayLength(), a4.getStartDate(), a4.getEndDate(), a4.getPerson())).thenReturn(BigDecimal.valueOf(1L));

        // 6 days at all: 2 before April + 4 after April
        final Application a5 = anyApplication(person);
        a5.setStartDate(LocalDate.of(2012, MARCH, 29));
        a5.setEndDate(LocalDate.of(2012, APRIL, 5));
        a5.setStatus(WAITING);
        when(workDaysCountService.getWorkDaysCount(a5.getDayLength(), a5.getStartDate(), lastMilestone, a5.getPerson())).thenReturn(BigDecimal.valueOf(2L));

        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(any(LocalDate.class), any(LocalDate.class), any(Person.class), eq(statuses), eq(HOLIDAY)))
            .thenReturn(List.of(a1, a2, a3, a4, a5, a11));

        // must be: 2 + 1 + 5 + 4 + 1 + 2 = 15
        final BigDecimal days = sut.getUsedVacationDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        assertThat(days).isEqualByComparingTo(new BigDecimal("15.0"));
    }

    @Test
    void testGetDaysAfterApril() {

        final Person person = anyPerson();

        final LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        // 4 days at all: 2.5 before January + 2 after January
        final Application a1 = anyApplication(person);
        a1.setStartDate(LocalDate.of(2012, DECEMBER, 27));
        a1.setEndDate(LocalDate.of(2013, JANUARY, 3));
        a1.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(a1.getDayLength(), a1.getStartDate(), lastMilestone, a1.getPerson())).thenReturn(BigDecimal.valueOf(2.5));

        // 5 days
        final Application a2 = anyApplication(person);
        a2.setStartDate(LocalDate.of(2012, SEPTEMBER, 3));
        a2.setEndDate(LocalDate.of(2012, SEPTEMBER, 7));
        a2.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(a2.getDayLength(), a2.getStartDate(), a2.getEndDate(), a2.getPerson())).thenReturn(BigDecimal.valueOf(5L));

        // 6 days at all: 2 before April + 4 after April
        final Application a4 = anyApplication(person);
        a4.setStartDate(LocalDate.of(2012, MARCH, 29));
        a4.setEndDate(LocalDate.of(2012, APRIL, 5));
        a4.setStatus(WAITING);
        when(workDaysCountService.getWorkDaysCount(a4.getDayLength(), firstMilestone, a4.getEndDate(), a4.getPerson())).thenReturn(BigDecimal.valueOf(4L));

        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(any(LocalDate.class), any(LocalDate.class), eq(person), eq(statuses), eq(HOLIDAY)))
            .thenReturn(List.of(a1, a2, a4));

        // must be: 2.5 + 5 + 4 = 11.5
        final BigDecimal days = sut.getUsedVacationDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        assertThat(days).isEqualTo(new BigDecimal("11.5"));
    }

    @Test
    void testGetVacationDaysLeft() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20Days.setEndDate(LocalDate.of(2022, MAY, 3));
        application20Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20Days.getDayLength(), application20Days.getStartDate(), application20Days.getEndDate(), application20Days.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20Days));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.empty());
        assertThat(vacationDaysLeft.getVacationDays()).isEqualByComparingTo(new BigDecimal(12L));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
    }

    @Test
    void testGetVacationDaysLeftWithoutExpire() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson()))
            .thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20Days.setEndDate(LocalDate.of(2022, MAY, 3));
        application20Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20Days.getDayLength(), application20Days.getStartDate(), application20Days.getEndDate(), application20Days.getPerson()))
            .thenReturn(BigDecimal.valueOf(20L));

        final LocalDate from = LocalDate.of(2022, 1, 1);
        final LocalDate to = LocalDate.of(2022, 12, 31);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(from, to, person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days, application20Days));

        final Account account = anyAccount(person, Year.of(2022));
        account.setDoRemainingVacationDaysExpireLocally(false);
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.empty());
        assertThat(vacationDaysLeft.getVacationDays()).isEqualByComparingTo(new BigDecimal(12L));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualByComparingTo(ZERO);
    }

    @Test
    void testGetVacationDaysLeftWithRemainingAlreadyUsed() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application20Days = anyApplication(person);
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20Days.setEndDate(LocalDate.of(2022, MAY, 3));
        application20Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20Days.getDayLength(), application20Days.getStartDate(), application20Days.getEndDate(), application20Days.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20Days));

        final Application application4DaysIn2023 = anyApplication(person);
        application4DaysIn2023.setStartDate(LocalDate.of(2023, JANUARY, 3));
        application4DaysIn2023.setEndDate(LocalDate.of(2023, JANUARY, 7));
        application4DaysIn2023.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4DaysIn2023.getDayLength(), application4DaysIn2023.getStartDate(), application4DaysIn2023.getEndDate(), application4DaysIn2023.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4DaysIn2023));

        final Application application20DaysIn2023 = anyApplication(person);
        application20DaysIn2023.setStartDate(LocalDate.of(2023, APRIL, 2));
        application20DaysIn2023.setEndDate(LocalDate.of(2023, MAY, 3));
        application20DaysIn2023.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20DaysIn2023.getDayLength(), application20DaysIn2023.getStartDate(), application20DaysIn2023.getEndDate(), application20DaysIn2023.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20DaysIn2023));

        // 36 Total, using 24, so 12 left
        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        final Account accountNextYear = anyAccount(person, Year.of(2023));
        accountNextYear.setAnnualVacationDays(new BigDecimal("12"));
        accountNextYear.setActualVacationDays(new BigDecimal("12"));
        accountNextYear.setRemainingVacationDays(new BigDecimal("20"));
        accountNextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        accountNextYear.setDoRemainingVacationDaysExpireLocally(true);

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.of(accountNextYear));
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(vacationDaysLeft.getVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
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
        application20DaysBeforeExpiryDate.setEndDate(LocalDate.of(2022, JANUARY, 28));
        application20DaysBeforeExpiryDate.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20DaysBeforeExpiryDate.getDayLength(), application20DaysBeforeExpiryDate.getStartDate(), application20DaysBeforeExpiryDate.getEndDate(), application20DaysBeforeExpiryDate.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20DaysBeforeExpiryDate));

        final Application application20DaysAfterApril = anyApplication(person);
        application20DaysAfterApril.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20DaysAfterApril.setEndDate(LocalDate.of(2022, MAY, 3));
        application20DaysAfterApril.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application20DaysAfterApril.getDayLength(), application20DaysAfterApril.getStartDate(), application20DaysAfterApril.getEndDate(), application20DaysAfterApril.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20DaysAfterApril));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("10"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("0"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(account);
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(TEN);
    }

    @Test
    void testGetTotalVacationDaysForPastYear() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application1Day = anyApplication(person);
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application1Day.getDayLength(), application1Day.getStartDate(), application1Day.getEndDate(), application1Day.getPerson())).thenReturn(BigDecimal.valueOf(1L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application1Day));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("31"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearExpiryDate() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application1Day = anyApplication(person);
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application1Day.getDayLength(), application1Day.getStartDate(), application1Day.getEndDate(), application1Day.getPerson())).thenReturn(BigDecimal.valueOf(1L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application1Day));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("32"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearAfterApril() {

        final Person person = anyPerson();

        final Application application4Days = anyApplication(person);
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application3Days = anyApplication(person);
        application3Days.setStartDate(LocalDate.of(2022, MAY, 2));
        application3Days.setEndDate(LocalDate.of(2022, MAY, 5));
        application3Days.setStatus(ALLOWED);
        when(workDaysCountService.getWorkDaysCount(application3Days.getDayLength(), application3Days.getStartDate(), application3Days.getEndDate(), application3Days.getPerson())).thenReturn(BigDecimal.valueOf(3L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application3Days));

        final Account account = anyAccount(person, Year.of(2022));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));
        account.setDoRemainingVacationDaysExpireLocally(true);

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("30"));
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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = buildWorkingTimeByDate(firstDayOfYear, lastDayOfYear, date -> new WorkingDayInformation(FULL, WORKDAY, WORKDAY));
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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

        final List<ApplicationStatus> applicationStatus = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getForStatesAndPerson(applicationStatus, List.of(person), firstDayOfYear, lastDayOfYear))
            .thenReturn(List.of(application));

        final Map<LocalDate, WorkingDayInformation> workingTimeByDate = workingTimeMondayToFriday(firstDayOfYear, lastDayOfYear);
        final WorkingTimeCalendar workingTimeCalendar = new WorkingTimeCalendar(workingTimeByDate);

        final Map<Account, HolidayAccountVacationDays> actual =
            sut.getVacationDaysLeft(List.of(account), Map.of(person, workingTimeCalendar), new DateRange(firstDayOfYear, lastDayOfYear));

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
        account.setExpiryDate(LocalDate.of(year.getValue(), APRIL, 1));
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
