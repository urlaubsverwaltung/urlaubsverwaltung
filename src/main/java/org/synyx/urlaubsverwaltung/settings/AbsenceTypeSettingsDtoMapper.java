package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AbsenceTypeSettingsDtoMapper {

    private AbsenceTypeSettingsDtoMapper() {
    }

    public static AbsenceTypeSettingsDto mapToAbsenceTypeItemSettingDto(List<VacationType> allVacationTypes) {

        final List<AbsenceTypeSettingsItemDto> absenceTypeDtos = allVacationTypes
            .stream()
            .map(AbsenceTypeSettingsDtoMapper::vacationTypeToDto)
            .collect(toList());

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(absenceTypeDtos);

        return absenceTypeSettingsDto;
    }

    private static AbsenceTypeSettingsItemDto vacationTypeToDto(VacationType vacationType) {
        return AbsenceTypeSettingsItemDto.builder()
            .setId(vacationType.getId())
            .setMessageKey(vacationType.getMessageKey())
            .setCategory(vacationType.getCategory())
            .setActive(vacationType.isActive())
            .setRequiresApproval(vacationType.isRequiresApproval())
            .setColor(vacationType.getColor())
            .build();
    }
}
