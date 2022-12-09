package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
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

        when(sickNoteService.save(any(SickNote.class))).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        final SickNote createdSickNote = sut.create(sickNote, creator);
        assertThat(createdSickNote).isNotNull();
        assertThat(createdSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, null);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureCreatedSickNoteHasComment() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);
        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.create(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CREATED, creator, comment);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureCreatingSickNoteAddsEventToCalendar() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);
        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.create(sickNote, creator);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService).create(eq(sickNote.getId()), eq(SICKNOTE), anyString());

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureUpdatedSickHasComment() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.update(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, comment);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureCancelledSickNoteIsPersisted() {
        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final SickNote cancelledSickNote = sut.cancel(sickNote, creator);
        assertThat(cancelledSickNote).isNotNull();
        assertThat(cancelledSickNote.getStatus()).isEqualTo(SickNoteStatus.CANCELLED);

        verify(commentService).create(sickNote, SickNoteCommentAction.CANCELLED, creator);
        verify(sickNoteService).save(sickNote);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.CANCELLED);
    }

    @Test
    void ensureCancellingSickNoteDeletesCalendarEvent() {
        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        when(sickNoteService.save(any())).then(returnsFirstArg());

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

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        final SickNote convertedSickNote = sut.convert(sickNote, applicationForLeave, creator);
        assertThat(convertedSickNote).isNotNull();
        assertThat(convertedSickNote.getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);

        // assert sick note correctly updated
        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.CONVERTED_TO_VACATION, creator);

        // assert application for leave correctly created
        verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, creator);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);
    }

    @Test
    void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() {

        when(calendarSyncService.isRealProviderConfigured()).thenReturn(true);

        final AbsenceMapping absenceMapping = new AbsenceMapping(1, VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(SICKNOTE))).thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(creator);

        final SickNote sickNote = SickNote.builder()
            .id(42)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.convert(sickNote, applicationForLeave, creator);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(SICKNOTE));
        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).delete(absenceMapping);
        verify(absenceMappingService).create(isNull(), eq(VACATION), anyString());

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.CONVERTED_TO_VACATION);
    }

    @Test
    void ensureDeletionOfAllSickNotesAndAllCommentsOnPersonDeletedEvent() {
        final Person person = new Person();
        final int personId = 1;
        person.setId(personId);

        sut.deleteAll(new PersonDeletedEvent(person));

        final InOrder inOrder = inOrder(commentService, sickNoteService);
        inOrder.verify(commentService).deleteAllBySickNotePerson(person);
        inOrder.verify(commentService).deleteCommentAuthor(person);
        inOrder.verify(sickNoteService).deleteAllByPerson(person);
        inOrder.verify(sickNoteService).deleteSickNoteApplier(person);
    }

    @Test
    void ensureDeletionOfAbsenceMappingOnPersonDeletedEvent() {
        final Person person = new Person();
        person.setId(42);

        final SickNote sickNote = SickNote.builder().id(42).build();
        when(sickNoteService.deleteAllByPerson(person)).thenReturn(List.of(sickNote));

        final AbsenceMapping absenceMapping = new AbsenceMapping(42, SICKNOTE, "eventId");
        when(absenceMappingService.getAbsenceByIdAndType(42, SICKNOTE)).thenReturn(Optional.of(absenceMapping));

        sut.deleteAll(new PersonDeletedEvent(person));

        verify(absenceMappingService).getAbsenceByIdAndType(42, SICKNOTE);
        verify(absenceMappingService).delete(absenceMapping);
        verify(calendarSyncService).deleteAbsence("eventId");
    }
}
