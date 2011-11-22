/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;


/**
 * @author  aljona
 */
@Ignore
public class KontoServiceImplTest {

    public KontoServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /** Test of newUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testNewUrlaubsanspruch() {

        System.out.println("newUrlaubsanspruch");

        Person person = null;
        Integer year = null;
        Double anspruch = null;
        KontoServiceImpl instance = null;
        instance.newUrlaubsanspruch(person, year, anspruch);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of saveUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testSaveUrlaubsanspruch() {

        System.out.println("saveUrlaubsanspruch");

        Urlaubsanspruch urlaubsanspruch = null;
        KontoServiceImpl instance = null;
        instance.saveUrlaubsanspruch(urlaubsanspruch);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of saveUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testSaveUrlaubskonto() {

        System.out.println("saveUrlaubskonto");

        Urlaubskonto urlaubskonto = null;
        KontoServiceImpl instance = null;
        instance.saveUrlaubskonto(urlaubskonto);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of newUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testNewUrlaubskonto() {

        System.out.println("newUrlaubskonto");

        Person person = null;
        Double vacDays = null;
        Double restVacDays = null;
        Integer year = null;
        KontoServiceImpl instance = null;
        instance.newUrlaubskonto(person, vacDays, restVacDays, year);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of getUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubsanspruch() {

        System.out.println("getUrlaubsanspruch");

        Integer year = null;
        Person person = null;
        KontoServiceImpl instance = null;
        Urlaubsanspruch expResult = null;
        Urlaubsanspruch result = instance.getUrlaubsanspruch(year, person);
        assertEquals(expResult, result);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of getUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubskonto() {

        System.out.println("getUrlaubskonto");

        Integer year = null;
        Person person = null;
        KontoServiceImpl instance = null;
        Urlaubskonto expResult = null;
        Urlaubskonto result = instance.getUrlaubskonto(year, person);
        assertEquals(expResult, result);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of getUrlaubskontoForYear method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubskontoForYear() {

        System.out.println("getUrlaubskontoForYear");

        Integer year = null;
        KontoServiceImpl instance = null;
        List expResult = null;
        List result = instance.getUrlaubskontoForYear(year);
        assertEquals(expResult, result);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of rollbackUrlaub method, of class KontoServiceImpl. */
    @Test
    public void testRollbackUrlaub() {

        System.out.println("rollbackUrlaub");

        Antrag antrag = null;
        KontoServiceImpl instance = null;
        instance.rollbackUrlaub(antrag);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of rollbackNoticeJanuary method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeJanuary() {

        System.out.println("rollbackNoticeJanuary");

        Antrag antrag = null;
        Urlaubskonto kontoCurrentYear = null;
        Urlaubskonto kontoNextYear = null;
        Double anspruchCurrentYear = null;
        Double anspruchNextYear = null;
        DateMidnight start = null;
        DateMidnight end = null;
        KontoServiceImpl instance = null;
        instance.rollbackNoticeJanuary(antrag, kontoCurrentYear, kontoNextYear, anspruchCurrentYear, anspruchNextYear,
            start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of rollbackNoticeApril method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeApril() {

        System.out.println("rollbackNoticeApril");

        Antrag antrag = null;
        Urlaubskonto konto = null;
        Double anspruch = null;
        DateMidnight start = null;
        DateMidnight end = null;
        KontoServiceImpl instance = null;
        instance.rollbackNoticeApril(antrag, konto, anspruch, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of noticeJanuary method, of class KontoServiceImpl. */
    @Test
    public void testNoticeJanuary() {

        System.out.println("noticeJanuary");

        Antrag antrag = null;
        Urlaubskonto kontoCurrentYear = null;
        Urlaubskonto kontoNextYear = null;
        DateMidnight start = null;
        DateMidnight end = null;
        KontoServiceImpl instance = null;
        instance.noticeJanuary(antrag, kontoCurrentYear, kontoNextYear, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of noticeApril method, of class KontoServiceImpl. */
    @Test
    public void testNoticeApril() {

        System.out.println("noticeApril");

        Antrag antrag = null;
        Urlaubskonto konto = null;
        DateMidnight start = null;
        DateMidnight end = null;
        KontoServiceImpl instance = null;
        instance.noticeApril(antrag, konto, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
