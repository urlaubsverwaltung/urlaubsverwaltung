package org.synyx.urlaubsverwaltung.extension.backup.model;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteStatusDTOTest {

    @ParameterizedTest
    @EnumSource(SickNoteStatus.class)
    void happyPathDTOToSickNoteStatus(SickNoteStatus sickNoteStatus) {
        SickNoteStatusDTO dto = SickNoteStatusDTO.valueOf(sickNoteStatus.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(SickNoteStatusDTO.class)
    void happyPathDTOToSickNoteStatus(SickNoteStatusDTO dto) {
        SickNoteStatus sickNoteStatus = dto.toSickNoteStatus();
        assertThat(sickNoteStatus).isNotNull();
    }
}
