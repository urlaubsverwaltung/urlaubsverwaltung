/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.HolidaysAccountDAO;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class HolidaysAccountServiceImplTest {

    private static final int CURRENT_YEAR = 2011;
    private static final int NEXT_YEAR = 2012;
    private static final BigDecimal ENTITLEMENT = BigDecimal.valueOf(25.0);

    private HolidaysAccountServiceImpl instance;
    private HolidaysAccountDAO holidaysAccountDAO = Mockito.mock(HolidaysAccountDAO.class);
    private HolidayEntitlementDAO holidaysEntitlementDAO = Mockito.mock(HolidayEntitlementDAO.class);

    private Person person;
    private HolidayEntitlement entitlement;

    public HolidaysAccountServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new HolidaysAccountServiceImpl(holidaysAccountDAO, holidaysEntitlementDAO);

        person = new Person();
        person.setLastName("Horschd");

        entitlement = new HolidayEntitlement();
        entitlement.setPerson(person);
        entitlement.setYear(CURRENT_YEAR);
        entitlement.setVacationDays(ENTITLEMENT);
    }


    @After
    public void tearDown() {
    }


    /** Test of getHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testGetHolidayEntitlement() {

        instance.getHolidayEntitlement(CURRENT_YEAR, person);
        Mockito.verify(holidaysEntitlementDAO).getHolidayEntitlementByYearAndPerson(CURRENT_YEAR, person);
    }


    /** Test of getHolidaysAccount method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testGetHolidaysAccount() {

        instance.getHolidaysAccount(NEXT_YEAR, person);
        Mockito.verify(holidaysAccountDAO).getHolidaysAccountByYearAndPerson(NEXT_YEAR, person);
    }


    /** Test of getHolidaysAccountsForYear method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testGetHolidaysAccountsForYear() {

        instance.getHolidaysAccountsForYear(CURRENT_YEAR);
        Mockito.verify(holidaysAccountDAO).getAllHolidaysAccountsByYear(CURRENT_YEAR);
    }


    /** Test of getAccountOrCreateOne method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testGetAccountOrCreateOne() {

        // case 1: account existent

        HolidaysAccount account = new HolidaysAccount();
        account.setPerson(person);
        account.setYear(CURRENT_YEAR);

        Mockito.when(holidaysAccountDAO.getHolidaysAccountByYearAndPerson(CURRENT_YEAR, person)).thenReturn(account);

        HolidaysAccount returnValue = instance.getAccountOrCreateOne(CURRENT_YEAR, person);
        assertNotNull(returnValue);
        assertEquals(account, returnValue);
        assertEquals(person, returnValue.getPerson());
        assertEquals(null, returnValue.getVacationDays());

        // case 2: account not yet existent

//        Mockito.when(holidaysAccountDAO.getHolidaysAccountByYearAndPerson(NEXT_YEAR, person)).thenReturn(null);
//
//        HolidaysAccount newAccount = new HolidaysAccount();
//        newAccount.setPerson(person);
//        newAccount.setYear(NEXT_YEAR);
//        newAccount.setRemainingVacationDays(BigDecimal.ZERO);
//        newAccount.setVacationDays(ENTITLEMENT);
//
//        Mockito.when(instance.newHolidaysAccount(person, ENTITLEMENT, BigDecimal.ZERO, NEXT_YEAR)).thenReturn(
//            newAccount);
//
//        returnValue = instance.getAccountOrCreateOne(NEXT_YEAR, person);
//        Mockito.verify(holidaysAccountDAO).save(newAccount);
//        assertNotNull(returnValue);
//        assertEquals(ENTITLEMENT, returnValue.getVacationDays());

    }


    /** Test of newHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testNewHolidayEntitlement() {

        HolidayEntitlement returnValue = instance.newHolidayEntitlement(person, NEXT_YEAR, ENTITLEMENT);
        assertNotNull(returnValue);
        assertEquals(person, returnValue.getPerson());
        assertEquals(ENTITLEMENT, returnValue.getVacationDays());
    }


    /** Test of newHolidaysAccount method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testNewHolidaysAccount() {

        HolidaysAccount returnValue = instance.newHolidaysAccount(person, ENTITLEMENT, BigDecimal.TEN, CURRENT_YEAR);
        assertNotNull(returnValue);
        assertEquals(person, returnValue.getPerson());
        assertEquals(BigDecimal.TEN, returnValue.getRemainingVacationDays());
        assertEquals(ENTITLEMENT, returnValue.getVacationDays());
    }


    /** Test of saveHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testSaveHolidayEntitlement() {

        HolidayEntitlement ent = new HolidayEntitlement();
        instance.saveHolidayEntitlement(ent);
        Mockito.verify(holidaysEntitlementDAO).save(ent);
    }


    /** Test of saveHolidaysAccount method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testSaveHolidaysAccount() {

        HolidaysAccount acc = new HolidaysAccount();
        instance.saveHolidaysAccount(acc);
        Mockito.verify(holidaysAccountDAO).save(acc);
    }
}
