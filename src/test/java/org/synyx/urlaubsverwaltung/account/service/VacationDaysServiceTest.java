package org.synyx.urlaubsverwaltung.account.service;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.period.NowService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.SPECIALLEAVE;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.UNPAIDLEAVE;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.account.service.VacationDaysService}.
 */
class VacationDaysServiceTest {

    private VacationDaysService vacationDaysService;

    private ApplicationService applicationService;
    private NowService nowService;

    @BeforeEach
    void setUp() {

        applicationService = mock(ApplicationService.class);
        nowService = mock(NowService.class);

        WorkingTimeService workingTimeService = mock(WorkingTimeService.class);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());
        workingTime.setWorkingDays(workingDays, FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class),
            any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        SettingsService settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(new Settings());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        HolidayManager holidayManager = HolidayManager.getInstance(managerParameter);

        WorkDaysCountService workDaysCountService = new WorkDaysCountService(new PublicHolidaysService(settingsService, holidayManager),
            workingTimeService, settingsService);

        vacationDaysService = new VacationDaysService(workDaysCountService, nowService, applicationService);
    }


    @Test
    void testGetDaysBeforeApril() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        LocalDate firstMilestone = LocalDate.of(2012, JANUARY, 1);
        LocalDate lastMilestone = LocalDate.of(2012, MARCH, 31);

        // 4 days at all: 2 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2011, DECEMBER, 29));
        a1.setEndDate(LocalDate.of(2012, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setStatus(ALLOWED);
        a1.setVacationType(getVacationType(HOLIDAY));
        a1.setPerson(person);

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, MARCH, 12));
        a2.setEndDate(LocalDate.of(2012, MARCH, 16));
        a2.setDayLength(FULL);
        a2.setStatus(ALLOWED);
        a2.setVacationType(getVacationType(HOLIDAY));
        a2.setPerson(person);

        // 4 days
        Application a3 = new Application();
        a3.setStartDate(LocalDate.of(2012, FEBRUARY, 6));
        a3.setEndDate(LocalDate.of(2012, FEBRUARY, 9));
        a3.setDayLength(FULL);
        a3.setStatus(WAITING);
        a3.setVacationType(getVacationType(HOLIDAY));
        a3.setPerson(person);

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, MARCH, 29));
        a4.setEndDate(LocalDate.of(2012, APRIL, 5));
        a4.setDayLength(FULL);
        a4.setStatus(WAITING);
        a4.setVacationType(getVacationType(HOLIDAY));
        a4.setPerson(person);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(Arrays.asList(a1, a2, a3, a4));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2 + 5 + 4 + 2 = 13

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("13.0"), days);
    }


    @Test
    void testGetDaysAfterApril() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        // 4 days at all: 2.5 before January + 2 after January
        Application a1 = new Application();
        a1.setStartDate(LocalDate.of(2012, DECEMBER, 27));
        a1.setEndDate(LocalDate.of(2013, JANUARY, 3));
        a1.setDayLength(FULL);
        a1.setPerson(person);
        a1.setStatus(ALLOWED);
        a1.setVacationType(getVacationType(HOLIDAY));

        // 5 days
        Application a2 = new Application();
        a2.setStartDate(LocalDate.of(2012, SEPTEMBER, 3));
        a2.setEndDate(LocalDate.of(2012, SEPTEMBER, 7));
        a2.setDayLength(FULL);
        a2.setPerson(person);
        a2.setStatus(ALLOWED);
        a2.setVacationType(getVacationType(HOLIDAY));

        // 6 days at all: 2 before April + 4 after April
        Application a4 = new Application();
        a4.setStartDate(LocalDate.of(2012, MARCH, 29));
        a4.setEndDate(LocalDate.of(2012, APRIL, 5));
        a4.setDayLength(FULL);
        a4.setPerson(person);
        a4.setStatus(WAITING);
        a4.setVacationType(getVacationType(HOLIDAY));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(Arrays.asList(a1, a2, a4));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2.5 + 5 + 4 = 11.5

        Assert.assertNotNull(days);
        Assert.assertEquals(new BigDecimal("11.5"), days);
    }


    @Test
    void testGetDaysBetweenMilestonesWithInactiveApplicationsForLeaveAndOfOtherVacationTypeThanHoliday() {

        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        LocalDate firstMilestone = LocalDate.of(2012, APRIL, 1);
        LocalDate lastMilestone = LocalDate.of(2012, DECEMBER, 31);

        Application cancelledHoliday = new Application();
        cancelledHoliday.setVacationType(getVacationType(HOLIDAY));
        cancelledHoliday.setStatus(CANCELLED);

        Application rejectedHoliday = new Application();
        rejectedHoliday.setVacationType(getVacationType(HOLIDAY));
        rejectedHoliday.setStatus(REJECTED);

        Application waitingSpecialLeave = new Application();
        waitingSpecialLeave.setVacationType(getVacationType(SPECIALLEAVE));
        waitingSpecialLeave.setStatus(WAITING);

        Application allowedSpecialLeave = new Application();
        allowedSpecialLeave.setVacationType(getVacationType(SPECIALLEAVE));
        allowedSpecialLeave.setStatus(ALLOWED);

        Application waitingUnpaidLeave = new Application();
        waitingUnpaidLeave.setVacationType(getVacationType(UNPAIDLEAVE));
        waitingUnpaidLeave.setStatus(WAITING);

        Application allowedUnpaidLeave = new Application();
        allowedUnpaidLeave.setVacationType(getVacationType(UNPAIDLEAVE));
        allowedUnpaidLeave.setStatus(ALLOWED);

        Application waitingOvertime = new Application();
        waitingOvertime.setVacationType(getVacationType(OVERTIME));
        waitingOvertime.setStatus(WAITING);

        Application allowedOvertime = new Application();
        allowedOvertime.setVacationType(getVacationType(OVERTIME));
        allowedOvertime.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(Arrays.asList(cancelledHoliday, rejectedHoliday, waitingSpecialLeave, allowedSpecialLeave,
                waitingUnpaidLeave, allowedUnpaidLeave, waitingOvertime, allowedOvertime));

        BigDecimal days = vacationDaysService.getUsedDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);

        Assert.assertNotNull(days);
        Assert.assertEquals(BigDecimal.ZERO, days);
    }


    @Test
    void testGetVacationDaysLeft() {

        initCustomService("4", "20");

        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, Optional.empty());

        Assert.assertNotNull("Should not be null", vacationDaysLeft);

        Assert.assertNotNull("Should not be null", vacationDaysLeft.getVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDays());
        Assert.assertNotNull("Should not be null", vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        Assert.assertEquals("Wrong number of vacation days", new BigDecimal("12"), vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong number of vacation days already used for next year", BigDecimal.ZERO,
            vacationDaysLeft.getVacationDaysUsedNextYear());

    }


    @Test
    void testGetVacationDaysLeftWithRemainingAlreadyUsed() {

        initCustomService("4", "20");

        // 36 Total, using 24, so 12 left
        Account account = new Account();
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        // next year has only 12 new days, but using 24, i.e. all 12 from this year
        Account nextYear = new Account();
        nextYear.setAnnualVacationDays(new BigDecimal("12"));
        nextYear.setVacationDays(new BigDecimal("12"));
        nextYear.setRemainingVacationDays(new BigDecimal("20"));
        nextYear.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));


        VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account, Optional.of(nextYear));

        Assert.assertEquals("Wrong number of vacation days already used for next year", new BigDecimal("12"), vacationDaysLeft.getVacationDaysUsedNextYear());

        Assert.assertEquals("Wrong number of vacation days", BigDecimal.ZERO, vacationDaysLeft.getVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days that do not expire", BigDecimal.ZERO,
            vacationDaysLeft.getRemainingVacationDaysNotExpiring());
    }

    @Test
    void testGetVacationDaysUsedOfEmptyAccount() {

        Assert.assertEquals(BigDecimal.ZERO, vacationDaysService.getRemainingVacationDaysAlreadyUsed(Optional.empty()));
    }

    @Test
    void testGetVacationDaysUsedOfZeroRemainingVacationDays() {

        Optional<Account> account = Optional.of(new Account());
        account.get().setRemainingVacationDays(BigDecimal.ZERO);

        Assert.assertEquals(BigDecimal.ZERO, vacationDaysService.getRemainingVacationDaysAlreadyUsed(account));
    }

    @Test
    void testGetVacationDaysUsedOfOneRemainingVacationDays() {

        initCustomService("20", "20");

        Optional<Account> account = Optional.of(new Account());
        account.get().setAnnualVacationDays(new BigDecimal("30"));
        account.get().setVacationDays(new BigDecimal("30"));
        account.get().setRemainingVacationDays(new BigDecimal("10"));
        account.get().setRemainingVacationDaysNotExpiring(new BigDecimal("0"));

        Assert.assertEquals(BigDecimal.TEN, vacationDaysService.getRemainingVacationDaysAlreadyUsed(account));
    }

    @Test
    void testGetTotalVacationDaysForPastYear() {

        when(nowService.now()).thenReturn(LocalDate.of(2015, 4, 2));

        initCustomService("4", "1");

        Account account = new Account();
        account.setValidFrom(LocalDate.of(2014, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("6"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("2"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left not expiring remaining vacation days
        // 31 = 30 + 1
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("31"), leftDays);
    }


    @Test
    void testGetTotalVacationDaysForThisYearBeforeApril() {

        when(nowService.now()).thenReturn(LocalDate.of(2015, 3, 2));
        when(nowService.currentYear()).thenReturn(2015);

        initCustomService("4", "1");

        Account account = new Account();
        account.setValidFrom(LocalDate.of(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left remaining vacation days
        // 32 = 30 + 2
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("32"), leftDays);
    }

    @Test
    void testGetTotalVacationDaysForThisYearAfterApril() {

        when(nowService.now()).thenReturn(LocalDate.of(2015, 4, 2));
        when(nowService.currentYear()).thenReturn(2015);

        initCustomService("4", "3");

        Account account = new Account();
        account.setValidFrom(LocalDate.of(2015, 1, 1));
        account.setAnnualVacationDays(new BigDecimal("30"));
        account.setVacationDays(new BigDecimal("30"));
        account.setRemainingVacationDays(new BigDecimal("7"));
        account.setRemainingVacationDaysNotExpiring(new BigDecimal("3"));

        BigDecimal leftDays = vacationDaysService.calculateTotalLeftVacationDays(account);

        Assert.assertNotNull("Should not be null", leftDays);

        // total number = left vacation days + left not expiring remaining vacation days
        // 30 = 30 + 0
        Assert.assertEquals("Wrong number of total vacation days", new BigDecimal("30"), leftDays);
    }

    @Test
    void testGetUsedDaysBeforeApril() {

        String expectedUsedDays = "4";
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(Collections.singletonList(getSomeApplication(person)));

        WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), eq(person))).thenReturn(new BigDecimal(expectedUsedDays));

        VacationDaysService vacationDaysService = new VacationDaysService(
            workDaysCountService,
            nowService,
            applicationService);

        Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.now(UTC));
        account.setValidTo(ZonedDateTime.now(UTC).plusDays(10).toLocalDate());

        BigDecimal usedDaysBeforeApril = vacationDaysService.getUsedDaysBeforeApril(account);

        Assert.assertEquals("Wrong number of used vacation days before april", new BigDecimal(expectedUsedDays), usedDaysBeforeApril);
    }

    @Test
    void testGetUsedDaysAfterApril() {

        String expectedUsedDays = "4";
        Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(Collections.singletonList(getSomeApplication(person)));

        WorkDaysCountService workDaysCountService = mock(WorkDaysCountService.class);
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), eq(person))).thenReturn(new BigDecimal(expectedUsedDays));

        VacationDaysService vacationDaysService = new VacationDaysService(
            workDaysCountService,
            nowService,
            applicationService);

        Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(LocalDate.now(UTC));
        account.setValidTo(ZonedDateTime.now(UTC).plusDays(10).toLocalDate());

        BigDecimal usedDaysAfterApril = vacationDaysService.getUsedDaysAfterApril(account);

        Assert.assertEquals("Wrong number of used vacation days after april", new BigDecimal(expectedUsedDays), usedDaysAfterApril);
    }

    private Application getSomeApplication(Person person) {

        Application application = new Application();
        application.setStartDate(LocalDate.of(2015, JANUARY, 1));
        application.setEndDate(LocalDate.of(2015, JANUARY, 3));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(getVacationType(HOLIDAY));
        application.setPerson(person);
        return application;
    }

    private void initCustomService(final String daysBeforeApril, final String daysAfterApril) {

        vacationDaysService = new VacationDaysService(mock(WorkDaysCountService.class), nowService,
            applicationService) {

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


    private VacationType getVacationType(VacationCategory category) {

        return TestDataCreator.createVacationType(category);
    }
}
