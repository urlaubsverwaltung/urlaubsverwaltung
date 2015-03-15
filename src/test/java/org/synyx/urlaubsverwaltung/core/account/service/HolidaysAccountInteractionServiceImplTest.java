package org.synyx.urlaubsverwaltung.core.account.service;

import com.google.common.base.Optional;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.core.calendar.NowService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.io.IOException;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class HolidaysAccountInteractionServiceImplTest {

    private AccountInteractionServiceImpl service;

    private AccountService accountService;
    private VacationDaysService vacationDaysService;
    private NowService nowService;

    private Person person;

    @Before
    public void setup() throws IOException {

        accountService = Mockito.mock(AccountService.class);
        nowService = Mockito.mock(NowService.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        OwnCalendarService calendarService = new OwnCalendarService(new JollydayCalendar(), workingTimeService);
        vacationDaysService = Mockito.mock(VacationDaysService.class);

        service = new AccountInteractionServiceImpl(accountService, calendarService, vacationDaysService, nowService);

        person = new Person();
        person.setLoginName("horscht");
    }


    @Test
    public void testCalculateActualVacationDaysGreaterThanHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.AUGUST, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(12), result);
    }


    @Test
    public void testCalculateActualVacationDaysBetweenHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(9.5), result);
    }


    @Test
    public void testCalculateActualVacationDaysAlmostZero() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(33.3),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(11), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonths() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.MAY, 15);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("17.5"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonths2() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.MAY, 14);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("18"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForTwoMonths() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("5.0").setScale(2), result);
    }


    @Test
    public void testCalculateActualVacationDaysForOneMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("2.5").setScale(2), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonthToLastOfMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 15);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("1.5"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonthFromFirstOfMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 16);

        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("1.5"), result);
    }


    @Test
    public void testUpdateRemainingVacationDays() {

        Mockito.when(nowService.currentYear()).thenReturn(2014);

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account2012 = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5), BigDecimal.ZERO);

        Account account2013 = new Account(person, startDate.withYear(2013).toDate(), endDate.withYear(2013).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(3), BigDecimal.ZERO);

        Account account2014 = new Account(person, startDate.withYear(2014).toDate(), endDate.withYear(2014).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.ZERO);

        Mockito.when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        Mockito.when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        Mockito.when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));
        Mockito.when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.<Account>absent());

        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));
        Mockito.when(vacationDaysService.calculateTotalLeftVacationDays(account2013)).thenReturn(BigDecimal.valueOf(2));

        service.updateRemainingVacationDays(2012, person);

        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(account2012);
        Mockito.verify(vacationDaysService).calculateTotalLeftVacationDays(account2013);
        Mockito.verify(vacationDaysService, Mockito.never()).calculateTotalLeftVacationDays(account2014);

        Mockito.verify(accountService, Mockito.never()).save(account2012);
        Mockito.verify(accountService).save(account2013);
        Mockito.verify(accountService).save(account2014);

        Assert.assertEquals("Wrong number of remaining vacation days for 2012", BigDecimal.valueOf(5),
            account2012.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2013", BigDecimal.valueOf(6),
            account2013.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2014", BigDecimal.valueOf(2),
            account2014.getRemainingVacationDays());
    }
}
