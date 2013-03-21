/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.service.ApplicationServiceImpl;
import org.synyx.urlaubsverwaltung.security.CryptoService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

/**
 * Unit test for serivce {@link ApplicationServiceImpl}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class ApplicationServiceImplTest {

    private ApplicationServiceImpl instance;
    private ApplicationDAO applicationDAO;
    private CryptoService cryptoService;
    private MailService mailService;
    private OwnCalendarService calendarService;
    private Application application;
    private Person person;

    @Before
    public void setUp() {

        applicationDAO = Mockito.mock(ApplicationDAO.class);
        cryptoService = new CryptoService();
        mailService = Mockito.mock(MailService.class);
        calendarService = new OwnCalendarService();

        instance = new ApplicationServiceImpl(applicationDAO, cryptoService, mailService, calendarService);

        // create person that is needed for tests
        person = new Person();
        person.setLastName("Testperson");

        // create application that is needed for tests
        application = new Application();
        application.setPerson(person);

    }

    /** Test of getApplicationById method, of class ApplicationServiceImpl. */
    @Test
    public void testGetApplicationById() {

        instance.getApplicationById(1234);
        Mockito.verify(applicationDAO).findOne(1234);
    }

    /** Test of save method, of class ApplicationServiceImpl. */
    @Test
    public void testSave() {

        instance.save(application);
        Mockito.verify(applicationDAO).save(application);
    }

    /** Test of allow method, of class ApplicationServiceImpl. */
    @Test
    public void testAllow() throws NoSuchAlgorithmException {

        // set private key for boss
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());
        application.setApplicationDate(DateMidnight.now());
        application.setVacationType(VacationType.HOLIDAY);

        application.setStatus(ApplicationStatus.WAITING);

        instance.allow(application, person);

        Assert.assertEquals(ApplicationStatus.ALLOWED, application.getStatus());
        Assert.assertEquals(person, application.getBoss());
    }

    /** Test of reject method, of class ApplicationServiceImpl. */
    @Test
    public void testReject() {

        Person boss = new Person();

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setStatus(ApplicationStatus.WAITING);

        // supplemental applications

        List<Application> sApps = new ArrayList<Application>();

        Application sa1 = new Application();
        sa1.setStatus(ApplicationStatus.WAITING);
        sa1.setStartDate(startDate);
        sa1.setEndDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 31));

        Application sa2 = new Application();
        sa2.setStatus(ApplicationStatus.WAITING);
        sa2.setStartDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(endDate);

        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(applicationDAO.getSupplementalApplicationsForApplication(application.getId())).thenReturn(sApps);

        instance.reject(application, boss);

        Assert.assertEquals(ApplicationStatus.REJECTED, application.getStatus());
        Assert.assertEquals(ApplicationStatus.REJECTED, sApps.get(0).getStatus());
        Assert.assertEquals(ApplicationStatus.REJECTED, sApps.get(1).getStatus());

        Assert.assertNotNull(application.getBoss());
        Assert.assertEquals(boss, application.getBoss());
    }

    /** Test of cancel method, of class ApplicationServiceImpl. */
    @Test
    public void testCancel() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStatus(ApplicationStatus.WAITING);
        application.setStartDate(startDate);
        application.setEndDate(endDate);

        // supplemental applications

        List<Application> sApps = new ArrayList<Application>();

        Application sa1 = new Application();
        sa1.setStatus(ApplicationStatus.WAITING);
        sa1.setStartDate(startDate);
        sa1.setEndDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 31));

        Application sa2 = new Application();
        sa2.setStatus(ApplicationStatus.WAITING);
        sa2.setStartDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(endDate);

        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(applicationDAO.getSupplementalApplicationsForApplication(application.getId())).thenReturn(sApps);

        instance.cancel(application);

        Assert.assertEquals(ApplicationStatus.CANCELLED, application.getStatus());
        Assert.assertEquals(ApplicationStatus.CANCELLED, sApps.get(0).getStatus());
        Assert.assertEquals(ApplicationStatus.CANCELLED, sApps.get(1).getStatus());
    }

    /** Test of signApplicationByUser method, of class ApplicationServiceImpl. */
    @Test
    public void testSignApplicationByUser() throws Exception {

        // person needs some info: private key, last name
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(VacationType.SPECIALLEAVE);
        application.setApplicationDate(new DateMidnight(2011, 11, 1));

        // execute method
        instance.signApplicationByUser(application, person);

        // signature of person should be filled, signature of boss not
        Assert.assertNotNull(application.getSignaturePerson());
        Assert.assertEquals(null, application.getSignatureBoss());
    }

    /** Test of signApplicationByBoss method, of class ApplicationServiceImpl. */
    @Test
    public void testSignApplicationByBoss() throws Exception {

        // person needs some info: private key, last name
        person.setPrivateKey(cryptoService.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setApplicationDate(new DateMidnight(2011, 12, 21));

        // execute method
        instance.signApplicationByBoss(application, person);

        // signature of boss should be filled, signature of person not
        Assert.assertNotNull(application.getSignatureBoss());
        Assert.assertEquals(null, application.getSignaturePerson());
    }
}
