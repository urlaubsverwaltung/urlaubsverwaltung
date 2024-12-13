package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VacationTypeColorDTOTest {

    @ParameterizedTest
    @EnumSource(VacationTypeColor.class)
    void happyPathVacationTypeColorToDTO(VacationTypeColor color) {
        VacationTypeColorDTO colorDTO = VacationTypeColorDTO.valueOf(color.name());
        assertNotNull(colorDTO);
    }

    @ParameterizedTest
    @EnumSource(VacationTypeColorDTO.class)
    void happyPathDTOToVacationTypeColor(VacationTypeColorDTO colorDTO) {
        VacationTypeColor color = colorDTO.toVacationTypeColor();
        assertNotNull(color);
    }

}
