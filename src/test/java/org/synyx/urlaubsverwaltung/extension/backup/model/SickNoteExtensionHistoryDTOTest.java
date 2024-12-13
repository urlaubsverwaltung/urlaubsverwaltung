package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionEntity;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteExtensionHistoryDTOTest {

    @Test
    void toSickNoteExtensionEntity_convertsToEntity() {
        Instant now = Instant.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        final SickNoteExtensionHistoryDTO dto = new SickNoteExtensionHistoryDTO(now, endDate, true, SickNoteExtensionStatusDTO.SUBMITTED);

        final SickNoteExtensionEntity entity = dto.toSickNoteExtensionEntity(1L);

        assertThat(entity.getCreatedAt()).isEqualTo(dto.createdAt());
        assertThat(entity.getSickNoteId()).isEqualTo(1L);
        assertThat(entity.getNewEndDate()).isEqualTo(dto.endDate());
        assertThat(entity.isAub()).isEqualTo(dto.aub());
        assertThat(entity.getStatus()).isEqualTo(dto.sickNoteExtensionStatus().toSickNoteExtensionStatus());
    }

}
