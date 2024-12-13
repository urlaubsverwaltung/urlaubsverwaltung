package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMapping;

public record AbsenceMappingDTO(Long id, Long absenceId, AbsenceMappingTypeDTO absenceMappingType, String eventId) {

    public static AbsenceMappingDTO of(AbsenceMapping absenceMapping) {
        return new AbsenceMappingDTO(absenceMapping.getId(), absenceMapping.getAbsenceId(), AbsenceMappingTypeDTO.valueOf(absenceMapping.getAbsenceMappingType().name()), absenceMapping.getEventId());
    }

    public AbsenceMapping toAbsenceMapping(Long absenceIdOfCreatedAbsence) {
        return new AbsenceMapping(absenceIdOfCreatedAbsence, absenceMappingType.toAbsenceMappingType(), eventId);
    }
}
