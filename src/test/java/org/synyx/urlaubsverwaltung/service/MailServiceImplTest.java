/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import java.util.List;
import org.junit.After;

import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  aljona
 */
@Ignore
public class MailServiceImplTest {

    public MailServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }

    /**
     * Test of sendDecayNotification method, of class MailServiceImpl.
     */
    @Test
    public void testSendDecayNotification() {
        System.out.println("sendDecayNotification");
        List<Person> persons = null;
        MailServiceImpl instance = null;
        instance.sendDecayNotification(persons);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendNewRequestsNotification method, of class MailServiceImpl.
     */
    @Test
    public void testSendNewRequestsNotification() {
        System.out.println("sendNewRequestsNotification");
        List<Person> persons = null;
        List<Antrag> requests = null;
        MailServiceImpl instance = null;
        instance.sendNewRequestsNotification(persons, requests);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendApprovedNotification method, of class MailServiceImpl.
     */
    @Test
    public void testSendApprovedNotification() {
        System.out.println("sendApprovedNotification");
        Person person = null;
        Antrag request = null;
        MailServiceImpl instance = null;
        instance.sendApprovedNotification(person, request);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendDeclinedNotification method, of class MailServiceImpl.
     */
    @Test
    public void testSendDeclinedNotification() {
        System.out.println("sendDeclinedNotification");
        Antrag request = null;
        MailServiceImpl instance = null;
        instance.sendDeclinedNotification(request);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendConfirmation method, of class MailServiceImpl.
     */
    @Test
    public void testSendConfirmation() {
        System.out.println("sendConfirmation");
        Antrag request = null;
        MailServiceImpl instance = null;
        instance.sendConfirmation(request);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendBalance method, of class MailServiceImpl.
     */
    @Test
    public void testSendBalance() {
        System.out.println("sendBalance");
        Object balanceObject = null;
        MailServiceImpl instance = null;
        instance.sendBalance(balanceObject);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendWeeklyVacationForecast method, of class MailServiceImpl.
     */
    @Test
    public void testSendWeeklyVacationForecast() {
        System.out.println("sendWeeklyVacationForecast");
        List<Person> urlauber = null;
        MailServiceImpl instance = null;
        instance.sendWeeklyVacationForecast(urlauber);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendCanceledNotification method, of class MailServiceImpl.
     */
    @Test
    public void testSendCanceledNotification() {
        System.out.println("sendCanceledNotification");
        Antrag request = null;
        String emailAddress = "";
        MailServiceImpl instance = null;
        instance.sendCanceledNotification(request, emailAddress);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
