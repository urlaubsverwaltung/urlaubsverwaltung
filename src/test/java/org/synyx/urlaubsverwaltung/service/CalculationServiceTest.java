/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * @author  Aljona Murygina
 */
public class CalculationServiceTest {

    private static final int CURRENT_YEAR = 2011;
    private static final int NEXT_YEAR = 2012;

    private CalculationService instance;
    private Application application;
    private Person person;
    private HolidayEntitlement entitlement;
    private HolidaysAccount accountCurrentYear;
    private HolidaysAccount accountNextYear;

    private OwnCalendarService calendarService = new OwnCalendarService();
    private HolidaysAccountService accountService = mock(HolidaysAccountService.class);

    public CalculationServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new CalculationService(calendarService, accountService);

        person = new Person();

        application = new Application();
        application.setPerson(person);

        // create accounts for person
        accountCurrentYear = new HolidaysAccount();
        accountCurrentYear.setPerson(person);
        accountCurrentYear.setYear(CURRENT_YEAR);

        accountNextYear = new HolidaysAccount();
        accountNextYear.setPerson(person);
        accountNextYear.setYear(NEXT_YEAR);

        // create entitlement of holiday for person
        entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);

        Mockito.when(accountService.getHolidaysAccount(CURRENT_YEAR, person)).thenReturn(accountCurrentYear);
        Mockito.when(accountService.getHolidaysAccount(NEXT_YEAR, person)).thenReturn(accountNextYear);
        Mockito.when(accountService.getAccountOrCreateOne(CURRENT_YEAR, person)).thenReturn(accountCurrentYear);
        Mockito.when(accountService.getAccountOrCreateOne(NEXT_YEAR, person)).thenReturn(accountNextYear);
        Mockito.when(accountService.getHolidayEntitlement(CURRENT_YEAR, person)).thenReturn(entitlement);
    }


    @After
    public void tearDown() {
    }


    /** Test of subtractVacationDays method, of class CalculationService. */
    @Test
    public void testSubtractVacationDays() {

        // application after April
        application.setHowLong(DayLength.FULL);

        // 11 days are work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 1));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 15));

        // enough days for holiday
        BigDecimal remainingVacDays = BigDecimal.ZERO;
        BigDecimal vacDays = BigDecimal.valueOf(20.0);

        accountCurrentYear.setRemainingVacationDays(remainingVacDays);
        accountCurrentYear.setVacationDays(vacDays);

        List<HolidaysAccount> returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        HolidaysAccount returnAccount = returnAccounts.get(0);

        assertEquals(vacDays.subtract(BigDecimal.valueOf(11.0)).setScale(2), returnAccount.getVacationDays());

        // application before April
        application.setHowLong(DayLength.FULL);

        // 8 days are work days
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 6));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 15));

        // enough days for holiday
        remainingVacDays = BigDecimal.valueOf(4.0);
        vacDays = BigDecimal.valueOf(20.0);

        accountNextYear.setRemainingVacationDays(remainingVacDays);
        accountNextYear.setVacationDays(vacDays);

        returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertEquals(BigDecimal.ZERO, returnAccount.getRemainingVacationDays());
        assertEquals(vacDays.subtract(BigDecimal.valueOf(4.0)).setScale(2), returnAccount.getVacationDays());

        // show that if application's period is after April, but holidays account's remaining vacation days don't
        // expire, it's the same case like 'beforeApril'
        application.setHowLong(DayLength.FULL);
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MAY, 14));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MAY, 24)); // == 9 work days

        remainingVacDays = BigDecimal.valueOf(4.0);
        vacDays = BigDecimal.valueOf(20.0);

        accountNextYear.setRemainingVacationDays(remainingVacDays);
        accountNextYear.setVacationDays(vacDays);

        returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertEquals(BigDecimal.ZERO, returnAccount.getRemainingVacationDays());
        assertEquals(vacDays.subtract(BigDecimal.valueOf(4.0)).setScale(2), returnAccount.getVacationDays());

        // number of remaining vacation days of account is equals number of days that are applied for leave
        application.setHowLong(DayLength.FULL);
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 7));

        remainingVacDays = BigDecimal.valueOf(5);
        vacDays = BigDecimal.valueOf(24);

        accountNextYear.setRemainingVacationDays(remainingVacDays);
        accountNextYear.setVacationDays(vacDays);

        returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertEquals(BigDecimal.ZERO, returnAccount.getRemainingVacationDays());
        assertEquals(vacDays, returnAccount.getVacationDays());

        // number of remaining vacation days of account is greater than number of days that are applied for leave
        application.setHowLong(DayLength.FULL);
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 7));

        remainingVacDays = BigDecimal.valueOf(7);
        vacDays = BigDecimal.valueOf(24);

        accountNextYear.setRemainingVacationDays(remainingVacDays);
        accountNextYear.setVacationDays(vacDays);

        returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertEquals(BigDecimal.valueOf(2).setScale(2), returnAccount.getRemainingVacationDays());
        assertEquals(vacDays, returnAccount.getVacationDays());

        // number of remaining vacation days of account is smaller than number of days that are applied for leave
        application.setHowLong(DayLength.FULL);
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 7));

        remainingVacDays = BigDecimal.valueOf(2);
        vacDays = BigDecimal.valueOf(24);

        accountNextYear.setRemainingVacationDays(remainingVacDays);
        accountNextYear.setVacationDays(vacDays);

        returnAccounts = instance.subtractVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        // days = 5
        // remaining days = 2
        // vac days = 24
        // --> vacdays = 24 - (5 - 2)

        assertEquals(BigDecimal.ZERO, returnAccount.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(21).setScale(2), returnAccount.getVacationDays());

        // TODO
        // test for this special case must be modified!

        // application between December and January
// application.setHowLong(DayLength.FULL);
//
// // 13 days are work days
// application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26));
// application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 15));
//
// // enough days for holiday
// vacDays = BigDecimal.valueOf(10.0);
//
// accountCurrentYear.setVacationDays(vacDays);
// accountNextYear.setVacationDays(BigDecimal.valueOf(20.0));
// accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
//
// returnAccounts = instance.subtractVacationDays(application);
//
// assertNotNull(returnAccounts);
// assertEquals(2, returnAccounts.size());
//
// HolidaysAccount returnAccountCurrent = returnAccounts.get(0);
// HolidaysAccount returnAccountNext = returnAccounts.get(1);
//
// assertNotNull(returnAccountCurrent);
// assertNotNull(returnAccountNext);
//
// assertEquals(BigDecimal.ZERO, returnAccountNext.getRemainingVacationDays());
// assertEquals(BigDecimal.valueOf(17.0).setScale(2), returnAccountNext.getVacationDays());
//
// // 8 days are work days
// application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26));
// application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 8));
//
// // enough days for holiday
// vacDays = BigDecimal.valueOf(15.0);
//
// accountCurrentYear.setVacationDays(vacDays);
// accountNextYear.setVacationDays(BigDecimal.valueOf(20.00));
// accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
//
// returnAccounts = instance.subtractVacationDays(application);
//
// assertNotNull(returnAccounts);
// assertEquals(2, returnAccounts.size());
//
// returnAccountCurrent = returnAccounts.get(0);
// returnAccountNext = returnAccounts.get(1);
//
// assertNotNull(returnAccountCurrent);
// assertNotNull(returnAccountNext);
//
// // there are 4.0 work days in current year
// // that means that 11.0 vacation days remain (15.0 - 4.0)
// // vacation days of current year are the remaining vacation days of next year
// // i.e. remaining vacation days = 15.0 - 4.0 = 11.0
// // there are 4.0 work days in next year
// // so they are subtracted from remaining vacation days: 11.0 - 4.0 = 7.0
// // that means: vacation days of next year still are 20.00
// // and remaining vacation days are 7.0
// assertEquals(BigDecimal.valueOf(7.0).setScale(2), returnAccountNext.getRemainingVacationDays());
// assertEquals(BigDecimal.valueOf(20.00), returnAccountNext.getVacationDays());
    }


    /** Test of addVacationDays method, of class CalculationService. */
    @Test
    public void testAddVacationDays() {

        entitlement.setVacationDays(BigDecimal.valueOf(24));
        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));

        application.setHowLong(DayLength.FULL);

        // application before April, 11 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 1));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 15));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20.0));

        List<HolidaysAccount> returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        HolidaysAccount returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays()); // remaining vacation days
                                                                                            // are filled up to number
                                                                                            // of entitlement's
                                                                                            // remaining vacation days
        assertEquals(BigDecimal.valueOf(26).setScale(2), accountCurrentYear.getVacationDays()); // 11 - 5 = 6 days are
                                                                                                // added to account's
                                                                                                // vacation days

        // application after April, 8 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 18));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 28));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15.0));
        accountCurrentYear.setRemainingVacationDaysExpire(true);

        returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(BigDecimal.valueOf(15.0 + 8.0).setScale(2), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays()); // not changed

        // application between March and April, 4 plus 7 work days (11.0)
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 28));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.APRIL, 11));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(17.0));

        returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays().setScale(2), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(4).setScale(2), accountCurrentYear.getRemainingVacationDays());
    }


    /** Test of addSickDaysOnHolidaysAccount method, of class CalculationService. */
    @Test
    public void testAddSickDaysOnHolidaysAccount() {

        // before April
        application.setSickDays(BigDecimal.valueOf(10.0));
        application.setDateOfAddingSickDays(new DateMidnight(CURRENT_YEAR, DateTimeConstants.FEBRUARY, 6));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20.0));
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));
        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));

        HolidaysAccount returnAccount = instance.addSickDaysOnHolidaysAccount(application, accountCurrentYear);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays());

        // after April
        application.setSickDays(BigDecimal.valueOf(12.0));
        application.setDateOfAddingSickDays(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 9));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20.0));
        accountCurrentYear.setRemainingVacationDaysExpire(true);
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));

        returnAccount = instance.addSickDaysOnHolidaysAccount(application, accountCurrentYear);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays());
        
        
        // after April, but remaining vacation days don't expire
        application.setSickDays(BigDecimal.valueOf(10));
        application.setDateOfAddingSickDays(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 9));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));
        accountCurrentYear.setRemainingVacationDaysExpire(false);
        entitlement.setVacationDays(BigDecimal.valueOf(25));
        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));

        returnAccount = instance.addSickDaysOnHolidaysAccount(application, accountCurrentYear);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays());
    }


    /** Test of subtractForCheck method, of class CalculationService. */
    @Test
    public void testSubtractForCheck() {

        /* This method works alike the method subtractVacationDays except that this method doesn't manipulate the real
         * account(s) but uses copies of the available account(s) days. Method is used by ApplicationServiceImpl's
         * method
         * checkApplication, i.e. real account(s) is/are not modified but only copies of it. */
    }


    /** Test of addDaysAfterApril method, of class CalculationService. */
    @Test
    public void testAddDaysAfterApril() {

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        entitlement.setVacationDays(BigDecimal.valueOf(24));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        // case 1:
        // number of added days plus account's vacation days is smaller than number of entitlement's vacation days

        BigDecimal days = BigDecimal.valueOf(2);

        instance.addDaysAfterApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(22), accountCurrentYear.getVacationDays()); // added two days
        assertEquals(BigDecimal.valueOf(3), accountCurrentYear.getRemainingVacationDays()); // not changed

        // case 2:
        // number of added days plus account's vacation days is equals number of entitlement's vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(4);

        instance.addDaysAfterApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(24), accountCurrentYear.getVacationDays()); // added four days
        assertEquals(BigDecimal.valueOf(3), accountCurrentYear.getRemainingVacationDays()); // not changed

        // case 3:
        // number of added days plus account's vacation days is greater than number of entitlement's vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(6);

        instance.addDaysAfterApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(24), accountCurrentYear.getVacationDays()); // number of account's vacation days
                                                                                    // is equals number of entitlement's
                                                                                    // vacation days
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays()); // added two days
    }


    /** Test of addDaysBeforeApril method, of class CalculationService. */
    @Test
    public void testAddDaysBeforeApril() {

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        entitlement.setVacationDays(BigDecimal.valueOf(24));

        // case 1: number of added days plus account's remaining vacation days is smaller than number of entitlement's
        // remaining vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        BigDecimal days = BigDecimal.valueOf(2);

        instance.addDaysBeforeApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(20), accountCurrentYear.getVacationDays()); // not changed
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays()); // added two days

        // case 2: number of added days plus account's remaining vacation days is equals number of entitlement's
        // remaining vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(3);

        instance.addDaysBeforeApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(20), accountCurrentYear.getVacationDays()); // not changed
        assertEquals(BigDecimal.valueOf(6), accountCurrentYear.getRemainingVacationDays()); // number of account's
                                                                                            // remaining vacation days
                                                                                            // is equals number of
                                                                                            // entitlement's remaining
                                                                                            // vacation days

        // case 3: number of added days plus account's remaining vacation days is greater than number of entitlement's
        // remaining vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(8);

        instance.addDaysBeforeApril(accountCurrentYear, days);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(25), accountCurrentYear.getVacationDays()); // added 5 days
        assertEquals(BigDecimal.valueOf(6), accountCurrentYear.getRemainingVacationDays()); // number of account's
                                                                                            // remaining vacation days
                                                                                            // is equals number of
                                                                                            // entitlement's remaining
                                                                                            // vacation days
    }


    /** Test of subtractSickDays method, of class CalculationService. */
    @Test
    public void testSubtractSickDays() {

        DateMidnight date = new DateMidnight(2012, 2, 13);

        Person p = new Person();
        p.setLastName("Testperson");

        HolidaysAccount acc = new HolidaysAccount();
        acc.setYear(date.getYear());
        acc.setPerson(p);

        Mockito.when(accountService.getHolidaysAccount(date.getYear(), p)).thenReturn(acc);

        // case 1: now is before April - 13th Feb 2012

        // remaining vacation days greater than sick days
        acc.setRemainingVacationDays(BigDecimal.valueOf(4));
        acc.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractSickDays(p, BigDecimal.valueOf(3), date);

        // 4 (remaining vacation days) - 3 (sick days) = 1
        assertEquals(BigDecimal.valueOf(1), acc.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20), acc.getVacationDays());

        // remaining vacation days equals sick days
        acc.setRemainingVacationDays(BigDecimal.valueOf(4));
        acc.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractSickDays(p, BigDecimal.valueOf(4), date);

        // 4 (remaining vacation days) - 4 (sick days) = 0
        assertEquals(BigDecimal.ZERO, acc.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20), acc.getVacationDays());

        // remaining vacation days smaller than sick days
        acc.setRemainingVacationDays(BigDecimal.valueOf(4));
        acc.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractSickDays(p, BigDecimal.valueOf(5), date);

        // 4 (remaining vacation days) - 5 (sick days) = -1
        assertEquals(BigDecimal.valueOf(0), acc.getRemainingVacationDays());

        // 20 (vacation days) + -1 (diff) = 19
        assertEquals(BigDecimal.valueOf(19), acc.getVacationDays());

        // CASE 2: now is after April - 13th Apr 2012
        date = new DateMidnight(2012, 4, 13);

        acc.setRemainingVacationDays(BigDecimal.valueOf(4));
        acc.setVacationDays(BigDecimal.valueOf(20));
        acc.setRemainingVacationDaysExpire(true);

        instance.subtractSickDays(p, BigDecimal.valueOf(5), date);

        assertEquals(BigDecimal.valueOf(4), acc.getRemainingVacationDays()); // unchanged
        assertEquals(BigDecimal.valueOf(15), acc.getVacationDays()); // 20 (vacation days) - 5 (sick days) = 15
    }
}
