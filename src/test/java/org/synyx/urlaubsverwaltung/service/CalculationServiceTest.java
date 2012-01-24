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

        application.setHowLong(DayLength.FULL);

        // application before April, 11 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 1));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 15));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20.0));
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));

        List<HolidaysAccount> returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        HolidaysAccount returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(6.0).setScale(2), accountCurrentYear.getRemainingVacationDays());

        // application after April, 8 work days
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 18));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 28));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(15.0));
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));

        returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(BigDecimal.valueOf(15.0 + 8.0).setScale(2), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays());

        // application between March and April, 4 plus 7 work days (11.0)
        application.setStartDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MARCH, 28));
        application.setEndDate(new DateMidnight(CURRENT_YEAR, DateTimeConstants.APRIL, 11));
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(17.0));
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));

        returnAccounts = instance.addVacationDays(application);

        assertNotNull(returnAccounts);
        assertEquals(1, returnAccounts.size());

        returnAccount = returnAccounts.get(0);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(3.0).setScale(2), accountCurrentYear.getRemainingVacationDays());
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

        HolidaysAccount returnAccount = instance.addSickDaysOnHolidaysAccount(application, accountCurrentYear);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.valueOf(5.0), accountCurrentYear.getRemainingVacationDays());

        // after April
        application.setSickDays(BigDecimal.valueOf(12.0));
        application.setDateOfAddingSickDays(new DateMidnight(CURRENT_YEAR, DateTimeConstants.MAY, 9));

        accountCurrentYear.setRemainingVacationDays(BigDecimal.ZERO);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20.0));
        entitlement.setVacationDays(BigDecimal.valueOf(25.0));

        returnAccount = instance.addSickDaysOnHolidaysAccount(application, accountCurrentYear);

        assertNotNull(returnAccount);
        assertEquals(entitlement.getVacationDays(), accountCurrentYear.getVacationDays());
        assertEquals(BigDecimal.ZERO, accountCurrentYear.getRemainingVacationDays());
    }
}
