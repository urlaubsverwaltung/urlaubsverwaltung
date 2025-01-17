package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;

import static org.assertj.core.api.Assertions.assertThat;


class VacationTypeCategoryDTOTest {

    @ParameterizedTest
    @EnumSource(VacationCategory.class)
    void happyPathVacationCategoryToDTO(VacationCategory vacationTypeCategory) {
        VacationTypeCategoryDTO dto = VacationTypeCategoryDTO.valueOf(vacationTypeCategory.name());
        assertThat(dto).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(VacationTypeCategoryDTO.class)
    void happyPathDTOToVacationCategory(VacationTypeCategoryDTO dto) {
        VacationCategory category = dto.toVacationCategory();
        assertThat(category).isNotNull();
    }
}
