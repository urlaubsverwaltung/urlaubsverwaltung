package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SickNoteExtensionHistoryServiceImplTest {

    private final SickNoteExtensionRepository sickNoteExtensionRepository = Mockito.mock(SickNoteExtensionRepository.class);
    private final SickNoteExtensionHistoryServiceImpl sickNoteExtensionHistoryService = new SickNoteExtensionHistoryServiceImpl(sickNoteExtensionRepository);

    @Test
    void getSickNoteExtensionHistory_returnsHistoryForGivenSickNoteId() {
        Long sickNoteId = 100L;
        Instant now = Instant.now();


        SickNoteExtensionEntity sickNoteExtensionEntity = new SickNoteExtensionEntity();
        sickNoteExtensionEntity.setId(1L);
        sickNoteExtensionEntity.setCreatedAt(now);
        sickNoteExtensionEntity.setSickNoteId(sickNoteId);
        sickNoteExtensionEntity.setAub(false);
        sickNoteExtensionEntity.setStatus(SickNoteExtensionStatus.ACCEPTED);

        when(sickNoteExtensionRepository.findAllBySickNoteId(sickNoteId)).thenReturn(List.of(sickNoteExtensionEntity));

        List<SickNoteExtensionHistory> result = sickNoteExtensionHistoryService.getSickNoteExtensionHistory(sickNoteId);

        assertThat(result).hasSize(1);

        SickNoteExtensionHistory sickNoteExtensionHistory = result.getFirst();
        assertThat(sickNoteExtensionHistory.createdAt()).isEqualTo(sickNoteExtensionEntity.getCreatedAt());
        assertThat(sickNoteExtensionHistory.newEndDate()).isEqualTo(sickNoteExtensionEntity.getNewEndDate());
        assertThat(sickNoteExtensionHistory.isAub()).isEqualTo(sickNoteExtensionEntity.isAub());
        assertThat(sickNoteExtensionHistory.status()).isEqualTo(sickNoteExtensionEntity.getStatus());
    }

    @Test
    void getSickNoteExtensionHistory_returnsEmptyListWhenNoExtensionsFound() {
        when(sickNoteExtensionRepository.findAllBySickNoteId(any())).thenReturn(List.of());

        List<SickNoteExtensionHistory> result = sickNoteExtensionHistoryService.getSickNoteExtensionHistory(1L);

        assertThat(result).isEmpty();
    }
}
