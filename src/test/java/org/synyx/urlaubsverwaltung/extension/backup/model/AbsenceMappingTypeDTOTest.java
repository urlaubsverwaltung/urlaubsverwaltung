package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceMappingTypeDTOTest {

    @ParameterizedTest
    @EnumSource(AbsenceMappingType.class)
    void happyPathToDTO(AbsenceMappingType type) {
        AbsenceMappingTypeDTO typeDTO = AbsenceMappingTypeDTO.valueOf(type.name());
        assertThat(typeDTO).isNotNull();
    }


    @ParameterizedTest
    @EnumSource(AbsenceMappingTypeDTO.class)
    void happyPathToDTO(AbsenceMappingTypeDTO type) {
        AbsenceMappingType absenceMappingType = type.toAbsenceMappingType();
        assertThat(absenceMappingType).isNotNull();
    }
}
