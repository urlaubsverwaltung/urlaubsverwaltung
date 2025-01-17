package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceMappingDTOTest {

    @Test
    void happyPathAbsenceMappingToDTO() {
        AbsenceMapping absenceMapping = new AbsenceMapping(1234L, AbsenceMappingType.VACATION, "event123");
        absenceMapping.setId(1L);

        AbsenceMappingDTO absenceMappingDTO = AbsenceMappingDTO.of(absenceMapping);

        assertThat(absenceMappingDTO).isNotNull();
        assertThat(absenceMappingDTO.id()).isEqualTo(absenceMapping.getId());
        assertThat(absenceMappingDTO.absenceId()).isEqualTo(absenceMapping.getAbsenceId());
        assertThat(absenceMappingDTO.absenceMappingType()).isEqualTo(AbsenceMappingTypeDTO.VACATION);
        assertThat(absenceMappingDTO.eventId()).isEqualTo(absenceMapping.getEventId());
    }

    @Test
    void happyPathDTOToAbsenceMapping() {
        long absenceIdOfCreatedAbsence = 4321L;
        final AbsenceMappingDTO absenceMappingDTO = new AbsenceMappingDTO(1L, 1234L, AbsenceMappingTypeDTO.VACATION, "eventId");

        final AbsenceMapping absenceMapping = absenceMappingDTO.toAbsenceMapping(absenceIdOfCreatedAbsence);

        assertThat(absenceMapping).isNotNull();
        assertThat(absenceMapping.getAbsenceId()).isEqualTo(absenceIdOfCreatedAbsence);
        assertThat(absenceMapping.getAbsenceMappingType()).isEqualTo(absenceMappingDTO.absenceMappingType().toAbsenceMappingType());
        assertThat(absenceMapping.getEventId()).isEqualTo(absenceMappingDTO.eventId());
    }

}
