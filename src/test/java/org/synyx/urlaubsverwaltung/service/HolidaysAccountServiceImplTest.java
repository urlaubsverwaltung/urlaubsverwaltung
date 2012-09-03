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
import org.synyx.urlaubsverwaltung.domain.legacy.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.legacy.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina
 * @author  Johannes Reuter
 */
public class HolidaysAccountServiceImplTest {

    private static final int CURRENT_YEAR = 2011;
    private static final int NEXT_YEAR = 2012;
    private static final BigDecimal ENTITLEMENT = BigDecimal.valueOf(25.0);
    private static final BigDecimal ENTITLEMENT_REMAINING = BigDecimal.valueOf(5.0);

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

        Mockito.when(holidaysAccountDAO.getHolidaysAccountByYearAndPerson(NEXT_YEAR, person)).thenReturn(null);

        Mockito.when(holidaysEntitlementDAO.getHolidayEntitlementByYearAndPerson(CURRENT_YEAR, person)).thenReturn(
            entitlement);

        entitlement.setAnnualVacationDays(BigDecimal.valueOf(28));

        returnValue = instance.getAccountOrCreateOne(NEXT_YEAR, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(28), returnValue.getVacationDays());
        assertEquals(person, returnValue.getPerson());
        assertEquals(NEXT_YEAR, returnValue.getYear());
        assertEquals(BigDecimal.ZERO, returnValue.getRemainingVacationDays());
    }


    /** Test of newHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testNewHolidayEntitlement() {

        HolidayEntitlement returnValue = instance.newHolidayEntitlement(person, NEXT_YEAR, BigDecimal.valueOf(28),
                ENTITLEMENT, ENTITLEMENT_REMAINING);
        assertNotNull(returnValue);
        assertEquals(person, returnValue.getPerson());
        assertEquals(BigDecimal.valueOf(28), returnValue.getAnnualVacationDays());
        assertEquals(ENTITLEMENT, returnValue.getVacationDays());
        assertEquals(ENTITLEMENT_REMAINING, returnValue.getRemainingVacationDays());
    }


    /** Test of newHolidaysAccount method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testNewHolidaysAccount() {

        HolidaysAccount returnValue = instance.newHolidaysAccount(person, CURRENT_YEAR, ENTITLEMENT, BigDecimal.TEN,
                true);
        assertNotNull(returnValue);
        assertEquals(person, returnValue.getPerson());
        assertEquals(BigDecimal.TEN, returnValue.getRemainingVacationDays());
        assertEquals(ENTITLEMENT, returnValue.getVacationDays());
        assertEquals(true, returnValue.isRemainingVacationDaysExpire());
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


    /** Test of updateHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testUpdateHolidayEntitlement() {

        /*
         * 10 vacation days are left over from last year entitlement of 25 days for new year, but 5 already taken
         *
         * Expected: entitlement of remaining vacation days should be 5 days vacation days of holidays account should be
         * 25 days (== entitlement) remaining vacation days of holidays account should be 5 days (== remaining vacation
         * days of last year entitlement)
         */

        entitlement.setPerson(person);
        entitlement.setYear(2012);
        entitlement.setVacationDays(BigDecimal.valueOf(25));
        entitlement.setRemainingVacationDays(BigDecimal.ZERO);

        Mockito.when(holidaysEntitlementDAO.getHolidayEntitlementByYearAndPerson(2012, person)).thenReturn(entitlement);

        List<Person> persons = new ArrayList<Person>();
        persons.add(person);

        HolidaysAccount accountLastYear = new HolidaysAccount();
        accountLastYear.setYear(2011);
        accountLastYear.setPerson(person);
        accountLastYear.setVacationDays(BigDecimal.TEN);

        HolidaysAccount accountCurrentYear = new HolidaysAccount();
        accountCurrentYear.setYear(2012);
        accountCurrentYear.setPerson(person);
        accountCurrentYear.setVacationDays(BigDecimal.valueOf(20));

        Mockito.when(holidaysAccountDAO.getHolidaysAccountByYearAndPerson(2011, person)).thenReturn(accountLastYear);
        Mockito.when(holidaysAccountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(accountCurrentYear);

        instance.updateHolidayEntitlement(persons, 2012);

        // TODO
// assertEquals(BigDecimal.valueOf(5), entitlement.getRemainingVacationDays());
// assertEquals(BigDecimal.valueOf(25), accountCurrentYear.getVacationDays());
// assertEquals(BigDecimal.valueOf(5), accountCurrentYear.getRemainingVacationDays());
    }


    /** Test of editHolidayEntitlement method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testEditHolidayEntitlement() {

        HolidayEntitlement ent = new HolidayEntitlement();
        ent.setVacationDays(BigDecimal.valueOf(23));
        ent.setRemainingVacationDays(BigDecimal.valueOf(5));

        instance.editHolidayEntitlement(ent, BigDecimal.valueOf(25), BigDecimal.valueOf(18), BigDecimal.valueOf(2));
        Mockito.verify(holidaysEntitlementDAO).save(ent);

        assertEquals(BigDecimal.valueOf(25), ent.getAnnualVacationDays());
        assertEquals(BigDecimal.valueOf(18), ent.getVacationDays());
        assertEquals(BigDecimal.valueOf(2), ent.getRemainingVacationDays());
    }


    /** Test of editHolidaysAccount method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testEditHolidaysAccount() {

        HolidaysAccount acc = new HolidaysAccount();
        acc.setVacationDays(BigDecimal.valueOf(22));
        acc.setRemainingVacationDays(BigDecimal.valueOf(3));
        acc.setRemainingVacationDaysExpire(true);

        instance.editHolidaysAccount(acc, BigDecimal.valueOf(28), BigDecimal.valueOf(5), false);
        Mockito.verify(holidaysAccountDAO).save(acc);

        assertEquals(BigDecimal.valueOf(28), acc.getVacationDays());
        assertEquals(BigDecimal.valueOf(5), acc.getRemainingVacationDays());
        assertEquals(false, acc.isRemainingVacationDaysExpire());
    }
}
