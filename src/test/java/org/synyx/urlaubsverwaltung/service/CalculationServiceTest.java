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
import static org.junit.Assert.assertTrue;

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

        accountCurrentYear.setVacationDays(BigDecimal.valueOf(10));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));

        accountNextYear.setVacationDays(BigDecimal.valueOf(25));
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(5));

        application.setHowLong(DayLength.FULL);

        // distinguish following cases
        // 1. holidays period spans December and January
        // 2. holidays period is in future (current year plus 1)
        // 3. holidays period is before April
        // 4. holidays period not important: account's remaining vacation days don't expire on 1st April
        // 5. holidays period is after April
        // 6. holidays period spans March and April

        // holidays period spans December and January
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 10));

        instance.subtractVacationDays(application); // jumps to method subtractCaseSpanningDecemberAndJanuary

        // holidays period is in future (current year plus 1)
        int futureYear = DateMidnight.now().getYear() + 1;

        HolidaysAccount acc2 = new HolidaysAccount();
        acc2.setPerson(person);
        acc2.setVacationDays(BigDecimal.valueOf(28));

        Mockito.when(accountService.getAccountOrCreateOne(futureYear, person)).thenReturn(acc2);

        application.setStartDate(new DateMidnight(futureYear, DateTimeConstants.JANUARY, 2));
        application.setEndDate(new DateMidnight(futureYear, DateTimeConstants.JANUARY, 10));
        application.setDays(BigDecimal.valueOf(5)); // not calculated, just set by me

        instance.subtractVacationDays(application); // jumps to method subtractCaseFutureYear

        // holidays period is before April
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.FEBRUARY, 26));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MARCH, 10));

        instance.subtractVacationDays(application); // jumps to method subtractCaseBeforeApril

        // holidays period is after April, but remaining vacation days don't expire on 1st April
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 6));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 18));
        accountNextYear.setRemainingVacationDaysExpire(false);

        instance.subtractVacationDays(application); // jumps to method subtractCaseBeforeApril

        accountNextYear.setRemainingVacationDaysExpire(true); // reset

        // holidays period is after April
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 6));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 18));

        instance.subtractVacationDays(application); // jumps to method subtractCaseAfterApril

        // holidays period spans March and April
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MARCH, 26));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 8));

        instance.subtractVacationDays(application); // jumps to method subtractCaseBetweenApril
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


    /** Test of subtractCaseAfterApril method, of class CalculationService. */
    @Test
    public void testSubtractCaseAfterApril() {

        accountNextYear.setVacationDays(BigDecimal.valueOf(20));
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(3));

        // work days = 5
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 10));
        application.setHowLong(DayLength.FULL);

        HolidaysAccount returnValue = instance.subtractCaseAfterApril(application, accountNextYear);

        assertEquals(BigDecimal.valueOf(3), returnValue.getRemainingVacationDays()); // remaining vacation days
                                                                                     // unchanged
        assertEquals((BigDecimal.valueOf(20 - 5)).setScale(2), returnValue.getVacationDays()); // 7 days subtracted from
                                                                                               // account's vacation
                                                                                               // days
    }


    /** Test of subtractCaseBeforeApril method, of class CalculationService. */
    @Test
    public void testSubtractCaseBeforeApril() {

        // work days = 9
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MARCH, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MARCH, 13));
        application.setHowLong(DayLength.FULL);

        // remaining vacation days enough for whole holiday (after calculation: >0)
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(11));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBeforeApril(application, accountNextYear);
        assertEquals(BigDecimal.valueOf(2).setScale(2), accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20), accountNextYear.getVacationDays());

        // remaining vacation days exactly enough for whole holiday (after calculation =0)
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(9));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBeforeApril(application, accountNextYear);
        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20), accountNextYear.getVacationDays());

        // remaining vacation days and vaction days are used for (after calculation both types of vacation days are
        // changed)
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(4));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBeforeApril(application, accountNextYear);
        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(15).setScale(2), accountNextYear.getVacationDays());
    }


    /** Test of subtractCaseBetweenApril method, of class CalculationService. */
    @Test
    public void testSubtractCaseBetweenApril() {

        application.setHowLong(DayLength.FULL);

        // work days = 9
        // 5 before April
        // 4 after April
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.MARCH, 26));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 5));

        // remaining vacation days remain
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(7));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBetweenApril(application, accountNextYear);
        assertEquals(BigDecimal.valueOf(7 - 5).setScale(2), accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20 - 4).setScale(2), accountNextYear.getVacationDays());

        // remaining vacation days are exact zero
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(5));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBetweenApril(application, accountNextYear);
        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(20 - 4).setScale(2), accountNextYear.getVacationDays());

        // remaining vacation days are zero and days before April has to be subtracted from vacation days (the days
        // after April of course too)
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(2));
        accountNextYear.setVacationDays(BigDecimal.valueOf(20));

        instance.subtractCaseBetweenApril(application, accountNextYear);
        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays()); // 2 remaining vacation days minus 5
                                                                                   // days before April --> from
                                                                                   // vacation days you have to subtract
                                                                                   // 3 days before April
        assertEquals(BigDecimal.valueOf(20 - 3 - 4).setScale(2), accountNextYear.getVacationDays());
    }


    /** Test of subtractCaseSpanningDecemberAndJanuary method, of class CalculationService. */
    @Test
    public void testSubtractCaseSpanningDecemberAndJanuary() {

        application.setHowLong(DayLength.FULL);

        // 13 days are work days
        // 4 work days in old year
        // 9 work days in new year
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 15));

        // remaining vacation days don't expire
        accountCurrentYear.setRemainingVacationDaysExpire(false);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));

        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountNextYear.setVacationDays(BigDecimal.valueOf(28));

        List<HolidaysAccount> returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application,
                accountCurrentYear);

        assertTrue(!returnedAccounts.isEmpty());
        assertTrue(returnedAccounts.size() == 2);

        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(15 - (4 - 2)).setScale(2), accountCurrentYear.getVacationDays()); // at first remaining vacation days are used and then the vacation days

        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(28 - 9).setScale(2), accountNextYear.getVacationDays()); // only vacation days
                                                                                                 // are used

        // remaining vacation days do expire
        accountCurrentYear.setRemainingVacationDaysExpire(true);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));

        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountNextYear.setVacationDays(BigDecimal.valueOf(28));

        returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application, accountCurrentYear);

        assertTrue(!returnedAccounts.isEmpty());
        assertTrue(returnedAccounts.size() == 2);

        assertEquals(BigDecimal.valueOf(2), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(15 - 4).setScale(2), accountCurrentYear.getVacationDays()); // only vacation days are used

        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(28 - 9).setScale(2), accountNextYear.getVacationDays()); // only vacation days
                                                                                                 // are used
    }


    /** Test of subtractCaseFutureYear method, of class CalculationService. */
    @Test
    public void testSubtractCaseFutureYear() {

        // because holiday is in the future and you don't know the number of remaining vacation days (set automatically
        // to 0) calculation only with vacation days

        accountNextYear.setVacationDays(BigDecimal.valueOf(11));
        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);

        application.setDays(BigDecimal.valueOf(7));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 18));

        HolidaysAccount returnValue = instance.subtractCaseFutureYear(application);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.ZERO, returnValue.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(11 - 7), returnValue.getVacationDays());
    }
}
