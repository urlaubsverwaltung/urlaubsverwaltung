package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

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
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteUpdatedEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private SickNoteExtensionService sickNoteExtensionService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteCommentService sickNoteCommentService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void ensureSubmitSickNoteExtensionThrowsWhenSickNoteDoesNotExist() {

        final Person submitter = new Person();
        submitter.setId(1L);

        when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

        final LocalDate nextEndDate = LocalDate.now();
        assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false))
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
        assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("person id=1 is not allowed to submit sickNote extension for sickNote id=1");
    }

    @Test
    void ensureSubmitSickNoteExtension() {

        final LocalDate nextEndDate = LocalDate.now();

        final Person submitter = new Person();
        submitter.setId(1L);

        final SickNote sickNote = SickNote.builder().person(submitter).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        final SickNoteExtension expected = new SickNoteExtension(42L, 1L, nextEndDate, false, SUBMITTED);
        when(sickNoteExtensionService.createSickNoteExtension(1L, nextEndDate, false)).thenReturn(expected);

        final SickNoteExtension actual = sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false);
        assertThat(actual).isSameAs(expected);
    }

    @Test
    void ensureAcceptSubmittedExtensionThrowsWhenMaintainerIsNotAuthorized() {

        final Person maintainer = new Person();
        maintainer.setId(1L);
        maintainer.setPermissions(List.of(USER));

        assertThatThrownBy(() -> sut.acceptSubmittedExtension(maintainer, 1L))
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

        final SickNote actual = sut.acceptSubmittedExtension(maintainer, 1L);
        assertThat(actual).isSameAs(sickNote);
    }

    @Test
    void ensureAcceptSubmittedExtensionCreatesSickNoteComment() {

        final Person maintainer = new Person();
        maintainer.setId(1L);
        maintainer.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder().id(1L).build();
        when(sickNoteExtensionService.acceptSubmittedExtension(1L)).thenReturn(sickNote);

        sut.acceptSubmittedExtension(maintainer, 1L);

        verify(sickNoteCommentService).create(sickNote, EXTENSION_ACCEPTED, maintainer);
    }

    @Test
    void ensureAcceptSubmittedExtensionPublishesApplicationEvent() {

        final Person maintainer = new Person();
        maintainer.setId(1L);
        maintainer.setPermissions(List.of(USER, OFFICE));

        final SickNote sickNote = SickNote.builder().id(1L).build();
        when(sickNoteExtensionService.acceptSubmittedExtension(1L)).thenReturn(sickNote);

        sut.acceptSubmittedExtension(maintainer, 1L);

        final ArgumentCaptor<SickNoteUpdatedEvent> captor = ArgumentCaptor.forClass(SickNoteUpdatedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue()).satisfies(event -> {
            assertThat(event.sickNote()).isSameAs(sickNote);
            assertThat(event.createdAt()).isNotNull();
            assertThat(event.id()).isNotNull();
        });
    }
}
