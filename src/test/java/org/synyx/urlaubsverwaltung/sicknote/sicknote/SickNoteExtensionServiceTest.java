package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteExtensionStatus.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class SickNoteExtensionServiceTest {

    private SickNoteExtensionService sut;

    @Mock
    private SickNoteExtensionRepository repository;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteInteractionService sickNoteInteractionService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteExtensionService(repository, sickNoteService, sickNoteInteractionService, clock);
    }

    @Test
    void ensureSubmitSickNoteExtensionThrowsWhenSickNoteDoesNotExist() {

        final Person submitter = new Person();
        submitter.setId(1L);

        when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

        final LocalDate nextEndDate = LocalDate.now(clock);
        assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("could not find sickNote id=1");
    }

    @Test
    void ensureSubmitSickNoteExtensionThrowsWhenSubmitterIsNotOwnerOfSickNote() {

        final Person submitter = new Person();
        submitter.setId(1L);

        final Person otherPerson = new Person();
        otherPerson.setId(2L);

        final SickNote sickNote = SickNote.builder().person(otherPerson).id(1L).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        final LocalDate nextEndDate = LocalDate.now(clock);
        assertThatThrownBy(() -> sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("person id=1 is not allowed to submit sickNote extension for sickNote id=1");
    }

    @Test
    void ensureSubmitSickNoteExtension() {

        final Person submitter = new Person();
        submitter.setId(1L);

        final SickNote sickNote = SickNote.builder().person(submitter).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        when(repository.save(any(SickNoteExtensionEntity.class))).thenAnswer(invocation -> {
            final SickNoteExtensionEntity entity = invocation.getArgument(0);
            entity.setId(42L);
            return entity;
        });

        final LocalDate nextEndDate = LocalDate.now(clock);
        final SickNoteExtension actual = sut.submitSickNoteExtension(submitter, 1L, nextEndDate, false);

        assertThat(actual.id()).isEqualTo(42L);
        assertThat(actual.sickNoteId()).isEqualTo(1L);
        assertThat(actual.isAub()).isFalse();
        assertThat(actual.nextEndDate()).isEqualTo(nextEndDate);
        assertThat(actual.status()).isEqualTo(SUBMITTED);
    }
}
