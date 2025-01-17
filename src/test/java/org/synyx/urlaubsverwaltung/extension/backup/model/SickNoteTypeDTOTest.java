package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteTypeDTOTest {

    @Test
    void happyPath() {

        final SickNoteTypeDTO dto = new SickNoteTypeDTO(1L, SickNoteTypeCategoryDTO.SICK_NOTE, "messageKey");

        final SickNoteType entity = dto.toSickNoteEntity();

        assertThat(entity.getCategory()).isEqualTo(dto.category().toSickNoteTypeCategory());
        assertThat(entity.getMessageKey()).isEqualTo(dto.messageKey());
    }

}
