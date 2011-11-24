/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.mockito.invocation.InvocationOnMock;

import org.mockito.stubbing.Answer;

import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.ArrayList;
import java.util.List;


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

        Person personToSave = new Person();
        instance.save(personToSave);
        Mockito.verify(personDAO).save(personToSave);
        // speichert einfach nur: DAO
    }


    /** Test of delete method, of class PersonServiceImpl. */
    @Test
    public void testDelete() {

        Person personToSave = new Person();
        instance.delete(personToSave);
        Mockito.verify(personDAO).delete(personToSave);
    }


    /** Test of getPersonByID method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByID() {

        instance.getPersonByID(123);
        Mockito.verify(personDAO).findOne(123);
    }


    /** Test of getAllPersons method, of class PersonServiceImpl. */
    @Test
    public void testGetAllPersons() {

        instance.getAllPersons();
        Mockito.verify(personDAO).findAll();
    }


    /** Test of deleteResturlaub method, of class PersonServiceImpl. */
    @Test
    public void testDeleteResturlaub() {

        List<Person> persons = new ArrayList<Person>();
        Person person = new Person();
        persons.add(person);

        Urlaubskonto konto = mock(Urlaubskonto.class);

        Mockito.when(personDAO.findAll()).thenReturn(persons);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto);

        instance.deleteResturlaub();

        Mockito.verify(konto).setRestVacationDays(0.0);
    }


    /** Test of getPersonsWithResturlaub method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonsWithResturlaub() {

        List<Person> persons = new ArrayList<Person>();
        Person person = new Person();
        persons.add(person);
        person.setFirstName("babbel");

        Urlaubskonto konto = mock(Urlaubskonto.class);

        Mockito.when(personDAO.findAll()).thenReturn(persons);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto);
        Mockito.when(konto.getRestVacationDays()).thenReturn(5.0);

        List<Person> result = instance.getPersonsWithResturlaub();

        assertEquals(result.get(0).getFirstName(), "babbel");
    }


    /** Test of updateVacationDays method, of class PersonServiceImpl. */
    @Test
    public void testUpdateVacationDaysResturlaubUeberlaufFall() {

        // einige objekte, die PersonService braucht, um zu laufen
        Urlaubsanspruch defaultUrlaubsanspruch = new Urlaubsanspruch();
        defaultUrlaubsanspruch.setVacationDays(20.0);

        Urlaubskonto lastYearKonto = mock(Urlaubskonto.class);
        Urlaubskonto thisYearKonto = mock(Urlaubskonto.class);
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person());

        // wenn personService seine dependcies fragt, soll DAS rauskommen
        Mockito.when(personDAO.findAll()).thenReturn(personList);
        Mockito.when(lastYearKonto.getVacationDays()).thenReturn(10.0);
        Mockito.when(thisYearKonto.getVacationDays()).thenReturn(15.0);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            defaultUrlaubsanspruch);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(
            lastYearKonto);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(
            thisYearKonto);

        // rufe zu testende methode auf
        instance.updateVacationDays(2001);

        Mockito.verify(thisYearKonto).setRestVacationDays(5.0);
        Mockito.verify(thisYearKonto).setVacationDays(20.0);

        // das neue urlaubskonto sollte auch gespeichert werden
        Mockito.verify(kontoService).saveUrlaubskonto(thisYearKonto);
    }


    /** Test of updateVacationDays method, of class PersonServiceImpl. */
    @Test
    public void testUpdateVacationDaysNormal() {

        // einige objekte, die PersonService braucht, um zu laufen
        Urlaubsanspruch defaultUrlaubsanspruch = new Urlaubsanspruch();
        defaultUrlaubsanspruch.setVacationDays(20.0);

        Urlaubskonto lastYearKonto = mock(Urlaubskonto.class);
        Urlaubskonto thisYearKonto = mock(Urlaubskonto.class);
        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person());

        // wenn personService seine dependcies fragt, soll DAS rauskommen
        Mockito.when(personDAO.findAll()).thenReturn(personList);
        Mockito.when(lastYearKonto.getVacationDays()).thenReturn(10.0);
        Mockito.when(thisYearKonto.getVacationDays()).thenReturn(5.0);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(
            defaultUrlaubsanspruch);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(
            lastYearKonto);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(
            thisYearKonto);

        // rufe zu testende methode auf
        instance.updateVacationDays(2001);

        Mockito.verify(thisYearKonto).setVacationDays(15.0);

        // das neue urlaubskonto sollte auch gespeichert werden
        Mockito.verify(kontoService).saveUrlaubskonto(thisYearKonto);
    }


    /** Test of getAllUrlauberForThisWeekAndPutItInAnEmail method, of class PersonServiceImpl. */
    @Test
    public void testGetAllUrlauberForThisWeekAndPutItInAnEmail() {

        System.out.println("getAllUrlauberForThisWeekAndPutItInAnEmail");

        DateMidnight startDate = new DateMidnight();
        DateMidnight endDate = new DateMidnight();
        List<Antrag> antraege = new ArrayList<Antrag>();
        Antrag antrag = new Antrag();
        Person person = new Person();
        person.setFirstName("hans-peter");
        antrag.setPerson(person);
        antraege.add(antrag);

        Mockito.when(antragService.getAllRequestsForACertainTime(startDate, endDate)).thenReturn(antraege);

        instance.getAllUrlauberForThisWeekAndPutItInAnEmail(startDate, endDate);

        Mockito.verify(mailService).sendWeeklyVacationForecast(Mockito.anyList());

        Mockito.doAnswer(new Answer() {

                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    List<Person> catchedList = (List<Person>) (args[0]);
                    assertEquals("hans-peter", catchedList.get(0).getFirstName());

                    return null;
                }
            }).when(mailService).sendWeeklyVacationForecast(Mockito.anyList());
    }


    /** Test of getUrlaubsanspruchByPersonAndYear method, of class PersonServiceImpl. */
    @Test
    public void testGetUrlaubsanspruchByPersonAndYear() {

        Person person = new Person();
        Urlaubsanspruch anspruch = new Urlaubsanspruch();

        Mockito.when(urlaubsanspruchDAO.getUrlaubsanspruchByDate(2000, person)).thenReturn(anspruch);

        Urlaubsanspruch gotByMethod = instance.getUrlaubsanspruchByPersonAndYear(person, 2000);

        assertEquals(gotByMethod, anspruch);
    }


    /** Test of getUrlaubsanspruchByPersonForAllYears method, of class PersonServiceImpl. */
    @Test
    public void testGetUrlaubsanspruchByPersonForAllYears() {

        Person person = new Person();
        List<Urlaubsanspruch> ansprueche = new ArrayList<Urlaubsanspruch>();
        Urlaubsanspruch anspruch = new Urlaubsanspruch();

        ansprueche.add(anspruch);

        Mockito.when(urlaubsanspruchDAO.getUrlaubsanspruchByPerson(person)).thenReturn(ansprueche);

        List<Urlaubsanspruch> gotByMethod = instance.getUrlaubsanspruchByPersonForAllYears(person);

        assertEquals(gotByMethod.get(0), anspruch);
    }


    /** Test of setUrlaubsanspruchForPerson method, of class PersonServiceImpl. */
    @Test
    public void testSetUrlaubsanspruchForPerson() {

        Person person = new Person();

        person.setFirstName("schnullibulli"); // ich bin nicht verrückt...

        Mockito.when(urlaubsanspruchDAO.save((Urlaubsanspruch) (Mockito.any()))).thenAnswer(new Answer() {

                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    Urlaubsanspruch catchedAnspruch = (Urlaubsanspruch) (args[0]);
                    assertEquals(catchedAnspruch.getPerson().getFirstName(), "schnullibulli");

//                        by Jojo:
//                        assertEquals((int) (catchedAnspruch.getVacationDays()), (int) (26.0)); //int-cast, weil sonst die 1000x überschriebene assertEquals() nicht eindeutig is....

//                        by aljona, nach Typechange zu double:
                    assertEquals(catchedAnspruch.getVacationDays(), 26.0, 0.0);
                    assertEquals((int) (catchedAnspruch.getYear()), (int) (2011));

                    return null;
                }
            });

        instance.setUrlaubsanspruchForPerson(person, 2011, 26.0);

        Mockito.verify(urlaubsanspruchDAO).save((Urlaubsanspruch) (Mockito.any()));
    }


    /** Test of getPersonByLogin method, of class PersonServiceImpl. */
    @Test
    public void testGetPersonByLogin() {

        Person person = new Person();

        Mockito.when(personDAO.getPersonByLogin("abcdastutnichtweh")).thenReturn(person);

        Person returnedPerson = instance.getPersonByLogin("abcdastutnichtweh");
        assertEquals(person, returnedPerson);
    }
}
