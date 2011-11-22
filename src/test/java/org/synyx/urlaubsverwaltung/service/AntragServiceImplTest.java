/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;
import org.synyx.urlaubsverwaltung.domain.VacationType;


/**
 * @author  aljona
 */
public class AntragServiceImplTest {

    private AntragServiceImpl instance;
    private Antrag antrag;
    private Person person;
    private Urlaubskonto konto;

    private AntragDAO antragDAO = mock(AntragDAO.class);
    private KontoService kontoService = mock(KontoService.class);
    private PGPService pgpService = new PGPService();
    private OwnCalendarService calendarService = new OwnCalendarService();
    private PersonService personService = mock(PersonService.class);
    private MailService mailService = mock(MailService.class);

    public AntragServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new AntragServiceImpl(antragDAO, kontoService, pgpService, calendarService, personService,
                mailService);

        // person erzeugen, die fuer tests gebraucht wird
        person = new Person();
        person.setLastName("Testperson");

        // antrag erzeugen, der fuer tests gebraucht wird
        antrag = new Antrag();
        antrag.setPerson(person);

        // konto fuer person erzeugen
        konto = new Urlaubskonto();
        konto.setPerson(person);
    }


    @After
    public void tearDown() {
    }


    /** Test of getRequestById method, of class AntragServiceImpl. */
    @Test
    public void testGetRequestById() {

        // only DAO stuff
    }


    /** Test of getAllRequestsForPerson method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsForPerson() {

        // only DAO stuff
    }


    /** Test of getAllRequests method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequests() {

        // only DAO stuff
    }


    /** Test of getAllRequestsByState method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsByState() {

        // only DAO stuff
    }


    /** Test of getAllRequestsForACertainTime method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsForACertainTime() {

        // only DAO stuff
    }


    /** Test of wait method, of class AntragServiceImpl. */
    @Test
    public void testWait() {

        antrag.setStatus(State.GENEHMIGT);
        instance.wait(antrag);

        assertEquals(State.WARTEND, antrag.getStatus());
    }


    /** Test of approve method, of class AntragServiceImpl. */
    @Test
    public void testApprove() {

        antrag.setStartDate(new DateMidnight(2011, 12, 17));
        antrag.setEndDate(new DateMidnight(2011, 12, 27));
        antrag.setStatus(State.WARTEND);

        instance.approve(antrag);

        assertEquals(State.GENEHMIGT, antrag.getStatus());
        assertNotNull(antrag.getBeantragteTageNetto());
        assertEquals(6.0, antrag.getBeantragteTageNetto(), 0.0);
    }


    /** Test of save method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testSave() {

        System.out.println("save");

        Antrag antrag = null;
        AntragServiceImpl instance = null;
        instance.save(antrag);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of decline method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testDecline() {

        System.out.println("decline");

        Antrag antrag = null;
        Person boss = null;
        String reasonToDecline = "";
        AntragServiceImpl instance = null;
        instance.decline(antrag, boss, reasonToDecline);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of storno method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testStorno() {

        System.out.println("storno");

        Antrag antrag = null;
        AntragServiceImpl instance = null;
        instance.storno(antrag);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of rollbackNoticeJanuary method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testRollbackNoticeJanuary() {

        System.out.println("rollbackNoticeJanuary");

        Antrag antrag = null;
        DateMidnight start = null;
        DateMidnight end = null;
        AntragServiceImpl instance = null;
        instance.rollbackNoticeJanuary(antrag, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of rollbackNoticeApril method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testRollbackNoticeApril() {

        System.out.println("rollbackNoticeApril");

        Antrag antrag = null;
        DateMidnight start = null;
        DateMidnight end = null;
        AntragServiceImpl instance = null;
        instance.rollbackNoticeApril(antrag, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of krankheitBeachten method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testKrankheitBeachten() {

        System.out.println("krankheitBeachten");

        Antrag antrag = null;
        Double krankheitsTage = null;
        AntragServiceImpl instance = null;
        instance.krankheitBeachten(antrag, krankheitsTage);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of signAntrag method, of class AntragServiceImpl. */
    @Test
    public void testSignAntrag() throws Exception {

        // person braucht einige infos: private key, nachname
        person.setPrivateKey(pgpService.generateKeyPair().getPrivate().getEncoded());

        // antrag braucht auch daten
        antrag.setPerson(person);
        antrag.setVacationType(VacationType.SONDERURLAUB);
        antrag.setAntragsDate(new DateMidnight(2011, 11, 1));

        // boss oder nicht
        boolean isBoss = true;

        instance.signAntrag(antrag, person, isBoss);

        assertNotNull(antrag.getSignedDataBoss());
        assertEquals(null, antrag.getSignedDataPerson());

        // roll back
        antrag.setSignedDataBoss(null);
        antrag.setSignedDataPerson(null);

        // nicht boss
        isBoss = false;

        instance.signAntrag(antrag, person, isBoss);

        assertNotNull(antrag.getSignedDataPerson());
        assertEquals(null, antrag.getSignedDataBoss());
    }


    /** Test of checkAntrag method, of class AntragServiceImpl. */
    @Test
    public void testCheckAntrag() {

        // Moegliche Faelle:
        // 1. ueber 1. Jan.
        // 2. ueber 1.4.
        // 3. vor 1.4.
        // 4. nach 1.4.
        // 5. Tage reichen == true
        // 6. Tage reichen nicht == false

        konto.setRestVacationDays(5.0);
        konto.setVacationDays(26.0);
        konto.setYear(2011);

        antrag.setStartDate(new DateMidnight(2011, 1, 12));
        antrag.setEndDate(new DateMidnight(2011, 1, 30));

        boolean returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);
    }


    /** Test of noticeJanuary method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testNoticeJanuary() {

        System.out.println("noticeJanuary");

        Antrag antrag = null;
        DateMidnight start = null;
        DateMidnight end = null;
        boolean mustBeSaved = false;
        AntragServiceImpl instance = null;
        instance.noticeJanuary(antrag, start, end, mustBeSaved);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /** Test of noticeApril method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testNoticeApril() {

        System.out.println("noticeApril");

        Antrag antrag = null;
        Urlaubskonto konto = null;
        DateMidnight start = null;
        DateMidnight end = null;
        AntragServiceImpl instance = null;
        instance.noticeApril(antrag, konto, start, end);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
