package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteTypeCategoryDTOTest {

    @ParameterizedTest
    @EnumSource(SickNoteCategory.class)
    void happyPathSickNoteCategoryToDTO(SickNoteCategory sickNoteTypeCategory) {
        SickNoteTypeCategoryDTO dto = SickNoteTypeCategoryDTO.valueOf(sickNoteTypeCategory.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(SickNoteTypeCategoryDTO.class)
    void happyPathDTOToSickNoteCategory(SickNoteTypeCategoryDTO dto) {
        SickNoteCategory category = dto.toSickNoteTypeCategory();
        assertThat(category).isNotNull();
    }

}
