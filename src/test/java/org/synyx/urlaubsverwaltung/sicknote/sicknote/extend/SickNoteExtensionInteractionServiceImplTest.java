package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction.EXTENSION_ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class SickNoteExtensionInteractionServiceImplTest {

    @InjectMocks
    private SickNoteExtensionInteractionServiceImpl sut;

    @Mock
    private SickNoteExtensionServiceImpl sickNoteExtensionService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteInteractionService sickNoteInteractionService;
    @Mock
    private SickNoteCommentService sickNoteCommentService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Nested
    class SubmitSickNoteExtension {

        @Test
        void ensureSubmitSickNoteExtensionThrowsWhenSickNoteDoesNotExist() {

            final Person submitter = new Person();
            submitter.setId(1L);

            when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

            final LocalDate nextEndDate = LocalDate.now();
            assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("could not find sickNote with id=1");
        }

        @Test
        void ensureSubmitSickNoteExtensionThrowsWhenSubmitterIsNotOwnerOfSickNote() {

            final Person submitter = new Person();
            submitter.setId(1L);

            final Person otherPerson = new Person();
            otherPerson.setId(2L);

            final SickNote sickNote = SickNote.builder().person(otherPerson).id(1L).build();
            when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

            final LocalDate nextEndDate = LocalDate.now();
            assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("person id=1 is not allowed to submit sickNote extension for sickNote id=1");
        }

        @ParameterizedTest
        @EnumSource(value = SickNoteStatus.class, names = {"SUBMITTED", "ACTIVE"}, mode = EXCLUDE)
        void ensureSubmitSickNoteExtensionThrowsWhenSickNoteHasStatus(SickNoteStatus givenStatus) {

            final Person submitter = new Person();
            submitter.setId(1L);

            final SickNote sickNote = SickNote.builder().id(1L).person(submitter).status(givenStatus).build();
            when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

            final LocalDate nextEndDate = LocalDate.now();
            assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot submit sickNoteExtension for sickNote id=1 with status=%s".formatted(givenStatus));
        }

        @Test
        void ensureSubmitSickNoteExtension() {

            final LocalDate nextEndDate = LocalDate.now();

            final Person submitter = new Person();
            submitter.setId(1L);

            final SickNote sickNote = SickNote.builder().person(submitter).status(SickNoteStatus.ACTIVE).build();
            when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

            final SickNoteExtension extension = new SickNoteExtension(42L, 1L, nextEndDate, SUBMITTED, BigDecimal.ONE);
            when(sickNoteExtensionService.createSickNoteExtension(sickNote, nextEndDate)).thenReturn(extension);

            sut.submitSickNoteExtension(submitter, 1L, nextEndDate);

            verifyNoInteractions(sickNoteInteractionService);
        }

        @Test
        void ensureSubmitSickNoteExtensionDirectlyEditsTheSickNoteSinceStatusIsStillSubmitted() {

            final LocalDate nextEndDate = LocalDate.now();

            final Person submitter = new Person();
            submitter.setId(1L);

            final SickNote existingSubmittedSickNote = SickNote.builder()
                .id(1L)
                .person(submitter)
                .status(SickNoteStatus.SUBMITTED)
                .endDate(nextEndDate.minusDays(2))
                .build();
            when(sickNoteService.getById(1L)).thenReturn(Optional.of(existingSubmittedSickNote));

            sut.submitSickNoteExtension(submitter, 1L, nextEndDate);

            verifyNoInteractions(sickNoteExtensionService);

            final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
            verify(sickNoteInteractionService).update(captor.capture(), eq(submitter), eq(""));

            assertThat(captor.getValue()).satisfies(updatedSickNote -> {
                assertThat(updatedSickNote.getId()).isEqualTo(1L);
                assertThat(updatedSickNote.getPerson()).isEqualTo(submitter);
                assertThat(updatedSickNote.getStatus()).isEqualTo(SickNoteStatus.SUBMITTED);
                assertThat(updatedSickNote.getEndDate()).isEqualTo(nextEndDate);
            });
        }
    }

    @Nested
    class AcceptSubmittedExtension {

        @Test
        void ensureAcceptSubmittedExtensionThrowsWhenMaintainerIsNotAuthorized() {

            final Person maintainer = new Person();
            maintainer.setId(1L);
            maintainer.setPermissions(List.of(USER));

            assertThatThrownBy(() -> sut.acceptSubmittedExtension(maintainer, 1L, ""))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("person id=1 is not authorized to accept submitted sickNoteExtension");

            verifyNoInteractions(sickNoteCommentService);
            verifyNoInteractions(applicationEventPublisher);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "SICK_NOTE_EDIT"})
        void ensureAcceptSubmittedExtension(Role role) {

            final Person maintainer = new Person();
            maintainer.setId(1L);
            maintainer.setPermissions(List.of(USER, role));

            final SickNote sickNote = SickNote.builder().id(1L).build();
            when(sickNoteExtensionService.acceptSubmittedExtension(1L)).thenReturn(sickNote);

            final SickNote actual = sut.acceptSubmittedExtension(maintainer, 1L, "");
            assertThat(actual).isSameAs(sickNote);
        }

        @Test
        void ensureAcceptSubmittedExtensionCreatesSickNoteComment() {

            final Person maintainer = new Person();
            maintainer.setId(1L);
            maintainer.setPermissions(List.of(USER, OFFICE));

            final SickNote sickNote = SickNote.builder().id(1L).build();
            when(sickNoteExtensionService.acceptSubmittedExtension(1L)).thenReturn(sickNote);

            sut.acceptSubmittedExtension(maintainer, 1L, "awesome comment");

            verify(sickNoteCommentService).create(sickNote, EXTENSION_ACCEPTED, maintainer, "awesome comment");
        }

        @Test
        void ensureAcceptSubmittedExtensionPublishesApplicationEvent() {

            final Person maintainer = new Person();
            maintainer.setId(1L);
            maintainer.setPermissions(List.of(USER, OFFICE));

            final SickNote sickNote = SickNote.builder().id(1L).build();
            when(sickNoteExtensionService.acceptSubmittedExtension(1L)).thenReturn(sickNote);

            sut.acceptSubmittedExtension(maintainer, 1L, "");

            final ArgumentCaptor<SickNoteUpdatedEvent> captor = ArgumentCaptor.forClass(SickNoteUpdatedEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());

            assertThat(captor.getValue()).satisfies(event -> {
                assertThat(event.sickNote()).isSameAs(sickNote);
                assertThat(event.createdAt()).isNotNull();
                assertThat(event.id()).isNotNull();
            });
        }
    }
}
