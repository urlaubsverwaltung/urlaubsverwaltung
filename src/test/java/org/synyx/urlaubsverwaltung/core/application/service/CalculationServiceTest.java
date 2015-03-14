package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;


/**
 * Unit test for {@link CalculationService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalculationServiceTest {

    private CalculationService service;
    private VacationDaysService vacationDaysService;
    private AccountInteractionService accountInteractionService;
    private AccountService accountService;
    private OwnCalendarService calendarService;

    @Before
    public void setUp() throws IOException {

        vacationDaysService = Mockito.mock(VacationDaysService.class);
        accountService = Mockito.mock(AccountService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        calendarService = new OwnCalendarService(new JollydayCalendar(), workingTimeService);

        // create working time object (MON-FRI)
        WorkingTime workingTime = new WorkingTime();
        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.any(Person.class),
                Mockito.any(DateMidnight.class))).thenReturn(Optional.of(workingTime));

        service = new CalculationService(vacationDaysService, accountService, accountInteractionService,
                calendarService);
    }


    @Test
    public void testCheckApplication() {

        Person person = new Person();
        person.setLoginName("horscht");

        Application applicationForLeaveToCheck = new Application();
        applicationForLeaveToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.AUGUST, 20));
        applicationForLeaveToCheck.setPerson(person);
        applicationForLeaveToCheck.setHowLong(DayLength.FULL);

        Account account = new Account();
        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account));

        // vacation days would be left after this application for leave
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.TEN);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));

        // not enough vacation days for this application for leave
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.ZERO);

        Assert.assertFalse("Should NOT be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));

        // enough vacation days for this application for leave, but none would be left
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account)).thenReturn(BigDecimal.ONE);

        Assert.assertTrue("Should be enough vacation days to apply for leave",
            service.checkApplication(applicationForLeaveToCheck));
    }
}
