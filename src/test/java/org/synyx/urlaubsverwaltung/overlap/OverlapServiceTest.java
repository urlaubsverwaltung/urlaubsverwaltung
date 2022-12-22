package org.synyx.urlaubsverwaltung.overlap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.anyApplication;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.NO_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@ExtendWith(MockitoExtension.class)
class OverlapServiceTest {

    private OverlapService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private SickNoteService sickNoteService;

    @BeforeEach
    void setup() {
        sut = new OverlapService(applicationService, sickNoteService);
    }

    @Test
    void ensureNoOverlappingIfOnlyInactiveApplicationsForLeaveInThePeriod() {

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        final LocalDate endDate = LocalDate.of(2012, JANUARY, 18);

        final Application cancelledApplication = new Application();
        cancelledApplication.setDayLength(FULL);
        cancelledApplication.setStartDate(startDate);
        cancelledApplication.setEndDate(endDate);
        cancelledApplication.setStatus(ApplicationStatus.CANCELLED);

        final Application rejectedApplication = new Application();
        rejectedApplication.setDayLength(MORNING);
        rejectedApplication.setStartDate(startDate);
        rejectedApplication.setEndDate(endDate);
        rejectedApplication.setStatus(ApplicationStatus.REJECTED);

        final Person person = new Person();

        final Application applicationToBeChecked = new Application();
        applicationToBeChecked.setPerson(person);
        applicationToBeChecked.setDayLength(FULL);
        applicationToBeChecked.setStartDate(startDate);
        applicationToBeChecked.setEndDate(endDate);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person))
            .thenReturn(asList(cancelledApplication, rejectedApplication));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToBeChecked);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }

    @Test
    void ensureNoOverlappingIfNoActiveApplicationsForLeaveInThePeriod() {

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        final LocalDate endDate = LocalDate.of(2012, JANUARY, 18);
        final Person person = new Person();

        // application for leave to check: 16.01. - 18.01.
        final Application applicationToCheck = new Application();
        applicationToCheck.setPerson(person);
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(startDate);
        applicationToCheck.setEndDate(endDate);


        when(applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person))
            .thenReturn(new ArrayList<>());

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }

    @Test
    void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfOtherApplicationsForLeave() {

        // first application for leave: 16.01. - 18.01.
        final Application waitingApplication = new Application();
        waitingApplication.setDayLength(FULL);
        waitingApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        waitingApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        waitingApplication.setStatus(WAITING);

        // second application for leave: 19.01. - 20.01.
        final Application allowedApplication = new Application();
        allowedApplication.setDayLength(FULL);
        allowedApplication.setStartDate(LocalDate.of(2012, JANUARY, 19));
        allowedApplication.setEndDate(LocalDate.of(2012, JANUARY, 20));
        allowedApplication.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(asList(waitingApplication, allowedApplication));

        // application for leave to check: 18.01. - 19.01.
        final Application applicationToCheck = anyApplication();
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 19));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }

    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application waitingApplication = new Application();
        waitingApplication.setDayLength(FULL);
        waitingApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        waitingApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        waitingApplication.setStatus(WAITING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(waitingApplication));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = anyApplication();
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 14));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 16));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(PARTLY_OVERLAPPING);
    }

    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyEndOfPeriodOfOtherApplicationsForLeave() {

        // application for leave: 16.01. - 18.01.
        Application allowedApplication = new Application();
        allowedApplication.setDayLength(FULL);
        allowedApplication.setStartDate(LocalDate.of(2012, JANUARY, 16));
        allowedApplication.setEndDate(LocalDate.of(2012, JANUARY, 18));
        allowedApplication.setStatus(ALLOWED);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class),
            any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(allowedApplication));

        // application for leave to check: 18.01. - 20.01.
        Application applicationToCheck = anyApplication();
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 20));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(PARTLY_OVERLAPPING);
    }


    @Test
    void ensureNoOverlappingIfOnlyInactiveSickNotesInThePeriod() {

        LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        LocalDate endDate = LocalDate.of(2012, JANUARY, 18);

        final SickNote inactiveSickNote = SickNote.builder()
            .dayLength(FULL)
            .startDate(startDate)
            .endDate(endDate)
            .status(SickNoteStatus.CANCELLED)
            .build();

        final Person person = new Person();

        // sick note to be checked: 16.01. - 18.01.
        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .startDate(LocalDate.of(2012, JANUARY, 16))
            .endDate(LocalDate.of(2012, JANUARY, 18))
            .build();

        when(sickNoteService.getByPersonAndPeriod(person, startDate, endDate)).thenReturn(singletonList(inactiveSickNote));

        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }

    @Test
    void ensureNoOverlappingIfNoActiveSickNotesInThePeriod() {

        final LocalDate startDate = LocalDate.of(2012, JANUARY, 16);
        final LocalDate endDate = LocalDate.of(2012, JANUARY, 18);
        final Person person = new Person();

        // sick note to be checked: 16.01. - 18.01.
        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .startDate(startDate)
            .endDate(endDate)
            .build();

        when(sickNoteService.getByPersonAndPeriod(person, startDate, endDate)).thenReturn(List.of());

        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }


    @Test
    void ensureFullyOverlappingIfTheApplicationForLeaveToCheckIsFullyInThePeriodOfASickNote() {

        // sick note: 16.01. - 19.01.
        final SickNote sickNote = SickNote.builder()
            .dayLength(FULL)
            .startDate(LocalDate.of(2012, JANUARY, 16))
            .endDate(LocalDate.of(2012, JANUARY, 19))
            .status(ACTIVE)
            .build();

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        // application for leave to check: 18.01. - 19.01.
        Application applicationToCheck = anyApplication();
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 18));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 19));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }


    @Test
    void ensurePartlyOverlappingIfTheApplicationForLeaveToCheckOverlapsOnlyStartOfPeriodOfASickNote() {

        // sick note: 16.01. - 18.01.
        final SickNote sickNote = SickNote.builder()
            .dayLength(FULL)
            .startDate(LocalDate.of(2012, JANUARY, 16))
            .endDate(LocalDate.of(2012, JANUARY, 18))
            .status(ACTIVE)
            .build();

        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        // application for leave to check: 14.01. - 16.01.
        Application applicationToCheck = anyApplication();
        applicationToCheck.setDayLength(FULL);
        applicationToCheck.setStartDate(LocalDate.of(2012, JANUARY, 14));
        applicationToCheck.setEndDate(LocalDate.of(2012, JANUARY, 16));

        final OverlapCase overlapCase = sut.checkOverlap(applicationToCheck);
        assertThat(overlapCase).isEqualTo(PARTLY_OVERLAPPING);
    }


    @Test
    void ensureSickNoteCanBeEditedAndNoOverlappingErrorOccurs() {

        // sick note: 16.03. - 16.03.
        final LocalDate sameDay = LocalDate.of(2015, MARCH, 16);

        final SickNote existentSickNote = SickNote.builder()
            .id(23L)
            .dayLength(FULL)
            .startDate(sameDay)
            .endDate(sameDay)
            .status(ACTIVE)
            .build();

        final Person person = new Person();

        // sick note should be edited to: 16.03. - 17.03.
        final LocalDate startDate = LocalDate.of(2015, MARCH, 16);
        final LocalDate endDate = LocalDate.of(2015, MARCH, 17);

        final SickNote sickNote = SickNote.builder()
            .id(23L)
            .person(person)
            .dayLength(FULL)
            .startDate(startDate)
            .endDate(endDate)
            .status(ACTIVE)
            .build();

        when(sickNoteService.getByPersonAndPeriod(person, startDate, endDate)).thenReturn(singletonList(existentSickNote));

        // edit sick note to: 16.03. - 17.03.
        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }


    @Test
    void ensureNoOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayButWithDifferentTimeOfDay() {

        final LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);
        final Person person = new Person();

        final Application morningVacation = new Application();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(WAITING);

        final Application noonVacation = new Application();
        noonVacation.setPerson(person);
        noonVacation.setDayLength(DayLength.NOON);
        noonVacation.setStartDate(vacationDate);
        noonVacation.setEndDate(vacationDate);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(vacationDate, vacationDate, person))
            .thenReturn(singletonList(morningVacation));

        final OverlapCase overlapCase = sut.checkOverlap(noonVacation);
        assertThat(overlapCase).isEqualTo(NO_OVERLAPPING);
    }


    @Test
    void ensureFullyOverlappingIfApplyingForTwoHalfDayVacationsOnTheSameDayAndTimeOfDay() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(WAITING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        Application otherMorningVacation = anyApplication();
        otherMorningVacation.setDayLength(MORNING);
        otherMorningVacation.setStartDate(vacationDate);
        otherMorningVacation.setEndDate(vacationDate);

        final OverlapCase overlapCase = sut.checkOverlap(otherMorningVacation);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }


    @Test
    void ensureFullyOverlappingIfApplyingForFullDayAlthoughThereIsAlreadyAHalfDayVacation() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application morningVacation = new Application();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(WAITING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(morningVacation));

        Application fullDayVacation = anyApplication();
        fullDayVacation.setDayLength(FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);

        final OverlapCase overlapCase = sut.checkOverlap(fullDayVacation);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }

    @Test
    void ensureFullyOverlappingIfApplyingForHalfDayAlthoughThereIsAlreadyAFullDayVacation() {

        LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        Application fullDayVacation = new Application();
        fullDayVacation.setDayLength(FULL);
        fullDayVacation.setStartDate(vacationDate);
        fullDayVacation.setEndDate(vacationDate);
        fullDayVacation.setStatus(WAITING);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(fullDayVacation));

        Application morningVacation = anyApplication();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);

        final OverlapCase overlapCase = sut.checkOverlap(morningVacation);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }


    @Test
    void ensureFullyOverlappingIfCreatingSickNoteOnADayWithHalfDayVacation() {

        final LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        final Application morningVacation = new Application();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ALLOWED);

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(person)))
            .thenReturn(singletonList(morningVacation));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .startDate(vacationDate)
            .endDate(vacationDate)
            .status(ACTIVE)
            .build();

        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }

    @Test
    void ensureOverlappingForTEMPORARY_ALLOWEDApplications() {

        final LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        final Application morningVacation = new Application();
        morningVacation.setDayLength(MORNING);
        morningVacation.setStartDate(vacationDate);
        morningVacation.setEndDate(vacationDate);
        morningVacation.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(person)))
            .thenReturn(singletonList(morningVacation));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .startDate(vacationDate)
            .endDate(vacationDate)
            .status(ACTIVE)
            .build();

        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }

    @Test
    void ensureOverlappingForCancellationRequestApplicationsWithSickNote() {

        final LocalDate vacationDate = LocalDate.of(2012, JANUARY, 16);

        final Application morningCancellationRequest = new Application();
        morningCancellationRequest.setDayLength(MORNING);
        morningCancellationRequest.setStartDate(vacationDate);
        morningCancellationRequest.setEndDate(vacationDate);
        morningCancellationRequest.setStatus(ALLOWED_CANCELLATION_REQUESTED);

        final Person person = new Person("username", "last name", "first name", "email@example.org");
        person.setId(1L);

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), eq(person)))
            .thenReturn(singletonList(morningCancellationRequest));

        final SickNote sickNote = SickNote.builder()
            .person(person)
            .dayLength(FULL)
            .startDate(vacationDate)
            .endDate(vacationDate)
            .status(ACTIVE)
            .build();

        final OverlapCase overlapCase = sut.checkOverlap(sickNote);
        assertThat(overlapCase).isEqualTo(FULLY_OVERLAPPING);
    }
}
