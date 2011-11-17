/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;


/**
 * @author  aljona
 */
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
    @Test
    public void testSave() {
        instance.save(new Person());
        Mockito.verify(personDAO).save((Person) (Mockito.any()));
        // speichert einfach nur: DAO
    }


    /** Test of delete method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testDelete() {

        // loescht einfach nur: DAO
    }


    /** Test of getPersonByID method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testGetPersonByID() {

        // holt sich Person nach Id einfach nur: DAO
    }


    /** Test of getAllPersons method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testGetAllPersons() {

        // holt sich alle Personen einfach nur: DAO
    }


    /** Test of deleteResturlaub method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testDeleteResturlaub() {

        // DAO-Schrott
    }


    /** Test of getPersonsWithResturlaub method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testGetPersonsWithResturlaub() {

        // DAO-Schrott
    }


    /** Test of updateVacationDays method, of class PersonServiceImpl. */
    @Test
    public void testUpdateVacationDays() {
        
        //einige objekte, die PersonService braucht, um zu laufen
        Urlaubsanspruch defaultUrlaubsanspruch  = new Urlaubsanspruch();
        defaultUrlaubsanspruch.setVacationDays(20);
        Urlaubskonto lastYearKonto = mock(Urlaubskonto.class);
        Urlaubskonto thisYearKonto = mock(Urlaubskonto.class);
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person());
        
        //wenn personService seine dependcies fragt, soll DAS rauskommen
        Mockito.when(personDAO.findAll()).thenReturn(personList);
        Mockito.when(lastYearKonto.getVacationDays()).thenReturn(10);
        Mockito.when(thisYearKonto.getVacationDays()).thenReturn(15);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(defaultUrlaubsanspruch);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(lastYearKonto);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(thisYearKonto);

        //rufe zu testende methode auf
        instance.updateVacationDays(2001);
        

        Mockito.verify(thisYearKonto).setRestVacationDays(5);
        Mockito.verify(thisYearKonto).setVacationDays(20);
        //das neue urlaubskonto sollte auch gespeichert werden
        Mockito.verify(kontoService).saveUrlaubskonto(thisYearKonto);
    }


    /** Test of getAllUrlauberForThisWeekAndPutItInAnEmail method, of class PersonServiceImpl. */
    @Test
    @Ignore
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
    @Ignore
    public void testGetUrlaubsanspruchByPersonAndYear() {

        // DAO-Schrott
    }


    /** Test of getUrlaubsanspruchByPersonForAllYears method, of class PersonServiceImpl. */
    @Test
    @Ignore
    public void testGetUrlaubsanspruchByPersonForAllYears() {

        // DAO-Schrott
    }


    /** Test of setUrlaubsanspruchForPerson method, of class PersonServiceImpl. */
    @Test
    @Ignore
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
    @Ignore
    public void testGetPersonByLogin() {

        // DAO-Schrott
    }
}
