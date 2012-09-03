/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service.legacy;

import org.synyx.urlaubsverwaltung.service.legacy.CalculationService;
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
import org.synyx.urlaubsverwaltung.domain.legacy.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidaysAccount;
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
    private HolidayEntitlement newEntitlement;
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

        newEntitlement = new HolidayEntitlement();
        newEntitlement.setPerson(person);
        newEntitlement.setRemainingVacationDays(BigDecimal.valueOf(3));

        Mockito.when(accountService.getHolidaysAccount(CURRENT_YEAR, person)).thenReturn(accountCurrentYear);
        Mockito.when(accountService.getHolidaysAccount(NEXT_YEAR, person)).thenReturn(accountNextYear);
        Mockito.when(accountService.getAccountOrCreateOne(CURRENT_YEAR, person)).thenReturn(accountCurrentYear);
        Mockito.when(accountService.getAccountOrCreateOne(NEXT_YEAR, person)).thenReturn(accountNextYear);
        Mockito.when(accountService.getHolidayEntitlement(CURRENT_YEAR, person)).thenReturn(entitlement);
        Mockito.when(accountService.getHolidayEntitlement(NEXT_YEAR, person)).thenReturn(newEntitlement);
    }


    @After
    public void tearDown() {
    }


    /** Test of subtractVacationDays method, of class CalculationService. */
    @Test
    public void testSubtractVacationDays() {

        // distinguish following cases
        // 1. holidays period spans December and January
        // 2. holidays period is in future (current year plus 1)
        // 3. holidays period is before April
        // 4. holidays period not important: account's remaining vacation days don't expire on 1st April
        // 5. holidays period is after April
        // 6. holidays period spans March and April

        // for case 1 and 2
        instance = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return CURRENT_YEAR;
            }
        };

        // 1. holidays period spans December and January

        application.setHowLong(DayLength.FULL);

        // 13 days are work days
        // 4 work days in old year
        // 9 work days in new year
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26)); // 4 days
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 15)); // 9 days

        // remaining vacation days don't expire
        // applying in same year - not subsequently
        accountCurrentYear.setRemainingVacationDaysExpire(false);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));

        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountNextYear.setVacationDays(BigDecimal.valueOf(28));

        List<HolidaysAccount> returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application,
                accountCurrentYear, false); // test for case not checking

        assertTrue(!returnedAccounts.isEmpty());
        assertTrue(returnedAccounts.size() == 2);

        assertEquals(BigDecimal.ZERO, returnedAccounts.get(0).getRemainingVacationDays());
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

        returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application, accountCurrentYear, false); // test for case not checking

        assertTrue(!returnedAccounts.isEmpty());
        assertTrue(returnedAccounts.size() == 2);

        assertEquals(BigDecimal.valueOf(2), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(15 - 4).setScale(2), accountCurrentYear.getVacationDays()); // only vacation days are used

        assertEquals(BigDecimal.ZERO, accountNextYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(28 - 9).setScale(2), accountNextYear.getVacationDays()); // only vacation days
                                                                                                 // are used

        // 2. holidays period is in future (current year plus 1)

        // because holiday is in the future and you don't know the number of remaining vacation days (set automatically
        // to 0) calculation only with vacation days

        accountNextYear.setVacationDays(BigDecimal.valueOf(11));
        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);

        application.setDays(BigDecimal.valueOf(7));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 18));

        HolidaysAccount returnValue = instance.subtractCaseFutureYear(application, false); // test for case not checking

        assertNotNull(returnValue);
        assertEquals(BigDecimal.ZERO, returnValue.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(11 - 7), returnValue.getVacationDays());

        instance = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return NEXT_YEAR;
            }
        };

        // 3. holidays period is before April

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

        // 5. holidays period is after April

        accountNextYear.setVacationDays(BigDecimal.valueOf(20));
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(3));

        // work days = 5
        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 1));
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.APRIL, 10));
        application.setHowLong(DayLength.FULL);

        returnValue = instance.subtractCaseAfterApril(application, accountNextYear);

        assertEquals(BigDecimal.valueOf(3), returnValue.getRemainingVacationDays()); // remaining vacation days
                                                                                     // unchanged
        assertEquals((BigDecimal.valueOf(20 - 5)).setScale(2), returnValue.getVacationDays()); // 7 days subtracted from
                                                                                               // account's vacation
                                                                                               // days

        // 6. holidays period spans March and April

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

        // 1 : simulate there aren't any applications before April

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        entitlement.setVacationDays(BigDecimal.valueOf(24));

        // case 1: number of added days plus account's remaining vacation days is smaller than number of entitlement's
        // remaining vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        BigDecimal days = BigDecimal.valueOf(2);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.ZERO);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(20), accountCurrentYear.getVacationDays()); // not changed
        assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays()); // added two days

        // case 2: number of added days plus account's remaining vacation days is equals number of entitlement's
        // remaining vacation days

        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(3);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.ZERO);

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

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.ZERO);

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(25), accountCurrentYear.getVacationDays()); // added 5 days
        assertEquals(BigDecimal.valueOf(6), accountCurrentYear.getRemainingVacationDays()); // number of account's
                                                                                            // remaining vacation days
                                                                                            // is equals number of
                                                                                            // entitlement's remaining
                                                                                            // vacation days

        // 2 : simulate there already are some applications before April

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(6);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.valueOf(21)); // used days + number of adding
                                                                                       // days

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(26), accountCurrentYear.getVacationDays());

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(5));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        days = BigDecimal.valueOf(6);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.valueOf(8)); // used days + number of adding
                                                                                      // days

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(3), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(23), accountCurrentYear.getVacationDays());

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(21));

        days = BigDecimal.valueOf(6);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.valueOf(9)); // used days + number of adding
                                                                                      // days

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(3), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(24), accountCurrentYear.getVacationDays());

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(22));

        days = BigDecimal.valueOf(3);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.valueOf(8)); // used days + number of adding
                                                                                      // days

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(1), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(24), accountCurrentYear.getVacationDays());

        entitlement.setRemainingVacationDays(BigDecimal.valueOf(6));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(23));

        days = BigDecimal.valueOf(7);

        instance.addDaysBeforeApril(accountCurrentYear, days, BigDecimal.valueOf(7)); // used days + number of adding
                                                                                      // days

        assertNotNull(accountCurrentYear);
        assertEquals(BigDecimal.valueOf(6), accountCurrentYear.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(24), accountCurrentYear.getVacationDays());
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

        instance = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return CURRENT_YEAR;
            }
        };

        application.setHowLong(DayLength.FULL);

        // 13 days are work days
        // 4 work days in old year
        // 9 work days in new year
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 26)); // 4 days
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 15)); // 9 days

        // remaining vacation days don't expire
        // applying in same year - not subsequently
        accountCurrentYear.setRemainingVacationDaysExpire(false);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));

        accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountNextYear.setVacationDays(BigDecimal.valueOf(28));

        List<HolidaysAccount> returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application,
                accountCurrentYear, false); // test for case not checking

        assertTrue(!returnedAccounts.isEmpty());
        assertTrue(returnedAccounts.size() == 2);

        assertEquals(BigDecimal.ZERO, returnedAccounts.get(0).getRemainingVacationDays());
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

        returnedAccounts = instance.subtractCaseSpanningDecemberAndJanuary(application, accountCurrentYear, false); // test for case not checking

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

        HolidaysAccount returnValue = instance.subtractCaseFutureYear(application, false); // test for case not checking

        assertNotNull(returnValue);
        assertEquals(BigDecimal.ZERO, returnValue.getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(11 - 7), returnValue.getVacationDays());
    }


    /**
     * Test of addDaysOfApplicationSpanningDecemberAndJanuary method, of class CalculationService. REGARD: test must be
     * modified every year! (because of calling DateMidnight.now() in method!)
     */
    @Test
    public void testAddDaysOfApplicationSpanningDecemberAndJanuary() {

        HolidayEntitlement ent = new HolidayEntitlement();
        ent.setRemainingVacationDays(BigDecimal.valueOf(5));
        ent.setVacationDays(BigDecimal.valueOf(28));
        ent.setPerson(person);
        ent.setYear(NEXT_YEAR);

        Mockito.when(accountService.getHolidayEntitlement(NEXT_YEAR, person)).thenReturn(ent);

        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(2));
        accountNextYear.setVacationDays(BigDecimal.valueOf(15));

        HolidaysAccount accNew = new HolidaysAccount();
        accNew.setPerson(person);
        accNew.setYear(NEXT_YEAR + 1);
        accNew.setRemainingVacationDays(BigDecimal.ZERO);
        accNew.setVacationDays(BigDecimal.valueOf(24));

        Mockito.when(accountService.getAccountOrCreateOne(NEXT_YEAR + 1, person)).thenReturn(accNew);

        application.setHowLong(DayLength.FULL);

        // two cases
        // 1 : cancelling occur before 1st January
        // 2 : cancelling occur after 1st January

        // 1 : cancelling occur before 1st January

        instance = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return NEXT_YEAR;
            }
        };

        application.setStartDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.DECEMBER, 25)); // 2.5 days before 1st
                                                                                               // January
        application.setEndDate(new DateMidnight(NEXT_YEAR + 1, DateTimeConstants.JANUARY, 8)); // 5 days after 1st
                                                                                               // January

        // remaining vacation days expire
        accountNextYear.setRemainingVacationDaysExpire(true);

        List<HolidaysAccount> accs = instance.addDaysOfApplicationSpanningDecemberAndJanuary(application,
                accountNextYear, BigDecimal.ZERO, BigDecimal.ZERO);

        assertTrue(accs.size() == 2);
        assertEquals(BigDecimal.valueOf(15 + 2.5).setScale(2), accs.get(0).getVacationDays());
        assertEquals(BigDecimal.valueOf(24 + 5).setScale(2), accs.get(1).getVacationDays());

        // remaining vacation days do not expire
        accountNextYear.setRemainingVacationDaysExpire(false);
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(2));
        accountNextYear.setVacationDays(BigDecimal.valueOf(15));

        accNew.setRemainingVacationDays(BigDecimal.ZERO);
        accNew.setVacationDays(BigDecimal.valueOf(24));

        // simulate there aren't any applications before April

        accs = instance.addDaysOfApplicationSpanningDecemberAndJanuary(application, accountNextYear, BigDecimal.ZERO,
                BigDecimal.ZERO);

        assertTrue(accs.size() == 2);
        assertEquals(BigDecimal.valueOf(2 + 2.5).setScale(2), accs.get(0).getRemainingVacationDays());
        assertEquals(BigDecimal.valueOf(15), accs.get(0).getVacationDays());
        assertEquals(BigDecimal.valueOf(24 + 5).setScale(2), accs.get(1).getVacationDays());

        // 2 : cancelling occur after 1st January

        instance = new CalculationService(calendarService, accountService) {

            @Override
            protected int getCurrentYear() {

                return NEXT_YEAR;
            }
        };

        accountCurrentYear.setVacationDays(BigDecimal.valueOf(3));
        accountCurrentYear.setRemainingVacationDays(BigDecimal.valueOf(2));
        accountCurrentYear.setRemainingVacationDaysExpire(true);

        ent.setVacationDays(BigDecimal.valueOf(28));
        ent.setRemainingVacationDays(BigDecimal.valueOf(3)); // new year's entitlement's remaining vacation days == last
                                                             // year's account's vacation days

        accountNextYear.setVacationDays(BigDecimal.valueOf(27));
        accountNextYear.setRemainingVacationDays(BigDecimal.valueOf(0));

        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.DECEMBER, 28)); // 3 days
        application.setEndDate(new DateMidnight(NEXT_YEAR, DateTimeConstants.JANUARY, 8)); // 4 days

        accs = instance.addDaysOfApplicationSpanningDecemberAndJanuary(application, accountCurrentYear, BigDecimal.ZERO,
                BigDecimal.ZERO);

        assertTrue(accs.size() == 2);
        assertEquals(BigDecimal.valueOf(28), ent.getVacationDays()); // unchanged
        assertEquals(BigDecimal.valueOf(3 + 3).setScale(2), ent.getRemainingVacationDays()); // old value (3) plus days
                                                                                             // before January (3)
        assertEquals(BigDecimal.valueOf(3 + 3).setScale(2), accs.get(0).getVacationDays()); // old value (3) plus days
                                                                                            // before January (3)
        assertEquals(BigDecimal.valueOf(2), accs.get(0).getRemainingVacationDays()); // unchanged

        // 4 days are added
        // 3 are added to remaining vacation days
        // 1 is added to vacation days
        assertEquals(BigDecimal.valueOf(28).setScale(2), accs.get(1).getVacationDays());
        assertEquals(BigDecimal.valueOf(6).setScale(2), accs.get(1).getRemainingVacationDays());
    }


    /** Test of createSupplementalApplication method, of class CalculationService. */
    @Test
    public void testCreateSupplementalApplication() {

        // is public but a stupid method: does nothing but setting values - testing not necessary
    }
}
