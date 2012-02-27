/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina
 */
public class VacationDaysServiceTest {

    private VacationDaysService instance;

    private ApplicationService appService = Mockito.mock(ApplicationService.class);
    private OwnCalendarService calendarService = new OwnCalendarService();

    private Person person;

    public VacationDaysServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {

        instance = new VacationDaysService(appService, calendarService);
        person = new Person();
    }


    @After
    public void tearDown() {
    }


    /** Test of getUsedVacationDaysOfPersonForYear method, of class VacationDaysService. */
    @Test
    public void testGetUsedVacationDaysOfPersonForYear() {

        int year = 2012;

        // expected to be used for calculation : 2 days
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 2));
        a1.setEndDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 4));
        a1.setDays(BigDecimal.valueOf(2));
        a1.setStatus(ApplicationStatus.WAITING);
        a1.setSupplementaryApplication(false);

        // expected to be used for calculation : 3 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(year, DateTimeConstants.APRIL, 3));
        a2.setEndDate(new DateMidnight(year, DateTimeConstants.APRIL, 6));
        a2.setDays(BigDecimal.valueOf(3));
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setSupplementaryApplication(false);

        // expected to be NOT used for calculation : 7 days - status is cancelled
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(year, DateTimeConstants.JUNE, 12));
        a3.setEndDate(new DateMidnight(year, DateTimeConstants.JUNE, 20));
        a3.setDays(BigDecimal.valueOf(7));
        a3.setStatus(ApplicationStatus.CANCELLED);
        a3.setSupplementaryApplication(false);

        // expected to be NOT used for calculation : 6 days - application spans December and January
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 21));
        a4.setEndDate(new DateMidnight(year + 1, DateTimeConstants.JANUARY, 3));
        a4.setDays(BigDecimal.valueOf(6)); // 4 days before 1st January
        a4.setStatus(ApplicationStatus.ALLOWED);
        a4.setSupplementaryApplication(false);

        List<Application> apps = new ArrayList<Application>();
        apps.add(a1);
        apps.add(a2);
        apps.add(a3);
        apps.add(a4);

        Mockito.when(appService.getApplicationsByPersonAndYear(person, year)).thenReturn(apps);

        // expected to be used for calculation : 4 days
        Application sa1 = new Application();
        sa1.setStartDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 21));
        sa1.setEndDate(new DateMidnight(year, DateTimeConstants.DECEMBER, 31));
        sa1.setDays(BigDecimal.valueOf(4));
        sa1.setStatus(ApplicationStatus.ALLOWED);
        sa1.setSupplementaryApplication(true);

        // expected to be NOT used for calculation : 2 days - status is rejected
        Application sa2 = new Application();
        sa2.setStartDate(new DateMidnight(year, DateTimeConstants.JANUARY, 1));
        sa2.setEndDate(new DateMidnight(year, DateTimeConstants.JANUARY, 3));
        sa2.setDays(BigDecimal.valueOf(2));
        sa2.setStatus(ApplicationStatus.REJECTED);
        sa2.setSupplementaryApplication(true);

        List<Application> sApps = new ArrayList<Application>();
        sApps.add(sa1);
        sApps.add(sa2);

        Mockito.when(appService.getSupplementalApplicationsByPersonAndYear(person, year)).thenReturn(sApps);

        // so it's ecpected that the calculation occurs following way:
        // a1 : +2
        // a2 : +3
        // ( a3 : 7 ) not used
        // ( a4 : 6 ) not used
        // sa1 : +4
        // ( sa2 : 2 ) not used
        // total number = 2 + 3 + 4 = 9 days

        BigDecimal returnValue = instance.getUsedVacationDaysOfPersonForYear(person, year);
        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9), returnValue);
    }


    /** Test of getUsedVacationDaysBeforeAprilOfPerson method, of class VacationDaysService. */
    @Test
    public void testGetUsedVacationDaysBeforeAprilOfPerson() {

        int year = 2012;

        // expected to be used for calculation : 2 days
        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 2));
        a1.setEndDate(new DateMidnight(year, DateTimeConstants.FEBRUARY, 4));
        a1.setDays(BigDecimal.valueOf(2));
        a1.setStatus(ApplicationStatus.WAITING);
        a1.setSupplementaryApplication(false);
        a1.setHowLong(DayLength.FULL);

        // expected to be used for calculation : 3 days
        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(year, DateTimeConstants.MARCH, 5));
        a2.setEndDate(new DateMidnight(year, DateTimeConstants.MARCH, 7));
        a2.setDays(BigDecimal.valueOf(3));
        a2.setStatus(ApplicationStatus.ALLOWED);
        a2.setSupplementaryApplication(false);
        a2.setHowLong(DayLength.FULL);

        // expected to be NOT used for calculation : 7 days - status is cancelled
        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(year, DateTimeConstants.JANUARY, 12));
        a3.setEndDate(new DateMidnight(year, DateTimeConstants.JANUARY, 20));
        a3.setDays(BigDecimal.valueOf(7));
        a3.setStatus(ApplicationStatus.CANCELLED);
        a3.setSupplementaryApplication(false);
        a3.setHowLong(DayLength.FULL);

        // expected to be used for calculation : special calculation - application spanning March and April
        // 5 days before April, 3 days after April
        // expected that only days before April are used for calculation: 5 days
        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(year, DateTimeConstants.MARCH, 26));
        a4.setEndDate(new DateMidnight(year + 1, DateTimeConstants.APRIL, 4));
        a4.setDays(BigDecimal.valueOf(5 + 3)); // 5 days before April, 3 days after April
        a4.setStatus(ApplicationStatus.ALLOWED);
        a4.setSupplementaryApplication(false);
        a4.setHowLong(DayLength.FULL);

        List<Application> apps = new ArrayList<Application>();
        apps.add(a1);
        apps.add(a2);
        apps.add(a3);
        apps.add(a4);

        Mockito.when(appService.getApplicationsBeforeAprilByPersonAndYear(person, year)).thenReturn(apps);

        // so it's ecpected that the calculation occurs following way:
        // a1 : +2
        // a2 : +3
        // ( a3 : 7 ) not used
        // ( a4 : 5 + 3 ) only 5 used
        // total number = 2 + 3 + 5 = 10 days

        BigDecimal returnValue = instance.getUsedVacationDaysBeforeAprilOfPerson(person, year);
        assertNotNull(returnValue);
        assertEquals(BigDecimal.TEN.setScale(2), returnValue);
    }
}
