package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.AUGUST;
import static org.joda.time.DateTimeConstants.FRIDAY;
import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.THURSDAY;
import static org.joda.time.DateTimeConstants.TUESDAY;
import static org.joda.time.DateTimeConstants.WEDNESDAY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link CalculationService}.
 *
 * @author Aljona Murygina - murygina@synyx.de
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

        calendarService = new WorkDaysService(new PublicHolidaysService(settingsService), workingTimeService,
            settingsService);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = asList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(any(Person.class), any(DateMidnight.class)))
            .thenReturn(Optional.of(workingTime));

        sut = new CalculationService(vacationDaysService, accountService, accountInteractionService, calendarService);
    }


    @Test
    public void testCheckApplicationSameYearAndEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        // vacation days would be left after this application for leave
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.TEN);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    public void testCheckApplicationSameYearAndNotEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        // not enough vacation days for this application for leave
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.ZERO);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }

    @Test
    public void testCheckApplicationSameYearAndExactEnoughDaysLeft() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        // enough vacation days for this application for leave, but none would be left
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.ONE);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    @Test
    public void testCheckApplicationLastYear() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account account = new Account();
        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.empty());
        when(accountService.getHolidaysAccount(2011, person)).thenReturn(Optional.of(account));
        when(accountInteractionService.autoCreateOrUpdateNextYearsHolidaysAccount(account)).thenReturn(account);

        // enough vacation days for this application for leave, but none would be left
        when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.ONE);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isTrue();
    }

    /**
     * https://github.com/synyx/urlaubsverwaltung/issues/447
     */
    @Test
    public void testCheckApplicationNextYearUsingRemainingAlready() {

        Person person = TestDataCreator.createPerson("horscht");

        Application applicationForLeaveToCheck = new Application();
        // nine days
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, AUGUST, 30));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setDayLength(DayLength.FULL);

        Account thisYear = new Account(
            person, DateUtil.getFirstDayOfYear(2012).toDate(), DateUtil.getLastDayOfYear(2012).toDate(), BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, "");

        Account nextYear = new Account(
            person, DateUtil.getFirstDayOfYear(2013).toDate(), DateUtil.getLastDayOfYear(2013).toDate(), BigDecimal.TEN,
            // here we set up 2013 to have 10 days remaining vacation available from 2012,
            // if those have already been used up, we cannot spend them in 2012 as well
            BigDecimal.TEN, BigDecimal.TEN, "");
        nextYear.setVacationDays(nextYear.getAnnualVacationDays());


        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(thisYear));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(nextYear));

        // set up 13 days already used next year, i.e. 10 + 3 remaining
        when(vacationDaysService.getVacationDaysLeft(nextYear, Optional.empty())).thenReturn(
            VacationDaysLeft.builder()
                .withAnnualVacation(BigDecimal.TEN)
                .withRemainingVacation(BigDecimal.TEN)
                .notExpiring(BigDecimal.ZERO)
                .forUsedDaysBeforeApril(BigDecimal.valueOf(13))
                .forUsedDaysAfterApril(BigDecimal.ZERO)
                .get());

        // this year still has all ten days (but 3 of them used up next year, see above)
        when(vacationDaysService.calculateTotalLeftVacationDays(thisYear)).thenReturn(BigDecimal.TEN);

        final boolean enoughDaysLeft = sut.checkApplication(applicationForLeaveToCheck);
        assertThat(enoughDaysLeft).isFalse();
    }
}
