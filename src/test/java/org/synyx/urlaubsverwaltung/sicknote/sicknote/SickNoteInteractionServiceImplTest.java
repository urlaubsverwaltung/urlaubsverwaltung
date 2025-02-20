package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SickNoteInteractionServiceImplTest {

    @InjectMocks
    private SickNoteInteractionServiceImpl sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteExtensionService sickNoteExtensionService;
    @Mock
    private SickNoteCommentService commentService;
    @Mock
    private ApplicationInteractionService applicationInteractionService;
    @Mock
    private SickNoteMailService sickNoteMailService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void ensureCreatedSickNoteIsPersisted() {

        when(sickNoteService.save(any(SickNote.class))).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
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

        final ArgumentCaptor<SickNoteCreatedEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final SickNoteCreatedEvent sickNoteCreatedEvent = eventCaptor.getValue();
        assertThat(sickNoteCreatedEvent.sickNote()).isEqualTo(createdSickNote);
        assertThat(sickNoteCreatedEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(sickNoteCreatedEvent.id()).isNotNull();
    }

    @Test
    void ensureCreatedSickNoteHasComment() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
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

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.create(sickNote, creator);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);
    }

    @Test
    void ensureCreatingSickNoteSendCreatedNotification() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        final String comment = "comment";

        sut.create(sickNote, creator, comment);

        verify(sickNoteMailService).sendCreatedToSickPerson(sickNote);
        verify(sickNoteMailService).sendCreatedOrAcceptedToColleagues(sickNote);
        verify(sickNoteMailService).sendSickNoteCreatedNotificationToOfficeAndResponsibleManagement(sickNote, comment);
    }

    @Test
    void ensureUpdatedSickHasComment() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.update(sickNote, creator, comment);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteCommentAction.EDITED, creator, comment);

        final ArgumentCaptor<SickNoteUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteUpdatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final SickNoteUpdatedEvent sickNoteUpdatedEvent = eventCaptor.getValue();
        assertThat(sickNoteUpdatedEvent.sickNote()).isEqualTo(sickNote);
        assertThat(sickNoteUpdatedEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(sickNoteUpdatedEvent.id()).isNotNull();

        verify(sickNoteMailService).sendEditedToSickPerson(sickNote);
    }

    @Test
    void ensureStatusIsPreservedInUpdatedSickNote() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final String comment = "test comment";
        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .status(SickNoteStatus.SUBMITTED)
            .build();

        sut.update(sickNote, creator, comment);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);
    }


    @Test
    void ensureCancelledSickNoteIsPersisted() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final SickNote cancelledSickNote = sut.cancel(sickNote, creator, "comment");
        assertThat(cancelledSickNote).isNotNull();
        assertThat(cancelledSickNote.getStatus()).isEqualTo(SickNoteStatus.CANCELLED);

        verify(commentService).create(sickNote, SickNoteCommentAction.CANCELLED, creator, "comment");
        verify(sickNoteService).save(sickNote);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.CANCELLED);

        final ArgumentCaptor<SickNoteCancelledEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteCancelledEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final SickNoteCancelledEvent sickNoteCancelledEvent = eventCaptor.getValue();
        assertThat(sickNoteCancelledEvent.sickNote()).isEqualTo(cancelledSickNote);
        assertThat(sickNoteCancelledEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(sickNoteCancelledEvent.id()).isNotNull();
    }

    @Test
    void ensureCancellingSickNoteSendCancelNotification() {

        when(sickNoteService.save(any())).then(returnsFirstArg());

        final Person canceller = new Person("canceller", "Senior", "Canceller", "canceller@example.org");

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        sut.cancel(sickNote, canceller, "comment");

        verify(sickNoteMailService).sendCancelledToSickPerson(sickNote);
        verify(sickNoteMailService).sendCancelToColleagues(sickNote);
    }

    @Nested
    class Convert {

        @Test
        void ensureConvertedSickNoteIsPersisted() {

            when(sickNoteService.save(any())).then(returnsFirstArg());

            final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");

            final Application applicationForLeave = new Application();
            applicationForLeave.setStartDate(LocalDate.now(UTC));
            applicationForLeave.setEndDate(LocalDate.now(UTC));
            applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
            applicationForLeave.setDayLength(DayLength.FULL);
            applicationForLeave.setPerson(new Person("muster", "Muster", "Marlene", "muster@example.org"));

            final SickNote sickNote = SickNote.builder()
                .id(42L)
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

            final ArgumentCaptor<SickNoteToApplicationConvertedEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteToApplicationConvertedEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            final SickNoteToApplicationConvertedEvent sickNoteToApplicationConvertedEvent = eventCaptor.getValue();
            assertThat(sickNoteToApplicationConvertedEvent.sickNote()).isEqualTo(convertedSickNote);
            assertThat(sickNoteToApplicationConvertedEvent.application()).isEqualTo(applicationForLeave);
            assertThat(sickNoteToApplicationConvertedEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(sickNoteToApplicationConvertedEvent.id()).isNotNull();
        }

        @Test
        void ensureConvertUpdatesExtensions() {

            final Person person = new Person();
            final SickNote sickNote = SickNote.builder().build();
            final Application application = new Application();

            final SickNote savedSickNote = SickNote.builder().build();
            when(sickNoteService.save(any(SickNote.class))).thenReturn(savedSickNote);

            sut.convert(sickNote, application, person);

            final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
            verify(sickNoteExtensionService).updateExtensionsForConvertedSickNote(captor.capture());

            assertThat(captor.getValue()).isSameAs(savedSickNote);
        }
    }

    @Test
    void ensureDeletionOfAllSickNotesAndAllCommentsOnPersonDeletedEvent() {
        final Person person = new Person();
        final long personId = 1;
        person.setId(personId);

        sut.deleteAll(new PersonDeletedEvent(person));

        final InOrder inOrder = inOrder(commentService, sickNoteService);
        inOrder.verify(commentService).deleteAllBySickNotePerson(person);
        inOrder.verify(commentService).deleteCommentAuthor(person);
        inOrder.verify(sickNoteService).deleteAllByPerson(person);
        inOrder.verify(sickNoteService).deleteSickNoteApplier(person);
    }

    @Test
    void ensureSickNoteDeletedEventsArePublishedWhenPersonIsDeleted() {
        final Person person = new Person();
        person.setId(42L);

        final SickNote sickNote = SickNote.builder().id(42L).build();
        when(sickNoteService.deleteAllByPerson(person)).thenReturn(List.of(sickNote));

        sut.deleteAll(new PersonDeletedEvent(person));

        final ArgumentCaptor<SickNoteDeletedEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteDeletedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final SickNoteDeletedEvent sickNoteDeletedEvent = eventCaptor.getValue();
        assertThat(sickNoteDeletedEvent.sickNote()).isEqualTo(sickNote);
        assertThat(sickNoteDeletedEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(sickNoteDeletedEvent.id()).isNotNull();
    }

    @Test
    void submit() {
        when(sickNoteService.save(any(SickNote.class))).then(returnsFirstArg());

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .build();

        final Person creator = new Person("creator", "Senior", "Creator", "creator@example.org");
        final String comment = "comment";

        final SickNote submittedSickNote = sut.submit(sickNote, creator, comment);
        assertThat(submittedSickNote).isNotNull();
        assertThat(submittedSickNote.getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);

        verify(commentService).create(sickNote, SickNoteCommentAction.SUBMITTED, creator, comment);

        verify(sickNoteMailService).sendSickNoteSubmittedNotificationToSickPerson(sickNote);
        verify(sickNoteMailService).sendSickNoteSubmittedNotificationToOfficeAndResponsibleManagement(sickNote);
    }

    @Test
    void accept() {
        when(sickNoteService.save(any(SickNote.class))).then(returnsFirstArg());

        final SickNote sickNote = SickNote.builder()
            .id(42L)
            .startDate(LocalDate.now(UTC))
            .endDate(LocalDate.now(UTC))
            .dayLength(DayLength.FULL)
            .person(new Person("muster", "Muster", "Marlene", "muster@example.org"))
            .status(SickNoteStatus.ACTIVE)
            .build();

        final Person maintainer = new Person("maintainer", "Senior", "Maintainer", "maintainer@example.org");

        final SickNote acceptedSickNote = sut.accept(sickNote, maintainer, "comment");
        assertThat(acceptedSickNote).isNotNull();
        assertThat(acceptedSickNote.getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SickNoteStatus.ACTIVE);

        verify(commentService).create(sickNote, SickNoteCommentAction.ACCEPTED, maintainer, "comment");

        verify(sickNoteMailService).sendSickNoteAcceptedNotificationToSickPerson(sickNote, maintainer);
        verify(sickNoteMailService).sendSickNoteAcceptedNotificationToOfficeAndResponsibleManagement(sickNote, maintainer);

        final ArgumentCaptor<SickNoteAcceptedEvent> eventCaptor = ArgumentCaptor.forClass(SickNoteAcceptedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        final SickNoteAcceptedEvent sickNoteCreatedEvent = eventCaptor.getValue();
        assertThat(sickNoteCreatedEvent.sickNote()).isEqualTo(acceptedSickNote);
        assertThat(sickNoteCreatedEvent.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(sickNoteCreatedEvent.id()).isNotNull();
    }
}
