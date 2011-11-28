/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  aljona
 */
@Ignore
public class ApplicationServiceImplTest {

    private ApplicationServiceImpl instance;
    private Application antrag;
    private Person person;
    private HolidaysAccount kontoOne;
    private HolidaysAccount kontoTwo;
    private HolidayEntitlement anspruch;

    private ApplicationDAO antragDAO = mock(ApplicationDAO.class);
    private HolidaysAccountService kontoService = mock(HolidaysAccountService.class);
    private CryptoService pgpService = new CryptoService();
    private OwnCalendarService calendarService = new OwnCalendarService();
    private MailService mailService = mock(MailService.class);

    public ApplicationServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

//        instance = new ApplicationServiceImpl(antragDAO, kontoService, pgpService, calendarService, mailService);
//
//        // person erzeugen, die fuer tests gebraucht wird
//        person = new Person();
//        person.setLastName("Testperson");
//
//        // antrag erzeugen, der fuer tests gebraucht wird
//        antrag = new Application();
//        antrag.setPerson(person);
//
//        // konto fuer person erzeugen
//        kontoOne = new HolidaysAccount();
//        kontoOne.setPerson(person);
//
//        kontoTwo = new HolidaysAccount();
//        kontoTwo.setPerson(person);
//
//        anspruch = new HolidayEntitlement();
//        anspruch.setPerson(person);
//
//        Mockito.when(kontoService.getUrlaubskonto(2011, person)).thenReturn(kontoOne);
//        Mockito.when(kontoService.getUrlaubskonto(2012, person)).thenReturn(kontoTwo);
//        Mockito.when(kontoService.getUrlaubsanspruch(2011, person)).thenReturn(anspruch);
    }


    @After
    public void tearDown() {
    }

//
//
//    /** Test of getRequestById method, of class ApplicationServiceImpl. */
//    @Test
//    public void testGetRequestById() {
//
//        instance.getRequestById(1234);
//        Mockito.verify(antragDAO).findOne(1234);
//    }
//
//
//    /** Test of getAllRequestsForPerson method, of class ApplicationServiceImpl. */
//    @Test
//    public void testGetAllRequestsForPerson() {
//
//        Person person = new Person();
//        instance.getAllRequestsForPerson(person);
//        Mockito.verify(antragDAO).getAllRequestsForPerson(person);
//    }
//
//
//    /** Test of getAllRequests method, of class ApplicationServiceImpl. */
//    @Test
//    public void testGetAllRequests() {
//
//        instance.getAllRequests();
//        Mockito.verify(antragDAO).findAll();
//    }
//
//
//    /** Test of getAllRequestsByState method, of class ApplicationServiceImpl. */
//    @Test
//    public void testGetAllRequestsByState() {
//
//        instance.getAllRequestsByState(ApplicationStatus.WARTEND);
//        Mockito.verify(antragDAO).getAllRequestsByState(ApplicationStatus.WARTEND);
//    }
//
//
//    /** Test of getAllRequestsForACertainTime method, of class ApplicationServiceImpl. */
//    @Test
//    public void testGetAllRequestsForACertainTime() {
//
//        DateMidnight start = DateMidnight.now();
//        DateMidnight end = DateMidnight.now();
//
//        instance.getAllRequestsForACertainTime(start, end);
//        Mockito.verify(antragDAO).getAllRequestsForACertainTime(start, end);
//    }
//
//
//    /** Test of wait method, of class ApplicationServiceImpl. */
//    @Test
//    public void testWait() {
//
//        antrag.setStatus(ApplicationStatus.GENEHMIGT);
//        instance.wait(antrag);
//
//        assertEquals(ApplicationStatus.WARTEND, antrag.getStatus());
//    }
//
//
//    /** Test of allow method, of class ApplicationServiceImpl. */
//    @Test
//    public void testApprove() {
//
//        antrag.setStartDate(new DateMidnight(2011, 12, 17));
//        antrag.setEndDate(new DateMidnight(2011, 12, 27));
//        antrag.setStatus(ApplicationStatus.WARTEND);
//
//        instance.allow(antrag);
//
//        assertEquals(ApplicationStatus.GENEHMIGT, antrag.getStatus());
//        assertNotNull(antrag.getBeantragteTageNetto());
//        assertEquals(6.0, antrag.getBeantragteTageNetto(), 0.0);
//    }
//
//
//    /** Test of save method, of class ApplicationServiceImpl. */
//    @Test
//    public void testSave1Year() {
//
//        Application antrag = new Application();
//        antrag.setStartDate(new DateMidnight(2000, 3, 10));
//        antrag.setEndDate(new DateMidnight(2000, 3, 20));
//
//        HolidayEntitlement anspruch = new HolidayEntitlement();
//        anspruch.setVacationDays(10.0);
//
//        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch);
//
//        instance.save(antrag);
//        Mockito.verify(antragDAO).save(antrag);
//        Mockito.verify(kontoService).noticeApril((Application) (Mockito.any()), (HolidaysAccount) (Mockito.any()));
//    }
//
//
//    /** Test of save method, of class ApplicationServiceImpl. */
//    @Test
//    public void testSave2Years() {
//
//        Application antrag = new Application();
//        antrag.setStartDate(new DateMidnight(2000, 12, 20));
//        antrag.setEndDate(new DateMidnight(2001, 1, 10));
//
//        HolidayEntitlement anspruch = new HolidayEntitlement();
//        anspruch.setVacationDays(10.0);
//
//        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch);
//
//        instance.save(antrag);
//        Mockito.verify(antragDAO).save(antrag);
//        Mockito.verify(kontoService).noticeJanuary((Application) (Mockito.any()), (HolidaysAccount) (Mockito.any()),
//            (HolidaysAccount) (Mockito.any()));
//    }
//
//
//    /** Test of reject method, of class ApplicationServiceImpl. */
//    @Test
//    public void testDecline() {
//
//        // vorgefertigte Infos setzen
//        Person boss = new Person();
//        boss.setLastName("Testboss");
//
//        String reason = "Einfach so halt, weil ich Bock drauf hab.";
//
//        antrag.setStatus(ApplicationStatus.WARTEND);
//
//        // bei Aufruf der Methode werden Infos geaendert/gesetzt
//
//        instance.reject(antrag, boss, reason);
//
//        assertEquals(ApplicationStatus.ABGELEHNT, antrag.getStatus());
//
//        assertNotNull(antrag.getBoss());
//        assertEquals(boss, antrag.getBoss());
//
//        assertNotNull(antrag.getReasonToDecline());
//        assertEquals(reason, antrag.getReasonToDecline().getText());
//    }
//
//
//    /** Test of cancel method, of class ApplicationServiceImpl. */
//    @Test
//    public void testStorno() {
//
//        antrag.setStatus(ApplicationStatus.WARTEND);
//
//        instance.cancel(antrag);
//
//        assertEquals(ApplicationStatus.STORNIERT, antrag.getStatus());
//    }
//
//
//    /** Test of krankheitBeachten method, of class ApplicationServiceImpl. */
//    @Test
//    public void testKrankheitBeachten() {
//
//        // kein Sonderfall:
//        // newVacDays < anspruch
//
//        Double krankheitsTage = 3.0;
//        Double daysStart = 16.0;
//        Double nettoTage = 10.0;
//        Double vacAnspruch = 24.0;
//
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(daysStart);
//        kontoOne.setYear(2011);
//
//        antrag.setBeantragteTageNetto(nettoTage);
//        antrag.setStartDate(new DateMidnight(2011, 11, 1));
//        antrag.setEndDate(new DateMidnight(2011, 11, 16));
//
//        anspruch.setVacationDays(vacAnspruch);
//
//        instance.krankheitBeachten(antrag, krankheitsTage);
//
//        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
//        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);
//
//        assertEquals((daysStart + krankheitsTage), kontoOne.getVacationDays(), 0.0);
//
//        // Sonderfall:
//        // newVacDays > anspruch
//        // UND
//        // es ist vor April
//
//        krankheitsTage = 10.0;
//        nettoTage = 15.0;
//        daysStart = 20.0;
//        vacAnspruch = 23.0;
//
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(daysStart);
//        kontoOne.setYear(2011);
//
//        antrag.setBeantragteTageNetto(nettoTage);
//        antrag.setStartDate(new DateMidnight(2011, 2, 1));
//        antrag.setEndDate(new DateMidnight(2011, 2, 20));
//
//        anspruch.setVacationDays(vacAnspruch);
//
//        instance.krankheitBeachten(antrag, krankheitsTage);
//
//        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
//        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);
//
//        assertEquals((krankheitsTage + daysStart) - vacAnspruch, kontoOne.getRestVacationDays(), 0.0);
//        assertEquals((vacAnspruch), kontoOne.getVacationDays(), 0.0);
//
//        // Sonderfall:
//        // newVacDays > anspruch
//        // UND
//        // es ist nach April
//
//        krankheitsTage = 10.0;
//        nettoTage = 12.0;
//        daysStart = 20.0;
//        vacAnspruch = 23.0;
//
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(daysStart);
//        kontoOne.setYear(2011);
//
//        antrag.setBeantragteTageNetto(nettoTage);
//        antrag.setStartDate(new DateMidnight(2011, 4, 5));
//        antrag.setEndDate(new DateMidnight(2011, 4, 23));
//
//        anspruch.setVacationDays(vacAnspruch);
//
//        instance.krankheitBeachten(antrag, krankheitsTage);
//
//        assertEquals(krankheitsTage, antrag.getKrankheitsTage(), 0.0);
//        assertEquals((nettoTage - krankheitsTage), antrag.getBeantragteTageNetto(), 0.0);
//
//        assertEquals(0.0, kontoOne.getRestVacationDays(), 0.0);
//        assertEquals((vacAnspruch), kontoOne.getVacationDays(), 0.0);
//    }
//
//
//    /** Test of signAntrag method, of class ApplicationServiceImpl. */
//    @Test
//    public void testSignAntrag() throws Exception {
//
//        // person braucht einige infos: private key, nachname
//        person.setPrivateKey(pgpService.generateKeyPair().getPrivate().getEncoded());
//
//        // antrag braucht auch daten
//        antrag.setPerson(person);
//        antrag.setVacationType(VacationType.SONDERURLAUB);
//        antrag.setAntragsDate(new DateMidnight(2011, 11, 1));
//
//        // boss oder nicht
//        boolean isBoss = true;
//
//        instance.signAntrag(antrag, person, isBoss);
//
//        assertNotNull(antrag.getSignedDataBoss());
//        assertEquals(null, antrag.getSignedDataPerson());
//
//        // roll back
//        antrag.setSignedDataBoss(null);
//        antrag.setSignedDataPerson(null);
//
//        // nicht boss
//        isBoss = false;
//
//        instance.signAntrag(antrag, person, isBoss);
//
//        assertNotNull(antrag.getSignedDataPerson());
//        assertEquals(null, antrag.getSignedDataBoss());
//    }
//
//
//    /** Test of checkAntrag method, of class ApplicationServiceImpl. */
//    @Test
//    public void testCheckAntrag() {
//
//        // Moegliche Faelle:
//        // 1. ueber 1. Jan.
//        // 2. ueber 1.4.
//        // 3. vor 1.4.
//        // 4. nach 1.4.
//        // 5. Tage reichen == true
//        // 6. Tage reichen nicht == false
//
//        // TEST 1 - kein Sonderfall, genug Urlaubstage
//        kontoOne.setRestVacationDays(5.0);
//        kontoOne.setVacationDays(26.0);
//        kontoOne.setYear(2011);
//
//        antrag.setStartDate(new DateMidnight(2011, 1, 12));
//        antrag.setEndDate(new DateMidnight(2011, 1, 30));
//
//        boolean returnValue = instance.checkAntrag(antrag);
//        assertNotNull(returnValue);
//        assertEquals(true, returnValue);
//
//        // TEST 2 - kein Sonderfall, zu wenig Urlaubstage
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(5.0);
//        kontoOne.setYear(2011);
//
//        antrag.setStartDate(new DateMidnight(2011, 12, 12));
//        antrag.setEndDate(new DateMidnight(2011, 12, 23));
//
//        returnValue = instance.checkAntrag(antrag);
//        assertNotNull(returnValue);
//        assertEquals(false, returnValue);
//
//        // TEST 3 - Sonderfall April, genug Urlaubstage
//        kontoOne.setRestVacationDays(10.0);
//        kontoOne.setVacationDays(26.0);
//        kontoOne.setYear(2011);
//
//        antrag.setStartDate(new DateMidnight(2011, 3, 28));
//        antrag.setEndDate(new DateMidnight(2011, 4, 23));
//
//        returnValue = instance.checkAntrag(antrag);
//        assertNotNull(returnValue);
//        assertEquals(true, returnValue);
//
//        // TEST 4 - Sonderfall Januar, genug Urlaubstage
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(10.0);
//        kontoOne.setYear(2011);
//
//        kontoTwo.setRestVacationDays(2.0);
//        kontoTwo.setVacationDays(26.0);
//        kontoTwo.setYear(2012);
//
//        antrag.setStartDate(new DateMidnight(2011, 12, 19));
//        antrag.setEndDate(new DateMidnight(2012, 1, 5));
//
//        returnValue = instance.checkAntrag(antrag);
//        assertNotNull(returnValue);
//        assertEquals(true, returnValue);
//
//        // TEST 5 - Sonderfall Januar, zu wenig Urlaubstage
//        kontoOne.setRestVacationDays(0.0);
//        kontoOne.setVacationDays(5.0);
//        kontoOne.setYear(2011);
//
//        kontoTwo.setRestVacationDays(0.0);
//        kontoTwo.setVacationDays(26.0);
//        kontoTwo.setYear(2012);
//
//        antrag.setStartDate(new DateMidnight(2011, 12, 19));
//        antrag.setEndDate(new DateMidnight(2012, 1, 5));
//
//        returnValue = instance.checkAntrag(antrag);
//        assertNotNull(returnValue);
//        assertEquals(false, returnValue);
//    }
}
