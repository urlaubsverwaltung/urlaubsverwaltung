
package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;

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
        cancelledApplication.setDayLength(DayLength.FULL);
        cancelledApplication.setStartDate(startDate);
        cancelledApplication.setEndDate(endDate);
        cancelledApplication.setStatus(ApplicationStatus.CANCELLED);

        Application rejectedApplication = new Application();
        rejectedApplication.setDayLength(DayLength.MORNING);
        rejectedApplication.setStartDate(startDate);
        rejectedApplication.setEndDate(endDate);
        rejectedApplication.setStatus(ApplicationStatus.REJECTED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(cancelledApplication, rejectedApplication));

        Application applicationToBeChecked = new Application();
        applicationToBeChecked.setDayLength(DayLength.FULL);
        applicationToBeChecked.setStartDate(startDate);
        applicationToBeChecked.setEndDate(endDate);

        OverlapCase overlapCase = service.checkOverlap(applicationToBeChecked);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfNoActiveApplicationsForLeaveInThePeriod() {

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(new ArrayList<>());

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 18);

        // application for leave to check: 16.01. - 18.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(startDate);
        applicationToCheck.setEndDate(endDate);

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfOtherApplicationsForLeave() {

        // first application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setDayLength(DayLength.FULL);
        waitingApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        waitingApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        // second application for leave: 19.01. - 20.01.
        Application allowedApplication = new Application();
        allowedApplication.setDayLength(DayLength.FULL);
        allowedApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        allowedApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(waitingApplication, allowedApplication));

        // application for leave to check: 18.01. - 19.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        applicationToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setDayLength(DayLength.FULL);
        waitingApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        waitingApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(waitingApplication));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 14));
        applicationToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyEndOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application allowedApplication = new Application();
        allowedApplication.setDayLength(DayLength.FULL);
        allowedApplication.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        allowedApplication.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(allowedApplication));

        // application for leave to check: 18.01. - 20.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        applicationToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfOnlyInactiveSickNotesInThePeriod() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 18);

        SickNote inactiveSickNote = new SickNote();
        inactiveSickNote.setDayLength(DayLength.FULL);
        inactiveSickNote.setStartDate(startDate);
        inactiveSickNote.setEndDate(endDate);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Arrays.asList(inactiveSickNote));

        // sick note to be checked: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfNoActiveSickNotesInThePeriod() {

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(new ArrayList<SickNote>());

        // sick note to be checked: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfASickNote() {

        // sick note: 16.01. - 19.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Arrays.asList(sickNote));

        // application for leave to check: 18.01. - 19.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        applicationToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfASickNote() {

        // sick note: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        sickNote.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Arrays.asList(sickNote));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = new Application();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 14));
        applicationToCheck.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureSickNoteCanBeEditedAndNoOverlappingErrorOccurs() {

        // sick note: 16.03. - 16.03.
        SickNote existentSickNote = new SickNote();
        existentSickNote.setId(23);
        existentSickNote.setDayLength(DayLength.FULL);
        existentSickNote.setStartDate(new DateMidnight(2015, DateTimeConstants.MARCH, 16));
        existentSickNote.setEndDate(new DateMidnight(2015, DateTimeConstants.MARCH, 16));
        existentSickNote.setStatus(SickNoteStatus.ACTIVE);

        Mockito.when(sickNoteDAO.findByPersonAndPeriod(Mockito.any(Person.class), Mockito.any(Date.class),
                    Mockito.any(Date.class)))
            .thenReturn(Arrays.asList(existentSickNote));

        // sick note should be edited to: 16.03. - 17.03.
        SickNote sickNote = new SickNote();
        sickNote.setId(23);
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(new DateMidnight(2015, DateTimeConstants.MARCH, 16));
        sickNote.setEndDate(new DateMidnight(2015, DateTimeConstants.MARCH, 17));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        // edit sick note to: 16.03. - 17.03.
        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureNoOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayButWithDifferentTimeOfDay() {

        DateMidnight vacationDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(morningVacation));

        Application noonVacation = new Application();
        noonVacation.setDayLength(DayLength.NOON);
        noonVacation.setStartDate(vacationDate);
        noonVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(noonVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayAndTimeOfDay() {

        DateMidnight vacationDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(morningVacation));

        Application otherMorningVacation = new Application();
        otherMorningVacation.setDayLength(DayLength.MORNING);
        otherMorningVacation.setStartDate(vacationDate);
        otherMorningVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(otherMorningVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfApplyingForFullDayAlthoughThereIsAlreadyAHalfDayVacation() {

        DateMidnight vacationDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(morningVacation));

        Application fullDayVacation = new Application();
        fullDayVacation.setDayLength(DayLength.FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(fullDayVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }

    @Test
    public void ensureFullyOverlappingIfApplyingForHalfDayAlthoughThereIsAlreadyAFullDayVacation() {

        DateMidnight vacationDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);

        Application fullDayVacation = new Application();
        fullDayVacation.setDayLength(DayLength.FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);
        fullDayVacation.setStatus(ApplicationStatus.WAITING);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                Mockito.any(Date.class), Mockito.any(Person.class)))
                .thenReturn(Arrays.asList(fullDayVacation));

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(morningVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    public void ensureFullyOverlappingIfCreatingSickNoteOnADayWithHalfDayVacation() {

        DateMidnight vacationDate = new DateMidnight(2012, DateTimeConstants.JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.ALLOWED);

        Mockito.when(applicationDAO.getApplicationsForACertainTimeAndPerson(Mockito.any(Date.class),
                    Mockito.any(Date.class), Mockito.any(Person.class)))
            .thenReturn(Arrays.asList(morningVacation));

        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(vacationDate);
        sickNote.setEndDate(vacationDate);
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }
}
