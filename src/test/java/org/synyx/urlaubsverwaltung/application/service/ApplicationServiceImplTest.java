/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.security.CryptoService;

import java.security.NoSuchAlgorithmException;


/**
 * Unit test for serivce {@link ApplicationServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
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

        WorkingTimeService workingTimeService = Mockito.mock(WorkingTimeService.class);
        calendarService = new OwnCalendarService(new JollydayCalendar(), workingTimeService);

        instance = new ApplicationServiceImpl(applicationDAO, cryptoService, mailService, calendarService);

        // touch person that is needed for tests
        person = new Person();
        person.setLastName("Testperson");

        // touch application that is needed for tests
        application = new Application();
        application.setPerson(person);
    }


    /**
     * Test of getApplicationById method, of class ApplicationServiceImpl.
     */
    @Test
    public void testGetApplicationById() {

        instance.getApplicationById(1234);
        Mockito.verify(applicationDAO).findOne(1234);
    }


    /**
     * Test of save method, of class ApplicationServiceImpl.
     */
    @Test
    public void testSave() {

        instance.save(application);
        Mockito.verify(applicationDAO).save(application);
    }


    /**
     * Test of allow method, of class ApplicationServiceImpl.
     */
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


    /**
     * Test of reject method, of class ApplicationServiceImpl.
     */
    @Test
    public void testReject() {

        Person boss = new Person();

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setStatus(ApplicationStatus.WAITING);

        instance.reject(application, boss);

        Assert.assertEquals(ApplicationStatus.REJECTED, application.getStatus());

        Assert.assertNotNull(application.getBoss());
        Assert.assertEquals(boss, application.getBoss());
    }


    /**
     * Test of cancel method, of class ApplicationServiceImpl.
     */
    @Test
    public void testCancel() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.JANUARY, 5);

        application.setStatus(ApplicationStatus.WAITING);
        application.setStartDate(startDate);
        application.setEndDate(endDate);

        instance.cancel(application);

        Assert.assertEquals(ApplicationStatus.CANCELLED, application.getStatus());
    }


    /**
     * Test of signApplicationByUser method, of class ApplicationServiceImpl.
     */
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


    /**
     * Test of signApplicationByBoss method, of class ApplicationServiceImpl.
     */
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
