package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.ACCEPTED;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class SickNoteExtensionServiceImplTest {

    private SickNoteExtensionService sut;

    @Mock
    private SickNoteExtensionRepository repository;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private SickNoteExtensionPreviewService sickNoteExtensionPreviewService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteExtensionService(repository, sickNoteService, sickNoteExtensionPreviewService, clock);
    }

    @Test
    void ensureCreateSickNoteExtension() {

        final Person submitter = new Person();
        submitter.setId(1L);

        when(repository.save(any(SickNoteExtensionEntity.class))).thenAnswer(invocation -> {
            final SickNoteExtensionEntity entity = invocation.getArgument(0);
            entity.setId(42L);
            return entity;
        });

        final LocalDate nextEndDate = LocalDate.now();
        final SickNoteExtension actual = sut.createSickNoteExtension(1L, nextEndDate, false);

        assertThat(actual.id()).isEqualTo(42L);
        assertThat(actual.sickNoteId()).isEqualTo(1L);
        assertThat(actual.isAub()).isFalse();
        assertThat(actual.nextEndDate()).isEqualTo(nextEndDate);
        assertThat(actual.status()).isEqualTo(SUBMITTED);
    }

    @Test
    void ensureAcceptSubmittedExtensionThrowsWhenSickNoteDoesNotExist() {

        when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.acceptSubmittedExtension(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("could not find sickNote with id=1");
    }

    @Test
    void ensureAcceptSubmittedExtensionThrowsWhenExtensionDoesNotExist() {

        when(sickNoteService.getById(1L)).thenReturn(Optional.of(SickNote.builder().id(1L).build()));
        when(sickNoteExtensionPreviewService.findExtensionPreviewOfSickNote(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.acceptSubmittedExtension(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("could not find extension of sickNote id=1");
    }

    @Test
    void ensureAcceptSubmittedExtension() {

        final LocalDate now = LocalDate.now(clock);
        final SickNote sickNote = SickNote.builder().id(1L).startDate(now).endDate(now).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        when(sickNoteService.save(any(SickNote.class))).thenAnswer(returnsFirstArg());

        final LocalDate nextEndDate = now.plusDays(2);
        final BigDecimal workingDays = BigDecimal.valueOf(2L);
        final SickNoteExtensionPreview preview = new SickNoteExtensionPreview(1L, now, nextEndDate, false, workingDays);
        when(sickNoteExtensionPreviewService.findExtensionPreviewOfSickNote(1L)).thenReturn(Optional.of(preview));

        final SickNote actual = sut.acceptSubmittedExtension(1L);

        // sickNoteService.save is mocked above accordingly
        assertThat(actual).satisfies(updatedSickNote -> {
            assertThat(updatedSickNote.getId()).isEqualTo(1L);
            assertThat(updatedSickNote.getEndDate()).isEqualTo(nextEndDate);
        });

        final ArgumentCaptor<SickNote> captor = ArgumentCaptor.forClass(SickNote.class);
        verify(sickNoteService).save(captor.capture());
        assertThat(captor.getValue()).satisfies(updatedSickNote -> {
            assertThat(updatedSickNote.getEndDate()).isEqualTo(nextEndDate);
        });
    }

    @Test
    void ensureAcceptSubmittedExtensionUpdatesExtensionStatusToAccepted() {

        final LocalDate now = LocalDate.now(clock);
        final SickNote sickNote = SickNote.builder().id(1L).startDate(now).endDate(now).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        when(sickNoteService.save(any(SickNote.class))).thenAnswer(returnsFirstArg());

        final LocalDate nextEndDate = now.plusDays(2);
        final BigDecimal workingDays = BigDecimal.valueOf(2L);
        final SickNoteExtensionPreview preview = new SickNoteExtensionPreview(1L, now, nextEndDate, false, workingDays);
        when(sickNoteExtensionPreviewService.findExtensionPreviewOfSickNote(1L)).thenReturn(Optional.of(preview));

        final SickNoteExtensionEntity currentExtension = new SickNoteExtensionEntity();
        currentExtension.setId(2L);
        currentExtension.setStatus(SUBMITTED);

        final SickNoteExtensionEntity previousExtension = new SickNoteExtensionEntity();
        previousExtension.setId(1L);
        previousExtension.setStatus(SUBMITTED);

        when(repository.findAllBySickNoteIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(currentExtension, previousExtension));

        sut.acceptSubmittedExtension(1L);

        final ArgumentCaptor<SickNoteExtensionEntity> captor = ArgumentCaptor.forClass(SickNoteExtensionEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getAllValues())
            // only the current extension is touched
            // TODO should we touch all extensions?
            .satisfiesExactly(
                extension -> {
                    assertThat(extension.getId()).isEqualTo(2L);
                    assertThat(extension.getStatus()).isEqualTo(ACCEPTED);
                }
            );
    }
}
