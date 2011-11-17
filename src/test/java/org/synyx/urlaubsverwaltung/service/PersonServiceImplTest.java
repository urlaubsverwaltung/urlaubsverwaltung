/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;


/**
 * @author  aljona
 */
@Ignore
public class PersonServiceImplTest {

    private PersonService instance;

    private PersonDAO personDAO = mock(PersonDAO.class);
    private AntragService antragService = mock(AntragService.class);
    private KontoService kontoService = mock(KontoService.class);
    private UrlaubsanspruchDAO urlaubsanspruchDAO = mock(UrlaubsanspruchDAO.class);
    private MailService mailService = mock(MailService.class);

    public PersonServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new PersonServiceImpl(personDAO, antragService, urlaubsanspruchDAO, mailService, kontoService);
    }


    @After
    public void tearDown() {
    }


    /** Test of save method, of class PersonServiceImpl. */
    @Ignore
    @Test
    public void testSave() {

        // speichert einfach nur: DAO
    }


    /** Test of delete method, of class PersonServiceImpl. */
    @Test
    public void testDelete() {

        // loescht einfach nur: DAO
    }


    /** Test of getPersonByID method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByID() {

        // holt sich Person nach Id einfach nur: DAO
    }


    /** Test of getAllPersons method, of class PersonServiceImpl. */
    @Test
    public void testGetAllPersons() {

        // holt sich alle Personen einfach nur: DAO
    }


    /** Test of deleteResturlaub method, of class PersonServiceImpl. */
    @Test
    public void testDeleteResturlaub() {

        // DAO-Schrott
    }


    /** Test of getPersonsWithResturlaub method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonsWithResturlaub() {

        // DAO-Schrott
    }


    /** Test of updateVacationDays method, of class PersonServiceImpl. */
    @Test
    public void testUpdateVacationDays() {

        System.out.println("updateVacationDays");

        PersonServiceImpl instance = null;
        instance.updateVacationDays();

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of getAllUrlauberForThisWeekAndPutItInAnEmail method, of class PersonServiceImpl. */
    @Test
    public void testGetAllUrlauberForThisWeekAndPutItInAnEmail() {

        System.out.println("getAllUrlauberForThisWeekAndPutItInAnEmail");

        DateMidnight startDate = null;
        DateMidnight endDate = null;
        PersonServiceImpl instance = null;
        instance.getAllUrlauberForThisWeekAndPutItInAnEmail(startDate, endDate);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of getUrlaubsanspruchByPersonAndYear method, of class PersonServiceImpl. */
    @Test
    public void testGetUrlaubsanspruchByPersonAndYear() {

        // DAO-Schrott
    }


    /** Test of getUrlaubsanspruchByPersonForAllYears method, of class PersonServiceImpl. */
    @Test
    public void testGetUrlaubsanspruchByPersonForAllYears() {

        // DAO-Schrott
    }


    /** Test of setUrlaubsanspruchForPerson method, of class PersonServiceImpl. */
    @Test
    public void testSetUrlaubsanspruchForPerson() {

//        Person person = new Person();
//
//        instance.setUrlaubsanspruchForPerson(person, 2011, 26);
//
//        Urlaubsanspruch anspruch = person.getUrlaubsanspruch().get(0);
//
//        assertEquals(person, anspruch.getPerson());
    }


    /** Test of getPersonByLogin method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByLogin() {

        // DAO-Schrott
    }
}
