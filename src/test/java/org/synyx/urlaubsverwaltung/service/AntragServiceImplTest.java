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

import static org.mockito.Mockito.mock;

import org.synyx.urlaubsverwaltung.dao.AntragDAO;
import org.synyx.urlaubsverwaltung.dao.PersonDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.util.DateService;

import java.security.KeyPair;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;


/**
 * @author  aljona
 */
public class AntragServiceImplTest {

    private AntragServiceImpl instance;

    private AntragDAO antragDAO = mock(AntragDAO.class);
    private PersonDAO personDAO = mock(PersonDAO.class);
    private UrlaubsanspruchDAO urlaubsanspruchDAO = mock(UrlaubsanspruchDAO.class);
    private UrlaubskontoDAO urlaubskontoDAO = mock(UrlaubskontoDAO.class);
    private KontoService kontoService = mock(KontoService.class);
    private DateService dateService = mock(DateService.class);
    private PGPService pgpService = new PGPService();
    private MailServiceImpl mailService = mock(MailServiceImpl.class);

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

        instance = new AntragServiceImpl(antragDAO, personDAO, kontoService,
                dateService, pgpService, mailService);
    }


    @After
    public void tearDown() {
    }


    /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSaveEasy() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(20);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(new DateMidnight(2000, 6, 1));
        antrag.setEndDate(new DateMidnight(2000, 6, 8));
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.save(antrag);
        
        Mockito.verify(konto2000).setVacationDays(13);    
    }
    
    /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSaveRest() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(20);
        Mockito.when(konto2000.getRestVacationDays()).thenReturn(5);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(new DateMidnight(2000, 2, 1));
        antrag.setEndDate(new DateMidnight(2000, 2, 8));
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.save(antrag);
        
        Mockito.verify(konto2000).setVacationDays(18);    
        Mockito.verify(konto2000).setRestVacationDays(0);    
    }
    
    
        /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSaveApril() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 3, 27);
        DateMidnight endDate = new DateMidnight(2000, 4, 4);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(20);
        Mockito.when(konto2000.getRestVacationDays()).thenReturn(5);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        Mockito.when(dateService.countDaysBetweenTwoDates(Mockito.eq(startDate),(DateMidnight) (Mockito.any()))).thenReturn(3);
        Mockito.when(dateService.countDaysBetweenTwoDates((DateMidnight) (Mockito.any()), Mockito.eq(endDate))).thenReturn(4);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.save(antrag);
        
        Mockito.verify(konto2000).setVacationDays(16);    
        Mockito.verify(konto2000).setRestVacationDays(2);    
    }
    
    
            /** Test of save method, of class AntragServiceImpl. */
    @Test
    public void testSave2Years() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        Urlaubskonto konto2001 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2001 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 12, 27);
        DateMidnight endDate = new DateMidnight(2001, 1, 4);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(5);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        Mockito.when(konto2001.getVacationDays()).thenReturn(10);
        Mockito.when(anspruch2001.getVacationDays()).thenReturn(30);
        
        Mockito.when(dateService.countDaysBetweenTwoDates(Mockito.eq(startDate),(DateMidnight) (Mockito.any()))).thenReturn(3);
        Mockito.when(dateService.countDaysBetweenTwoDates((DateMidnight) (Mockito.any()), Mockito.eq(endDate))).thenReturn(4);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(konto2001);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(anspruch2001);
        
        instance.save(antrag);
        
        Mockito.verify(konto2000).setVacationDays(2);    
        Mockito.verify(konto2001).setVacationDays(6);    
    }


    /** Test of approve method, of class AntragServiceImpl. */
    @Test
    public void testApprove() {

        Antrag antrag = new Antrag();

        instance.approve(antrag);

        assertEquals(State.GENEHMIGT, antrag.getStatus());
    }


    /** Test of decline method, of class AntragServiceImpl. */
    @Test
    public void testDecline() {

        Antrag antrag = new Antrag();
        Person person = new Person();
        person.setLastName("Bossname");

        String reason = "irgendein Grund";

        instance.decline(antrag, person, reason);

        assertEquals(State.ABGELEHNT, antrag.getStatus());
        assertNotNull(antrag.getReasonToDecline());
        assertNotNull(antrag.getBoss());
        assertEquals("Bossname", antrag.getBoss().getLastName());
        assertEquals("irgendein Grund", antrag.getReasonToDecline().getText());
    }


    /** Test of wait method, of class AntragServiceImpl. */
    @Test
    public void testWait() {

        Antrag antrag = new Antrag();

        instance.wait(antrag);

        assertEquals(State.WARTEND, antrag.getStatus());
    }


    /** Test of storno method, of class AntragServiceImpl. */
    @Test
    public void testStornoEasy() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(20);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(new DateMidnight(2000, 6, 1));
        antrag.setEndDate(new DateMidnight(2000, 6, 8));
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.storno(antrag);
        
        Mockito.verify(konto2000).setVacationDays(27);    
    }
    
    
    @Test
    public void testStornoBeforeApril() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(30);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(new DateMidnight(2000, 3, 2));
        antrag.setEndDate(new DateMidnight(2000, 3, 4));
        antrag.setBeantragteTageNetto(2);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.storno(antrag);
        
        Mockito.verify(konto2000).setVacationDays(30);    
        Mockito.verify(konto2000).setRestVacationDays(2);    
    }
    
    
    @Test
    public void testStornoApril() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 3, 27);
        DateMidnight endDate = new DateMidnight(2000, 4, 4);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(26);
        Mockito.when(konto2000.getRestVacationDays()).thenReturn(4);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        Mockito.when(dateService.countDaysBetweenTwoDates(Mockito.eq(startDate),(DateMidnight) (Mockito.any()))).thenReturn(3);
        Mockito.when(dateService.countDaysBetweenTwoDates((DateMidnight) (Mockito.any()), Mockito.eq(endDate))).thenReturn(4);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(7);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.storno(antrag);
        
        Mockito.verify(konto2000).setVacationDays(30);    
        Mockito.verify(konto2000).setRestVacationDays(7);    
    }
    
    
    @Test
    public void testStorno2Years() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        Urlaubskonto konto2001 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2001 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 12, 25);
        DateMidnight endDate = new DateMidnight(2001, 1, 5);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(10);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        Mockito.when(konto2001.getVacationDays()).thenReturn(20);
        Mockito.when(anspruch2001.getVacationDays()).thenReturn(30);
        
        Mockito.when(dateService.countDaysBetweenTwoDates((DateMidnight) (Mockito.any()),(DateMidnight) (Mockito.any()))).thenReturn(5);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(10);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(konto2001);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2001), (Person) (Mockito.any()))).thenReturn(anspruch2001);
        
        instance.storno(antrag);
        
        Mockito.verify(konto2000).setVacationDays(15);    
        Mockito.verify(konto2001).setVacationDays(25);    
    }


    /** Test of getRequestById method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testGetRequestById() {

        // nur DAO stuff, nothing to test
    }


    /** Test of getAllRequestsForPerson method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testGetAllRequestsForPerson() {

        // nur DAO stuff, nothing to test
    }


    /** Test of getAllRequests method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testGetAllRequests() {

        // nur DAO stuff, nothing to test
    }


    /** Test of getAllRequestsByState method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testGetAllRequestsByState() {

        // nur DAO stuff, nothing to test
    }


    /** Test of getAllRequestsForACertainTime method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testGetAllRequestsForACertainTime() {

        // nur DAO stuff, nothing to test
    }


    /** Test of krankheitBeachten method, of class AntragServiceImpl. */
    @Test
    public void testKrankheitBeachtenApril() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 2, 5);
        DateMidnight endDate = new DateMidnight(2000, 2, 15);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(25);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(10);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
        instance.krankheitBeachten(antrag, 6);
        
        Mockito.verify(konto2000).setVacationDays(30);    
        Mockito.verify(konto2000).setRestVacationDays(1);    
    }
    
    
        /** Test of krankheitBeachten method, of class AntragServiceImpl. */
    @Test
    public void testKrankheitBeachtenNOTApril() {

        Antrag antrag = new Antrag();
        
        Urlaubskonto konto2000 = mock(Urlaubskonto.class);
        Urlaubsanspruch anspruch2000 = mock(Urlaubsanspruch.class);
        
        DateMidnight startDate = new DateMidnight(2000, 6, 5);
        DateMidnight endDate = new DateMidnight(2000, 6, 15);
        
        Mockito.when(konto2000.getVacationDays()).thenReturn(10);
        Mockito.when(anspruch2000.getVacationDays()).thenReturn(30);
        
        antrag.setStartDate(startDate);
        antrag.setEndDate(endDate);
        antrag.setBeantragteTageNetto(10);
        
        Mockito.when(kontoService.getUrlaubskonto(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(konto2000);
        Mockito.when(kontoService.getUrlaubsanspruch(Mockito.eq(2000), (Person) (Mockito.any()))).thenReturn(anspruch2000);
        
       instance.krankheitBeachten(antrag, 6);
        
        Mockito.verify(konto2000).setVacationDays(16);
    }


    /** Test of signAntrag method, of class AntragServiceImpl. */
    @Test
    public void testSignAntrag() throws Exception {

        Antrag antrag = new Antrag();
        Person person = new Person();

        KeyPair pair = pgpService.generateKeyPair();
        person.setPrivateKey(pair.getPrivate().getEncoded());
        person.setPublicKey(pair.getPublic().getEncoded());
        person.setLastName("Testname");

        antrag.setPerson(person);
        antrag.setAntragsDate(DateMidnight.now());
        antrag.setVacationType(VacationType.ERHOLUNGSURLAUB);

        boolean isBoss = true;

        instance.signAntrag(antrag, person, isBoss);

        assertNotNull(antrag.getSignedDataBoss());

        isBoss = false;
        instance.signAntrag(antrag, person, isBoss);

        assertNotNull(antrag.getSignedDataPerson());
    }
}
