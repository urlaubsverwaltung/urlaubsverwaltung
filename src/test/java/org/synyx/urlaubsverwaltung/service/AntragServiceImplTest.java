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

        instance = new AntragServiceImpl(antragDAO, personDAO, urlaubsanspruchDAO, urlaubskontoDAO, kontoService,
                dateService, pgpService, mailService);
    }


    @After
    public void tearDown() {
    }


    /** Test of save method, of class AntragServiceImpl. */
    @Ignore
    @Test
    public void testSave() {

        // nur DAO stuff, nothing to test
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
    public void testStorno() {

        // not yet implemented
        // seeeeeeehr riesige logik
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
    public void testKrankheitBeachten() {

        // not yet implemented
        // fette logik
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
