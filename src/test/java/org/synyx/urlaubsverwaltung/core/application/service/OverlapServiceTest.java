
package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.OverlapCase;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Unit test for {@link OverlapService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OverlapServiceTest {

    private OverlapService service;
    private ApplicationDAO applicationDAO;
    private SickNoteDAO sickNoteDAO;

    @Before
    public void setup() {

        applicationDAO = Mockito.mock(ApplicationDAO.class);
        sickNoteDAO = Mockito.mock(SickNoteDAO.class);
        service = new OverlapService(applicationDAO, sickNoteDAO);
    }


    @Test
    public void ensureNoOverlappingIfOnlyInactiveApplicationsForLeaveInThePeriod() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 18);

        Application cancelledApplication = new Application();
        cancelledApplication.setHowLong(DayLength.FULL);
        cancelledApplication.setStartDate(startDate);
        cancelledApplication.setEndDate(endDate);
        cancelledApplication.setStatus(ApplicationStatus.CANCELLED);

        Application rejectedApplication = new Application();
        rejectedApplication.setHowLong(DayLength.MORNING);
        rejectedApplication.setStartDate(startDate);
        rejectedApplication.setEndDate(endDate);
        rejectedApplication.setStatus(ApplicationStatus.REJECTED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(cancelledApplication,
                rejectedApplication));

        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class), startDate, endDate);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfNoActiveApplicationsForLeaveInThePeriod() {

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(new ArrayList<Application>());

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 18);

        // application for leave to check: 16.01. - 18.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setHowLong(DayLength.FULL);
        applicationToCheck.setStartDate(startDate);
        applicationToCheck.setEndDate(endDate);

        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class), startDate, endDate);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfOtherApplicationsForLeave() {

        // first application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setHowLong(DayLength.FULL);
        waitingApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        waitingApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        // second application for leave: 19.01. - 20.01.
        Application allowedApplication = new Application();
        allowedApplication.setHowLong(DayLength.FULL);
        allowedApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        allowedApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(waitingApplication,
                allowedApplication));

        // application for leave to check: 18.01. - 19.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 18),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setHowLong(DayLength.FULL);
        waitingApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        waitingApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(waitingApplication));

        // application for leave to check: 14.01. - 16.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 14),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 16));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyEndOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application allowedApplication = new Application();
        allowedApplication.setHowLong(DayLength.FULL);
        allowedApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        allowedApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class))).thenReturn(Arrays.asList(allowedApplication));

        // application for leave to check: 18.01. - 20.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 18),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfOnlyInactiveSickNotesInThePeriod() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 18);

        SickNote inactiveSickNote = new SickNote();
        inactiveSickNote.setStartDate(startDate);
        inactiveSickNote.setEndDate(endDate);
        inactiveSickNote.setActive(false);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                Mockito.any(Date.class))).thenReturn(Arrays.asList(inactiveSickNote));

        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class), startDate, endDate);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfNoActiveSickNotesInThePeriod() {

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                Mockito.any(Date.class))).thenReturn(new ArrayList<SickNote>());

        // application for leave to check: 16.01. - 18.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 16),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfASickNote() {

        // sick note: 16.01. - 19.01.
        SickNote sickNote = new SickNote();
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        sickNote.setActive(true);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                Mockito.any(Date.class))).thenReturn(Arrays.asList(sickNote));

        // application for leave to check: 18.01. - 19.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 18),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfASickNote() {

        // sick note: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        sickNote.setActive(true);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                Mockito.any(Date.class))).thenReturn(Arrays.asList(sickNote));

        // application for leave to check: 14.01. - 16.01.
        OverlapCase overlapCase = service.checkOverlap(Mockito.mock(Person.class),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 14),
                new DateMidnight(2012, DateTimeConstants.JANUARY, 16));

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }
}
