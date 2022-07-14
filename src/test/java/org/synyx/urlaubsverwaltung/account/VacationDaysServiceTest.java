package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.SEPTEMBER;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypeEntity;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate firstMilestone = LocalDate.of(2012, JANUARY, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        final Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2011, DECEMBER, 29));
        a1.setEndDate(LocalDate.of(2012, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setStatus(ALLOWED);
        a1.setVacationType(createVacationTypeEntity(HOLIDAY));
        a1.setPerson(person);
        when(workDaysCountService.getWorkDaysCount(a1.getDayLength(), firstMilestone, a1.getEndDate(), a1.getPerson())).thenReturn(BigDecimal.valueOf(2L));

        // 1 day
        final Application a11 = new Application();
        a11.setStartDate(LocalDate.of(2012, MARCH, 1));
        a11.setEndDate(LocalDate.of(2012, MARCH, 1));
        a11.setDayLength(FULL);
        a11.setStatus(TEMPORARY_ALLOWED);
        a11.setVacationType(createVacationTypeEntity(HOLIDAY));
        a11.setPerson(person);
        when(workDaysCountService.getWorkDaysCount(a11.getDayLength(), a11.getStartDate(), a11.getEndDate(), a11.getPerson())).thenReturn(BigDecimal.valueOf(1L));

        // 5 days
        final Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, MARCH, 12));
        a2.setEndDate(LocalDate.of(2012, MARCH, 16));
        a2.setDayLength(FULL);
        a2.setStatus(ALLOWED);
        a2.setVacationType(createVacationTypeEntity(HOLIDAY));
        a2.setPerson(person);
        when(workDaysCountService.getWorkDaysCount(a2.getDayLength(), a2.getStartDate(), a2.getEndDate(), a2.getPerson())).thenReturn(BigDecimal.valueOf(5L));

        // 4 days
        final Application a3 = new Application();
        a3.setStartDate(LocalDate.of(2012, FEBRUARY, 6));
        a3.setEndDate(LocalDate.of(2012, FEBRUARY, 9));
        a3.setDayLength(FULL);
        a3.setStatus(WAITING);
        a3.setVacationType(createVacationTypeEntity(HOLIDAY));
        a3.setPerson(person);
        when(workDaysCountService.getWorkDaysCount(a3.getDayLength(), a3.getStartDate(), a3.getEndDate(), a3.getPerson())).thenReturn(BigDecimal.valueOf(4L));

        // 1 day
        final Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setEndDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setDayLength(FULL);
        a4.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        a4.setVacationType(createVacationTypeEntity(HOLIDAY));
        a4.setPerson(person);
        when(workDaysCountService.getWorkDaysCount(a4.getDayLength(), a4.getStartDate(), a4.getEndDate(), a4.getPerson())).thenReturn(BigDecimal.valueOf(1L));

        // 6 days at all: 2 before April + 4 after April
        final Application a5 = new Application();
        a5.setStartDate(LocalDate.of(2012, MARCH, 29));
        a5.setEndDate(LocalDate.of(2012, APRIL, 5));
        a5.setDayLength(FULL);
        a5.setStatus(WAITING);
        a5.setVacationType(createVacationTypeEntity(HOLIDAY));
        a5.setPerson(person);
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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        // 4 days at all: 2.5 before January + 2 after January
        final Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2012, DECEMBER, 27));
        a1.setEndDate(LocalDate.of(2013, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setPerson(person);
        a1.setStatus(ALLOWED);
        a1.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(a1.getDayLength(), a1.getStartDate(), lastMilestone, a1.getPerson())).thenReturn(BigDecimal.valueOf(2.5));

        // 5 days
        final Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, SEPTEMBER, 3));
        a2.setEndDate(LocalDate.of(2012, SEPTEMBER, 7));
        a2.setDayLength(FULL);
        a2.setPerson(person);
        a2.setStatus(ALLOWED);
        a2.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(a2.getDayLength(), a2.getStartDate(), a2.getEndDate(), a2.getPerson())).thenReturn(BigDecimal.valueOf(5L));

        // 6 days at all: 2 before April + 4 after April
        final Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, MARCH, 29));
        a4.setEndDate(LocalDate.of(2012, APRIL, 5));
        a4.setDayLength(FULL);
        a4.setPerson(person);
        a4.setStatus(WAITING);
        a4.setVacationType(createVacationTypeEntity(HOLIDAY));
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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application4Days = new Application();
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setDayLength(FULL);
        application4Days.setPerson(person);
        application4Days.setStatus(ALLOWED);
        application4Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application20Days = new Application();
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20Days.setEndDate(LocalDate.of(2022, MAY, 3));
        application20Days.setDayLength(FULL);
        application20Days.setPerson(person);
        application20Days.setStatus(ALLOWED);
        application20Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application20Days.getDayLength(), application20Days.getStartDate(), application20Days.getEndDate(), application20Days.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20Days));

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
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

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application4Days = new Application();
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 7));
        application4Days.setDayLength(FULL);
        application4Days.setPerson(person);
        application4Days.setStatus(ALLOWED);
        application4Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application20Days = new Application();
        application20Days.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20Days.setEndDate(LocalDate.of(2022, MAY, 3));
        application20Days.setDayLength(FULL);
        application20Days.setPerson(person);
        application20Days.setStatus(ALLOWED);
        application20Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application20Days.getDayLength(), application20Days.getStartDate(), application20Days.getEndDate(), application20Days.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20Days));

        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days, application20Days));

        final Application application4DaysIn2023 = new Application();
        application4DaysIn2023.setStartDate(LocalDate.of(2023, JANUARY, 3));
        application4DaysIn2023.setEndDate(LocalDate.of(2023, JANUARY, 7));
        application4DaysIn2023.setDayLength(FULL);
        application4DaysIn2023.setPerson(person);
        application4DaysIn2023.setStatus(ALLOWED);
        application4DaysIn2023.setVacationType(createVacationTypeEntity(HOLIDAY));

        final Application application20DaysIn2023 = new Application();
        application20DaysIn2023.setStartDate(LocalDate.of(2023, APRIL, 2));
        application20DaysIn2023.setEndDate(LocalDate.of(2023, MAY, 3));
        application20DaysIn2023.setDayLength(FULL);
        application20DaysIn2023.setPerson(person);
        application20DaysIn2023.setStatus(ALLOWED);
        application20DaysIn2023.setVacationType(createVacationTypeEntity(HOLIDAY));

        // 36 Total, using 24, so 12 left
        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        final Account nextYear = new Account();
        nextYear.setPerson(person);
        nextYear.setValidFrom(LocalDate.of(2023, 1, 1));
        nextYear.setExpiryDate(LocalDate.of(2023, 4, 1));
        nextYear.setAnnualVacationDays(new BigDecimal("12"));
        nextYear.setActualVacationDays(new BigDecimal("12"));
        nextYear.setRemainingVacationDays(new BigDecimal("20"));
        nextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.of(nextYear));
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(vacationDaysLeft.getVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualByComparingTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualByComparingTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfEmptyAccount() {
        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(Optional.empty());
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfZeroRemainingVacationDays() {

        final Account account = new Account();
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setRemainingVacationDays(ZERO);

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(Optional.of(account));
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfOneRemainingVacationDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application20DaysBeforeExpiryDate = new Application();
        application20DaysBeforeExpiryDate.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application20DaysBeforeExpiryDate.setEndDate(LocalDate.of(2022, JANUARY, 28));
        application20DaysBeforeExpiryDate.setDayLength(FULL);
        application20DaysBeforeExpiryDate.setPerson(person);
        application20DaysBeforeExpiryDate.setStatus(ALLOWED);
        application20DaysBeforeExpiryDate.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application20DaysBeforeExpiryDate.getDayLength(), application20DaysBeforeExpiryDate.getStartDate(), application20DaysBeforeExpiryDate.getEndDate(), application20DaysBeforeExpiryDate.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20DaysBeforeExpiryDate));

        final Application application20DaysAfterApril = new Application();
        application20DaysAfterApril.setStartDate(LocalDate.of(2022, APRIL, 2));
        application20DaysAfterApril.setEndDate(LocalDate.of(2022, MAY, 3));
        application20DaysAfterApril.setDayLength(FULL);
        application20DaysAfterApril.setPerson(person);
        application20DaysAfterApril.setStatus(ALLOWED);
        application20DaysAfterApril.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application20DaysAfterApril.getDayLength(), application20DaysAfterApril.getStartDate(), application20DaysAfterApril.getEndDate(), application20DaysAfterApril.getPerson())).thenReturn(BigDecimal.valueOf(20L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application20DaysAfterApril));

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("10"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("0"));

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getUsedRemainingVacationDays(Optional.of(account));
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(TEN);
    }

    @Test
    void testGetTotalVacationDaysForPastYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application4Days = new Application();
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setDayLength(FULL);
        application4Days.setPerson(person);
        application4Days.setStatus(ALLOWED);
        application4Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application1Day = new Application();
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setDayLength(FULL);
        application1Day.setPerson(person);
        application1Day.setStatus(ALLOWED);
        application1Day.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application1Day.getDayLength(), application1Day.getStartDate(), application1Day.getEndDate(), application1Day.getPerson())).thenReturn(BigDecimal.valueOf(1L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application1Day));

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("31"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearExpiryDate() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application4Days = new Application();
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setDayLength(FULL);
        application4Days.setPerson(person);
        application4Days.setStatus(ALLOWED);
        application4Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application1Day = new Application();
        application1Day.setStartDate(LocalDate.of(2022, MAY, 2));
        application1Day.setEndDate(LocalDate.of(2022, MAY, 2));
        application1Day.setDayLength(FULL);
        application1Day.setPerson(person);
        application1Day.setStatus(ALLOWED);
        application1Day.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application1Day.getDayLength(), application1Day.getStartDate(), application1Day.getEndDate(), application1Day.getPerson())).thenReturn(BigDecimal.valueOf(1L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application1Day));

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("32"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearAfterApril() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application application4Days = new Application();
        application4Days.setStartDate(LocalDate.of(2022, JANUARY, 3));
        application4Days.setEndDate(LocalDate.of(2022, JANUARY, 6));
        application4Days.setDayLength(FULL);
        application4Days.setPerson(person);
        application4Days.setStatus(ALLOWED);
        application4Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application4Days.getDayLength(), application4Days.getStartDate(), application4Days.getEndDate(), application4Days.getPerson())).thenReturn(BigDecimal.valueOf(4L));
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application4Days));

        final Application application3Days = new Application();
        application3Days.setStartDate(LocalDate.of(2022, MAY, 2));
        application3Days.setEndDate(LocalDate.of(2022, MAY, 5));
        application3Days.setDayLength(FULL);
        application3Days.setPerson(person);
        application3Days.setStatus(ALLOWED);
        application3Days.setVacationType(createVacationTypeEntity(HOLIDAY));
        when(workDaysCountService.getWorkDaysCount(application3Days.getDayLength(), application3Days.getStartDate(), application3Days.getEndDate(), application3Days.getPerson())).thenReturn(BigDecimal.valueOf(3L));
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 12, 31), person, statuses, HOLIDAY))
            .thenReturn(List.of(application3Days));

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(2022, 1, 1));
        account.setExpiryDate(LocalDate.of(2022, 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("30"));
    }

    @Test
    void ensureVacationDaysLeftStartAfterFirstDayOfAprilUsesOnlyApplicationsAfterApril() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate today = LocalDate.now();
        final LocalDate start = YearMonth.of(today.getYear(), MAY).atDay(1);
        final LocalDate end = today.with(lastDayOfYear());

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(today.getYear(), 1, 1));
        account.setExpiryDate(LocalDate.of(today.getYear(), 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        sut.getVacationDaysLeft(start, end, account, Optional.empty());

        // called only one time there are no days before april and no next year account
        verify(applicationService).getApplicationsForACertainPeriodAndPersonAndVacationCategory(eq(start), eq(end), any(), any(), any());
    }

    @Test
    void ensureVacationDaysLeftEndBeforeFirstDayOfAprilUsesOnlyApplicationsExpiryDate() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate today = LocalDate.now();
        final LocalDate start = today.with(firstDayOfYear());
        final LocalDate end = YearMonth.of(today.getYear(), FEBRUARY).atDay(1);

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(today.getYear(), 1, 1));
        account.setExpiryDate(LocalDate.of(today.getYear(), 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        sut.getVacationDaysLeft(start, end, account, Optional.empty());

        // called only one time there are no days after april and no next year account
        verify(applicationService).getApplicationsForACertainPeriodAndPersonAndVacationCategory(eq(start), eq(end), any(), any(), any());
    }

    @Test
    void ensureUsesRemainingVacationDaysWithInconsistentTimeRangeReturnsZero() {
        final LocalDate today = LocalDate.now();
        final LocalDate start = today.with(lastDayOfYear());
        final LocalDate end = today.with(firstDayOfYear());

        assertThat(sut.getUsedRemainingVacationDays(start, end, Optional.empty())).isEqualTo(ZERO);
    }

    @Test
    void ensureUsesRemainingVacationDaysWithNegativeRemainingUsedReturnsZero() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate today = LocalDate.now();
        final LocalDate start = today.with(firstDayOfYear());
        final LocalDate end = today.with(lastDayOfYear());

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.of(today.getYear(), 1, 1));
        account.setExpiryDate(LocalDate.of(today.getYear(), 4, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setActualVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        assertThat(sut.getUsedRemainingVacationDays(start, end, Optional.of(account))).isEqualTo(ZERO);
    }
}
