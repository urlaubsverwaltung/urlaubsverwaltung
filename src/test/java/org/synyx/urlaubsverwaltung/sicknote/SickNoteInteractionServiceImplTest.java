package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.DemoDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionServiceImpl}.
 */
class SickNoteInteractionServiceImplTest {

    private SickNoteInteractionService sickNoteInteractionService;

    private SickNoteService sickNoteService;
    private SickNoteCommentService commentService;
    private ApplicationInteractionService applicationInteractionService;
    private CalendarSyncService calendarSyncService;
    private AbsenceMappingService absenceMappingService;
    private SettingsService settingsService;

    private SickNote sickNote;
    private Person person;
    private AbsenceMapping absenceMapping;

    @BeforeEach
    void setUp() {

        sickNoteService = mock(SickNoteService.class);
        commentService = mock(SickNoteCommentService.class);
        applicationInteractionService = mock(ApplicationInteractionService.class);
        calendarSyncService = mock(CalendarSyncService.class);
        absenceMappingService = mock(AbsenceMappingService.class);
        settingsService = mock(SettingsService.class);

        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        absenceMapping = new AbsenceMapping(1, AbsenceType.VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        sickNoteInteractionService = new SickNoteInteractionServiceImpl(sickNoteService, commentService,
            applicationInteractionService, calendarSyncService, absenceMappingService, settingsService);

        sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setPerson(DemoDataCreator.createPerson());

        person = DemoDataCreator.createPerson();
    }


    @Test
    void ensureCreatedSickNoteIsPersisted() {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.CREATED, person, null);

        assertThat(createdSickNote).isNotNull();
        assertThat(createdSickNote.getLastEdited()).isNotNull();
        assertThat(createdSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureCreatedSickNoteHasComment() {
        String comment = "test comment";

        sickNoteInteractionService.create(sickNote, person, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.CREATED, person, comment);
    }


    @Test
    void ensureCreatingSickNoteAddsEventToCalendar() {

        sickNoteInteractionService.create(sickNote, person);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService)
            .create(eq(sickNote.getId()), eq(AbsenceType.SICKNOTE), anyString());
    }


    @Test
    void ensureUpdatedSickNoteIsPersisted() {

        SickNote updatedSickNote = sickNoteInteractionService.update(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.EDITED, person, null);

        assertThat(updatedSickNote).isNotNull();
        assertThat(updatedSickNote.getLastEdited()).isNotNull();
        assertThat(updatedSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureUpdatedSickHasComment() {
        final String comment = "test comment";

        sickNoteInteractionService.update(sickNote, person, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.EDITED, person, comment);
    }


    @Test
    void ensureUpdatingSickNoteUpdatesCalendarEvent() {

        sickNoteInteractionService.update(sickNote, person);

        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
    }


    @Test
    void ensureCancelledSickNoteIsPersisted() {

        SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.CANCELLED, person);

        assertThat(cancelledSickNote).isNotNull();
        assertThat(cancelledSickNote.getLastEdited()).isNotNull();
        assertThat(cancelledSickNote.getStatus()).isEqualTo(SickNoteStatus.CANCELLED);
    }


    @Test
    void ensureCancellingSickNoteDeletesCalendarEvent() {

        sickNoteInteractionService.cancel(sickNote, person);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
        verify(calendarSyncService).deleteAbsence(anyString());
        verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    void ensureConvertedSickNoteIsPersisted() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(DemoDataCreator.createPerson());

        SickNote convertedSickNote = sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        // assert sick note correctly updated

        verify(sickNoteService).save(sickNote);
        verify(commentService)
            .create(sickNote, SickNoteAction.CONVERTED_TO_VACATION, person);

        assertThat(convertedSickNote).isNotNull();
        assertThat(convertedSickNote.getLastEdited()).isNotNull();
        assertThat(convertedSickNote.getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);

        // assert application for leave correctly created
        verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, person);
    }


    @Test
    void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(person);

        sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).delete(eq(absenceMapping));
        verify(absenceMappingService).create(isNull(), eq(AbsenceType.VACATION), anyString());
    }
}
