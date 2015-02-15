package org.synyx.urlaubsverwaltung.core.account;

import junit.framework.Assert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.io.IOException;

import java.math.BigDecimal;


/**
 * Unit test for {@link AccountServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class HolidaysAccountServiceImplTest {

    private AccountServiceImpl service;
    private AccountDAO accountDAO;
    private OwnCalendarService calendarService;
    private Account account;
    private Person person;

    @Before
    public void setup() throws IOException {

        accountDAO = Mockito.mock(AccountDAO.class);

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        calendarService = new OwnCalendarService(new JollydayCalendar(settingsService), workingTimeService);
        service = new AccountServiceImpl(accountDAO, calendarService);

        person = new Person();
        person.setLoginName("horscht");

        DateMidnight validFrom = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight validTo = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, validFrom.toDate(), validTo.toDate(), BigDecimal.valueOf(28),
                BigDecimal.valueOf(5), true);
    }


    @Test
    public void testGetAccount() {

        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Account result = service.getHolidaysAccount(2012, person);

        Assert.assertNotNull(result);
        Assert.assertEquals(account, result);
    }


    @Test
    public void testCalculateActualVacationDaysGreaterThanHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.AUGUST, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(12), result);
    }


    @Test
    public void testCalculateActualVacationDaysBetweenHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(9.5), result);
    }


    @Test
    public void testCalculateActualVacationDaysAlmostZero() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(33.3), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(11), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonths() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.MAY, 15);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("17.5"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonths2() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.MAY, 14);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("18"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForTwoMonths() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("5.0").setScale(2), result);
    }


    @Test
    public void testCalculateActualVacationDaysForOneMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("2.5").setScale(2), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonthToLastOfMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 15);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("1.5"), result);
    }


    @Test
    public void testCalculateActualVacationDaysForHalfMonthFromFirstOfMonth() {

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 16);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30), BigDecimal.ZERO,
                true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(new BigDecimal("1.5"), result);
    }


    @Test
    public void testGetOrCreateAccount() {

        Account a = new Account(person, new DateMidnight(2012, DateTimeConstants.JANUARY, 1).toDate(),
                new DateMidnight(2012, DateTimeConstants.DECEMBER, 31).toDate(), BigDecimal.valueOf(28),
                BigDecimal.valueOf(5), true);

        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(a);

        service.getOrCreateNewAccount(2013, person);

        // show that a new account is created by verifying that save method was called
        Mockito.verify(accountDAO).save(Mockito.any(Account.class));
    }
}
