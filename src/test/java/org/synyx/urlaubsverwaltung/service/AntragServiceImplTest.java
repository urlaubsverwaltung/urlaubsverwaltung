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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.AntragStatus;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;
import org.synyx.urlaubsverwaltung.domain.VacationType;


/**
 * @author  aljona
 */
@Ignore
public class AntragServiceImplTest {

    private AntragServiceImpl instance;
    private Antrag antrag;
    private Person person;
    private Urlaubskonto kontoOne;
    private Urlaubskonto kontoTwo;
    private Urlaubsanspruch anspruch;

    private AntragDAO antragDAO = mock(AntragDAO.class);
    private KontoService kontoService = mock(KontoService.class);
    private CryptoService pgpService = new CryptoService();
    private OwnCalendarService calendarService = new OwnCalendarService();
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

        instance = new AntragServiceImpl(antragDAO, kontoService, pgpService, calendarService, mailService);

        // person erzeugen, die fuer tests gebraucht wird
        person = new Person();
        person.setLastName("Testperson");

        // antrag erzeugen, der fuer tests gebraucht wird
        antrag = new Antrag();
        antrag.setPerson(person);

        // konto fuer person erzeugen
        kontoOne = new Urlaubskonto();
        kontoOne.setPerson(person);

        kontoTwo = new Urlaubskonto();
        kontoTwo.setPerson(person);

        anspruch = new Urlaubsanspruch();
        anspruch.setPerson(person);

        Mockito.when(kontoService.getUrlaubskonto(2011, person)).thenReturn(kontoOne);
        Mockito.when(kontoService.getUrlaubskonto(2012, person)).thenReturn(kontoTwo);
        Mockito.when(kontoService.getUrlaubsanspruch(2011, person)).thenReturn(anspruch);
    }


    @After
    public void tearDown() {
    }


    /** Test of getRequestById method, of class AntragServiceImpl. */
    @Test
    public void testGetRequestById() {

        instance.getRequestById(1234);
        Mockito.verify(antragDAO).findOne(1234);
    }


    /** Test of getAllRequestsForPerson method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsForPerson() {

        Person person = new Person();
        instance.getAllRequestsForPerson(person);
        Mockito.verify(antragDAO).getAllRequestsForPerson(person);
    }


    /** Test of getAllRequests method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequests() {

        instance.getAllRequests();
        Mockito.verify(antragDAO).findAll();
    }


    /** Test of getAllRequestsByState method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsByState() {

        instance.getAllRequestsByState(AntragStatus.WARTEND);
        Mockito.verify(antragDAO).getAllRequestsByState(AntragStatus.WARTEND);
    }


    /** Test of getAllRequestsForACertainTime method, of class AntragServiceImpl. */
    @Test
    public void testGetAllRequestsForACertainTime() {

        DateMidnight start = DateMidnight.now();
        DateMidnight end = DateMidnight.now();

        instance.getAllRequestsForACertainTime(start, end);
        Mockito.verify(antragDAO).getAllRequestsForACertainTime(start, end);
    }


    /** Test of wait method, of class AntragServiceImpl. */
    @Test
    public void testWait() {

        antrag.setStatus(AntragStatus.GENEHMIGT);
        instance.wait(antrag);

        assertEquals(AntragStatus.WARTEND, antrag.getStatus());
    }


    /** Test of approve method, of class AntragServiceImpl. */
    @Test
    public void testApprove() {

        antrag.setStartDate(new DateMidnight(2011, 12, 17));
        antrag.setEndDate(new DateMidnight(2011, 12, 27));
        antrag.setStatus(AntragStatus.WARTEND);

        instance.approve(antrag);

        assertEquals(AntragStatus.GENEHMIGT, antrag.getStatus());
        assertNotNull(antrag.getBeantragteTageNetto());
        assertEquals(6.0, antrag.getBeantragteTageNetto(), 0.0);
    }


    /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSave1Year() {

        Antrag antrag = new Antrag();
        antrag.setStartDate(new DateMidnight(2000, 3, 10));
        antrag.setEndDate(new DateMidnight(2000, 3, 20));

        Urlaubsanspruch anspruch = new Urlaubsanspruch();
        anspruch.setVacationDays(10.0);

        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch);

        instance.save(antrag);
        Mockito.verify(antragDAO).save(antrag);
        Mockito.verify(kontoService).noticeApril((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()));
    }


    /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSave2Years() {

        Antrag antrag = new Antrag();
        antrag.setStartDate(new DateMidnight(2000, 12, 20));
        antrag.setEndDate(new DateMidnight(2001, 1, 10));

        Urlaubsanspruch anspruch = new Urlaubsanspruch();
        anspruch.setVacationDays(10.0);

        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch);

        instance.save(antrag);
        Mockito.verify(antragDAO).save(antrag);
        Mockito.verify(kontoService).noticeJanuary((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()),
            (Urlaubskonto) (Mockito.any()));
    }


    /** Test of decline method, of class AntragServiceImpl. */
    @Test
    public void testDecline() {

        // vorgefertigte Infos setzen
        Person boss = new Person();
        boss.setLastName("Testboss");

        String reason = "Einfach so halt, weil ich Bock drauf hab.";

        antrag.setStatus(AntragStatus.WARTEND);

        // bei Aufruf der Methode werden Infos geaendert/gesetzt

        instance.decline(antrag, boss, reason);

        assertEquals(AntragStatus.ABGELEHNT, antrag.getStatus());

        assertNotNull(antrag.getBoss());
        assertEquals(boss, antrag.getBoss());

        assertNotNull(antrag.getReasonToDecline());
        assertEquals(reason, antrag.getReasonToDecline().getText());
    }


    /** Test of storno method, of class AntragServiceImpl. */
    @Test
    public void testStorno() {

        antrag.setStatus(AntragStatus.WARTEND);

        instance.storno(antrag);

        assertEquals(AntragStatus.STORNIERT, antrag.getStatus());
    }


    /** Test of krankheitBeachten method, of class AntragServiceImpl. */
    @Test
    public void testKrankheitBeachten() {

        // kein Sonderfall:
        // newVacDays < anspruch

        Double krankheitsTage = 3.0;
        Double daysStart = 16.0;
        Double nettoTage = 10.0;
        Double vacAnspruch = 24.0;

        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(daysStart);
        kontoOne.setYear(2011);

        antrag.setBeantragteTageNetto(nettoTage);
        antrag.setStartDate(new DateMidnight(2011, 11, 1));
        antrag.setEndDate(new DateMidnight(2011, 11, 16));

        anspruch.setVacationDays(vacAnspruch);

        instance.krankheitBeachten(antrag, krankheitsTage);

        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);

        assertEquals((daysStart + krankheitsTage), kontoOne.getVacationDays(), 0.0);

        // Sonderfall:
        // newVacDays > anspruch
        // UND
        // es ist vor April

        krankheitsTage = 10.0;
        nettoTage = 15.0;
        daysStart = 20.0;
        vacAnspruch = 23.0;

        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(daysStart);
        kontoOne.setYear(2011);

        antrag.setBeantragteTageNetto(nettoTage);
        antrag.setStartDate(new DateMidnight(2011, 2, 1));
        antrag.setEndDate(new DateMidnight(2011, 2, 20));

        anspruch.setVacationDays(vacAnspruch);

        instance.krankheitBeachten(antrag, krankheitsTage);

        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);

        assertEquals((krankheitsTage + daysStart) - vacAnspruch, kontoOne.getRestVacationDays(), 0.0);
        assertEquals((vacAnspruch), kontoOne.getVacationDays(), 0.0);

        // Sonderfall:
        // newVacDays > anspruch
        // UND
        // es ist nach April

        krankheitsTage = 10.0;
        nettoTage = 12.0;
        daysStart = 20.0;
        vacAnspruch = 23.0;

        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(daysStart);
        kontoOne.setYear(2011);

        antrag.setBeantragteTageNetto(nettoTage);
        antrag.setStartDate(new DateMidnight(2011, 4, 5));
        antrag.setEndDate(new DateMidnight(2011, 4, 23));

        anspruch.setVacationDays(vacAnspruch);

        instance.krankheitBeachten(antrag, krankheitsTage);

        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);

        assertEquals(0.0, kontoOne.getRestVacationDays(), 0.0);
        assertEquals((vacAnspruch), kontoOne.getVacationDays(), 0.0);
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

        // TEST 1 - kein Sonderfall, genug Urlaubstage
        kontoOne.setRestVacationDays(5.0);
        kontoOne.setVacationDays(26.0);
        kontoOne.setYear(2011);

        antrag.setStartDate(new DateMidnight(2011, 1, 12));
        antrag.setEndDate(new DateMidnight(2011, 1, 30));

        boolean returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 2 - kein Sonderfall, zu wenig Urlaubstage
        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(5.0);
        kontoOne.setYear(2011);

        antrag.setStartDate(new DateMidnight(2011, 12, 12));
        antrag.setEndDate(new DateMidnight(2011, 12, 23));

        returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);

        // TEST 3 - Sonderfall April, genug Urlaubstage
        kontoOne.setRestVacationDays(10.0);
        kontoOne.setVacationDays(26.0);
        kontoOne.setYear(2011);

        antrag.setStartDate(new DateMidnight(2011, 3, 28));
        antrag.setEndDate(new DateMidnight(2011, 4, 23));

        returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 4 - Sonderfall Januar, genug Urlaubstage
        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(10.0);
        kontoOne.setYear(2011);

        kontoTwo.setRestVacationDays(2.0);
        kontoTwo.setVacationDays(26.0);
        kontoTwo.setYear(2012);

        antrag.setStartDate(new DateMidnight(2011, 12, 19));
        antrag.setEndDate(new DateMidnight(2012, 1, 5));

        returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(true, returnValue);

        // TEST 5 - Sonderfall Januar, zu wenig Urlaubstage
        kontoOne.setRestVacationDays(0.0);
        kontoOne.setVacationDays(5.0);
        kontoOne.setYear(2011);

        kontoTwo.setRestVacationDays(0.0);
        kontoTwo.setVacationDays(26.0);
        kontoTwo.setYear(2012);

        antrag.setStartDate(new DateMidnight(2011, 12, 19));
        antrag.setEndDate(new DateMidnight(2012, 1, 5));

        returnValue = instance.checkAntrag(antrag);
        assertNotNull(returnValue);
        assertEquals(false, returnValue);
    }
}
