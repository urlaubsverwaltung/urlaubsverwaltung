package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link OverlapService}.
 */
class OverlapServiceTest {

    private OverlapService service;
    private ApplicationRepository applicationRepository;
    private SickNoteService sickNoteService;

    @BeforeEach
    void setup() {

        applicationRepository = mock(ApplicationRepository.class);
        sickNoteService = mock(SickNoteService.class);
        service = new OverlapService(applicationRepository, sickNoteService);
    }


    @Test
    void ensureNoOverlappingIfOnlyInactiveApplicationsForLeaveInThePeriod() {

        LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        LocalDate endDate = LocalDate.of(2012, JANUARY, 18);

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

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
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
    void ensureNoOverlappingIfNoActiveApplicationsForLeaveInThePeriod() {

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(new ArrayList<>());

        LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        LocalDate endDate = LocalDate.of(2012, JANUARY, 18);

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
    void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfOtherApplicationsForLeave() {

        // first application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setDayLength(DayLength.FULL);
        waitingApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        waitingApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        // second application for leave: 19.01. - 20.01.
        Application allowedApplication = new Application();
        allowedApplication.setDayLength(DayLength.FULL);
        allowedApplication.setStartDate(LocalDate.of(2012, JANUARY, 19));
        allowedApplication.setEndDate(LocalDate.of(2012, JANUARY, 20));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(Arrays.asList(waitingApplication, allowedApplication));

        // application for leave to check: 18.01. - 19.01.
        Application applicationToCheck = DemoDataCreator.anyApplication();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 19));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setDayLength(DayLength.FULL);
        waitingApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        waitingApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        waitingApplication.setStatus(ApplicationStatus.WAITING);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(waitingApplication));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = DemoDataCreator.anyApplication();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 14));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 16));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyEndOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application allowedApplication = new Application();
        allowedApplication.setDayLength(DayLength.FULL);
        allowedApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        allowedApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        allowedApplication.setStatus(ApplicationStatus.ALLOWED);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(allowedApplication));

        // application for leave to check: 18.01. - 20.01.
        Application applicationToCheck = DemoDataCreator.anyApplication();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 20));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureNoOverlappingIfOnlyInactiveSickNotesInThePeriod() {

        LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        LocalDate endDate = LocalDate.of(2012, JANUARY, 18);

        SickNote inactiveSickNote = new SickNote();
        inactiveSickNote.setDayLength(DayLength.FULL);
        inactiveSickNote.setStartDate(startDate);
        inactiveSickNote.setEndDate(endDate);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class),
            any(LocalDate.class)))
            .thenReturn(singletonList(inactiveSickNote));

        // sick note to be checked: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(LocalDate.of(2012, JANUARY, 16));
        sickNote.setEndDate(LocalDate.of(2012, JANUARY, 18));

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureNoOverlappingIfNoActiveSickNotesInThePeriod() {

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(new ArrayList<>());

        // sick note to be checked: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(LocalDate.of(2012, JANUARY, 16));
        sickNote.setEndDate(LocalDate.of(2012, JANUARY, 18));

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfASickNote() {

        // sick note: 16.01. - 19.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(LocalDate.of(2012, JANUARY, 16));
        sickNote.setEndDate(LocalDate.of(2012, JANUARY, 19));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        // application for leave to check: 18.01. - 19.01.
        Application applicationToCheck = DemoDataCreator.anyApplication();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 19));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfASickNote() {

        // sick note: 16.01. - 18.01.
        SickNote sickNote = new SickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(LocalDate.of(2012, JANUARY, 16));
        sickNote.setEndDate(LocalDate.of(2012, JANUARY, 18));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = DemoDataCreator.anyApplication();
        applicationToCheck.setDayLength(DayLength.FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 14));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 16));

        OverlapCase overlapCase = service.checkOverlap(applicationToCheck);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.PARTLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureSickNoteCanBeEditedAndNoOverlappingErrorOccurs() {

        // sick note: 16.03. - 16.03.
        SickNote existentSickNote = new SickNote();
        existentSickNote.setId(23);
        existentSickNote.setDayLength(DayLength.FULL);
        existentSickNote.setStartDate(LocalDate.of(2015, MARCH, 16));
        existentSickNote.setEndDate(LocalDate.of(2015, MARCH, 16));
        existentSickNote.setStatus(SickNoteStatus.ACTIVE);

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(existentSickNote));

        // sick note should be edited to: 16.03. - 17.03.
        SickNote sickNote = new SickNote();
        sickNote.setId(23);
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(LocalDate.of(2015, MARCH, 16));
        sickNote.setEndDate(LocalDate.of(2015, MARCH, 17));
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        // edit sick note to: 16.03. - 17.03.
        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureNoOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayButWithDifferentTimeOfDay() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        Application noonVacation = new Application();
        noonVacation.setDayLength(DayLength.NOON);
        noonVacation.setStartDate(vacationDate);
        noonVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(noonVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.NO_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureFullyOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayAndTimeOfDay() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        Application otherMorningVacation = DemoDataCreator.anyApplication();
        otherMorningVacation.setDayLength(DayLength.MORNING);
        otherMorningVacation.setStartDate(vacationDate);
        otherMorningVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(otherMorningVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureFullyOverlappingIfApplyingForFullDayAlthoughThereIsAlreadyAHalfDayVacation() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.WAITING);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        Application fullDayVacation = DemoDataCreator.anyApplication();
        fullDayVacation.setDayLength(DayLength.FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(fullDayVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }

    @Test
    void ensureFullyOverlappingIfApplyingForHalfDayAlthoughThereIsAlreadyAFullDayVacation() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application fullDayVacation = new Application();
        fullDayVacation.setDayLength(DayLength.FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);
        fullDayVacation.setStatus(ApplicationStatus.WAITING);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(fullDayVacation));

        Application morningVacation = DemoDataCreator.anyApplication();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);

        OverlapCase overlapCase = service.checkOverlap(morningVacation);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }


    @Test
    void ensureFullyOverlappingIfCreatingSickNoteOnADayWithHalfDayVacation() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.ALLOWED);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        SickNote sickNote = DemoDataCreator.anySickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(vacationDate);
        sickNote.setEndDate(vacationDate);
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }

    @Test
    void ensureOverlappingForTEMPORARY_ALLOWEDApplications() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(DayLength.MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);

        when(applicationRepository.getApplicationsForACertainTimeAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        SickNote sickNote = DemoDataCreator.anySickNote();
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setStartDate(vacationDate);
        sickNote.setEndDate(vacationDate);
        sickNote.setStatus(SickNoteStatus.ACTIVE);

        OverlapCase overlapCase = service.checkOverlap(sickNote);

        Assert.assertNotNull("Should not be null", overlapCase);
        Assert.assertEquals("Wrong overlap case", OverlapCase.FULLY_OVERLAPPING, overlapCase);
    }
}
