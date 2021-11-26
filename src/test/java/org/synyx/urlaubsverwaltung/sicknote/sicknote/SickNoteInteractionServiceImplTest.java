package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

/**
 * Unit test for {@link SickNoteInteractionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class SickNoteInteractionServiceImplTest {

    private SickNoteInteractionServiceImpl sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteCommentService commentService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private CalendarSyncService calendarSyncService;
    @Mock
    private AbsenceMappingService absenceMappingService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new SickNoteInteractionServiceImpl(sickNoteService, commentService, applicationInteractionService, calendarSyncService,
            absenceMappingService, settingsService, Clock.systemUTC());
    }

    @Test
    void ensureCreatedSickNoteIsPersisted() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);
        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote createdSickNote = sut.create(sickNote, creator);
        assertThat(createdSickNote).isNotNull();
        assertThat(createdSickNote.getLastEdited()).isNotNull();
        assertThat(createdSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, null);
    }

    @Test
    void ensureCreatedSickNoteHasComment() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);
        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.create(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, comment);
    }

    @Test
    void ensureCreatingSickNoteAddsEventToCalendar() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);
        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.create(sickNote, creator);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService).create(eq(sickNote.getId()), eq(SICKNOTE), anyString());
    }

    @Test
    void ensureUpdatedSickNoteIsPersisted() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote updatedSickNote = sut.update(sickNote, creator);
        assertThat(updatedSickNote).isNotNull();
        assertThat(updatedSickNote.getLastEdited()).isNotNull();
        assertThat(updatedSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, null);
    }

    @Test
    void ensureUpdatedSickHasComment() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.update(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, comment);
    }

    @Test
    void ensureUpdatingSickNoteUpdatesCalendarEvent() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, AbsenceMappingType.VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceMappingType.SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.update(sickNote, creator);

        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(SICKNOTE));
    }

    @Test
    void ensureCancelledSickNoteIsPersisted() {
        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        final SickNote cancelledSickNote = sut.cancel(sickNote, creator);
        assertThat(cancelledSickNote).isNotNull();
        assertThat(cancelledSickNote.getLastEdited()).isNotNull();
        assertThat(cancelledSickNote.getStatus()).isEqualTo(SickNoteStatus.CANCELLED);

        verify(commentService).create(sickNote, SickNoteCommentAction.CANCELLED, creator);
        verify(sickNoteService).save(sickNote);
    }

    @Test
    void ensureCancellingSickNoteDeletesCalendarEvent() {
        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = getSickNote();

        sut.cancel(sickNote, creator);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(SICKNOTE));
        verify(calendarSyncService).deleteAbsence(anyString());
        verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }

    @Test
    void ensureConvertedSickNoteIsPersisted() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));

        final SickNote sickNote = getSickNote();

        final SickNote convertedSickNote = sut.convert(sickNote, applicationForLeave, creator);
        assertThat(convertedSickNote).isNotNull();
        assertThat(convertedSickNote.getLastEdited()).isNotNull();
        assertThat(convertedSickNote.getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);

        // assert sick note correctly updated
        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, creator);

        // assert application for leave correctly created
        verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, creator);
    }

    @Test
    void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(creator);

        final SickNote sickNote = getSickNote();

        sut.convert(sickNote, applicationForLeave, creator);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(SICKNOTE));
        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).delete(absenceMapping);
        verify(absenceMappingService).create(isNull(), eq(VACATION), anyString());
    }

    private SickNote getSickNote() {
        final SickNote sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));
        return sickNote;
    }
}
