package org.synyx.urlaubsverwaltung.account;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.BADEN_WUERTTEMBERG;

@ExtendWith(MockitoExtension.class)
class VacationDaysServiceTest {

    private VacationDaysService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;

    private WorkDaysCountService workDaysCountService;

    @BeforeEach
    void setUp() {

        workDaysCountService = new WorkDaysCountService(new PublicHolidaysService(settingsService, getHolidayManager()),
            workingTimeService, settingsService);

        sut = new VacationDaysService(workDaysCountService, applicationService, Clock.systemUTC());
    }

    @Test
    void testGetDaysBeforeApril() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN, BADEN_WUERTTEMBERG);
        List<DayOfWeek> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        when(settingsService.getSettings()).thenReturn(new Settings());

        final LocalDate firstMilestone = LocalDate.of(2012, JANUARY, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        final Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2011, DECEMBER, 29));
        a1.setEndDate(LocalDate.of(2012, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setStatus(ALLOWED);
        a1.setVacationType(createVacationType(HOLIDAY));
        a1.setPerson(person);

        // 5 days
        final Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, MARCH, 12));
        a2.setEndDate(LocalDate.of(2012, MARCH, 16));
        a2.setDayLength(FULL);
        a2.setStatus(ALLOWED);
        a2.setVacationType(createVacationType(HOLIDAY));
        a2.setPerson(person);

        // 4 days
        final Application a3 = new Application();
        a3.setStartDate(LocalDate.of(2012, FEBRUARY, 6));
        a3.setEndDate(LocalDate.of(2012, FEBRUARY, 9));
        a3.setDayLength(FULL);
        a3.setStatus(WAITING);
        a3.setVacationType(createVacationType(HOLIDAY));
        a3.setPerson(person);

        // 1 day
        final Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setEndDate(LocalDate.of(2012, FEBRUARY, 10));
        a4.setDayLength(FULL);
        a4.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        a4.setVacationType(createVacationType(HOLIDAY));
        a4.setPerson(person);

        // 6 days at all: 2 before April + 4 after April
        final Application a5 = new Application();
        a5.setStartDate(LocalDate.of(2012, MARCH, 29));
        a5.setEndDate(LocalDate.of(2012, APRIL, 5));
        a5.setDayLength(FULL);
        a5.setStatus(WAITING);
        a5.setVacationType(createVacationType(HOLIDAY));
        a5.setPerson(person);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(List.of(a1, a2, a3, a4, a5));

        // must be: 2 + 5 + 1 + 4 + 2 = 13
        final BigDecimal days = sut.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        assertThat(days).isEqualTo(new BigDecimal("14.0"));
    }

    @Test
    void testGetDaysAfterApril() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN, BADEN_WUERTTEMBERG);
        List<DayOfWeek> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        when(settingsService.getSettings()).thenReturn(new Settings());

        final LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        // 4 days at all: 2.5 before January + 2 after January
        final Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2012, DECEMBER, 27));
        a1.setEndDate(LocalDate.of(2013, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setPerson(person);
        a1.setStatus(ALLOWED);
        a1.setVacationType(createVacationType(HOLIDAY));

        // 5 days
        final Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, SEPTEMBER, 3));
        a2.setEndDate(LocalDate.of(2012, SEPTEMBER, 7));
        a2.setDayLength(FULL);
        a2.setPerson(person);
        a2.setStatus(ALLOWED);
        a2.setVacationType(createVacationType(HOLIDAY));

        // 6 days at all: 2 before April + 4 after April
        final Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, MARCH, 29));
        a4.setEndDate(LocalDate.of(2012, APRIL, 5));
        a4.setDayLength(FULL);
        a4.setPerson(person);
        a4.setStatus(WAITING);
        a4.setVacationType(createVacationType(HOLIDAY));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(List.of(a1, a2, a4));

        // must be: 2.5 + 5 + 4 = 11.5
        final BigDecimal days = sut.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        assertThat(days).isEqualTo(new BigDecimal("11.5"));
    }

    @Test
    void testGetDaysBetweenMilestonesWithInactiveApplicationsForLeaveAndOfOtherVacationTypeThanHoliday() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        final LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        final Application cancelledHoliday = new Application();
        cancelledHoliday.setVacationType(createVacationType(HOLIDAY));
        cancelledHoliday.setStatus(CANCELLED);

        final Application rejectedHoliday = new Application();
        rejectedHoliday.setVacationType(createVacationType(HOLIDAY));
        rejectedHoliday.setStatus(REJECTED);

        final Application waitingSpecialLeave = new Application();
        waitingSpecialLeave.setVacationType(createVacationType(SPECIALLEAVE));
        waitingSpecialLeave.setStatus(WAITING);

        final Application allowedSpecialLeave = new Application();
        allowedSpecialLeave.setVacationType(createVacationType(SPECIALLEAVE));
        allowedSpecialLeave.setStatus(ALLOWED);

        final Application waitingUnpaidLeave = new Application();
        waitingUnpaidLeave.setVacationType(createVacationType(UNPAIDLEAVE));
        waitingUnpaidLeave.setStatus(WAITING);

        final Application allowedUnpaidLeave = new Application();
        allowedUnpaidLeave.setVacationType(createVacationType(UNPAIDLEAVE));
        allowedUnpaidLeave.setStatus(ALLOWED);

        final Application waitingOvertime = new Application();
        waitingOvertime.setVacationType(createVacationType(OVERTIME));
        waitingOvertime.setStatus(WAITING);

        final Application allowedOvertime = new Application();
        allowedOvertime.setVacationType(createVacationType(OVERTIME));
        allowedOvertime.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(List.of(cancelledHoliday, rejectedHoliday, waitingSpecialLeave, allowedSpecialLeave,
                waitingUnpaidLeave, allowedUnpaidLeave, waitingOvertime, allowedOvertime));

        final BigDecimal days = sut.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        assertThat(days).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysLeft() {

        initCustomService("4", "20");

        final Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.empty());
        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(new BigDecimal("12"));
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualTo(ZERO);

    }

    @Test
    void testGetVacationDaysLeftWithRemainingAlreadyUsed() {

        initCustomService("4", "20");

        // 36 Total, using 24, so 12 left
        final Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        final Account nextYear = new Account();
        nextYear.setAnnualVacationDays(new BigDecimal("12"));
        nextYear.setVacationDays(new BigDecimal("12"));
        nextYear.setRemainingVacationDays(new BigDecimal("20"));
        nextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        final VacationDaysLeft vacationDaysLeft = sut.getVacationDaysLeft(account, Optional.of(nextYear));
        assertThat(vacationDaysLeft.getVacationDaysUsedNextYear()).isEqualTo(new BigDecimal("12"));
        assertThat(vacationDaysLeft.getVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDays()).isEqualTo(ZERO);
        assertThat(vacationDaysLeft.getRemainingVacationDaysNotExpiring()).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfEmptyAccount() {
        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getRemainingVacationDaysAlreadyUsed(Optional.empty());
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfZeroRemainingVacationDays() {

        final Account account = new Account();
        account.setRemainingVacationDays(ZERO);

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getRemainingVacationDaysAlreadyUsed(Optional.of(account));
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(ZERO);
    }

    @Test
    void testGetVacationDaysUsedOfOneRemainingVacationDays() {

        initCustomService("20", "20");

        final Optional<Account> account = Optional.of(new Account());
        account.get().setAnnualVacationDays(new BigDecimal("30"));
        account.get().setVacationDays(new BigDecimal("30"));
        account.get().setRemainingVacationDays(new BigDecimal("10"));
        account.get().setRemainingVacationDaysNotExpiring(new BigDecimal("0"));

        final BigDecimal remainingVacationDaysAlreadyUsed = sut.getRemainingVacationDaysAlreadyUsed(account);
        assertThat(remainingVacationDaysAlreadyUsed).isEqualTo(TEN);
    }

    @Test
    void testGetTotalVacationDaysForPastYear() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2015-04-02T00:00:00.00Z"), ZoneId.of("UTC"));
        sut = new VacationDaysService(workDaysCountService, applicationService, fixedClock);

        initCustomService("4", "1");

        final Account account = new Account();
        account.setValidFrom(LocalDate.of(2014, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("31"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearBeforeApril() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2015-03-02T00:00:00.00Z"), ZoneId.of("UTC"));
        sut = new VacationDaysService(workDaysCountService, applicationService, fixedClock);

        initCustomService("4", "1");

        final Account account = new Account();
        account.setValidFrom(LocalDate.of(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("32"));
    }

    @Test
    void testGetTotalVacationDaysForThisYearAfterApril() {

        final Clock fixedClock = Clock.fixed(Instant.parse("2015-04-02T00:00:00.00Z"), ZoneId.of("UTC"));
        sut = new VacationDaysService(workDaysCountService, applicationService, fixedClock);

        initCustomService("4", "3");

        final Account account = new Account();
        account.setValidFrom(LocalDate.of(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        final BigDecimal leftDays = sut.calculateTotalLeftVacationDays(account);
        assertThat(leftDays).isEqualTo(new BigDecimal("30"));
    }

    @Test
    void testGetUsedDaysBeforeApril() {

        final String expectedUsedDays = "4";
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(Collections.singletonList(getSomeApplication(person)));

        final WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), eq(person))).thenReturn(new BigDecimal(expectedUsedDays));

        final Clock clock = Clock.systemUTC();
        VacationDaysService sut = new VacationDaysService(workDaysCountService, applicationService, clock);

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.now(clock));
        account.setValidTo(ZonedDateTime.now(clock).plusDays(10).toLocalDate());

        final BigDecimal usedDaysBeforeApril = sut.getUsedDaysBeforeApril(account);
        assertThat(usedDaysBeforeApril).isEqualTo(new BigDecimal(expectedUsedDays));
    }

    @Test
    void testGetUsedDaysAfterApril() {

        final String expectedUsedDays = "4";
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(Collections.singletonList(getSomeApplication(person)));

        final WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), eq(person))).thenReturn(new BigDecimal(expectedUsedDays));

        final Clock clock = Clock.systemUTC();
        VacationDaysService sut = new VacationDaysService(workDaysCountService, applicationService, clock);

        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.now(clock));
        account.setValidTo(ZonedDateTime.now(clock).plusDays(10).toLocalDate());

        final BigDecimal usedDaysAfterApril = sut.getUsedDaysAfterApril(account);
        assertThat(usedDaysAfterApril).isEqualTo(new BigDecimal(expectedUsedDays));
    }

    private Application getSomeApplication(Person person) {
        final Application application = new Application();
        application.setStartDate(LocalDate.of(2015, JANUARY, 1));
        application.setEndDate(LocalDate.of(2015, JANUARY, 3));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(createVacationType(HOLIDAY));
        application.setPerson(person);
        return application;
    }

    private void initCustomService(final String daysBeforeApril, final String daysAfterApril) {
        sut = new VacationDaysService(mock(WorkDaysCountService.class), applicationService, Clock.systemUTC()) {

            @Override
            protected BigDecimal getUsedDaysBeforeApril(Account account) {
                return new BigDecimal(daysBeforeApril);
            }

            @Override
            protected BigDecimal getUsedDaysAfterApril(Account account) {
                return new BigDecimal(daysAfterApril);
            }
        };
    }

    private HolidayManager getHolidayManager() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        final ManagerParameter managerParameter = ManagerParameters.create(url);
        return HolidayManager.getInstance(managerParameter);
    }
}
