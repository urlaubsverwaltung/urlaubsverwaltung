package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.CustomVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeLabel;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

public class AbsenceTypeSettingsDtoMapper {

    private AbsenceTypeSettingsDtoMapper() {
    }

    public static AbsenceTypeSettingsDto mapToAbsenceTypeItemSettingDto(List<VacationType<?>> allVacationTypes, Locale locale) {

        final List<AbsenceTypeSettingsItemDto> absenceTypeDtos = allVacationTypes
            .stream()
            .map(vacationType -> vacationTypeToDto(vacationType, locale))
            .collect(toList());

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(absenceTypeDtos);

        return absenceTypeSettingsDto;
    }

    private static AbsenceTypeSettingsItemDto vacationTypeToDto(VacationType<?> vacationType, Locale locale) {
        return AbsenceTypeSettingsItemDto.builder()
            .setId(vacationType.getId())
            .setLabel(vacationType.getLabel(locale))
            .setCategory(vacationType.getCategory())
            .setActive(vacationType.isActive())
            .setRequiresApprovalToApply(vacationType.isRequiresApprovalToApply())
            .setRequiresApprovalToCancel(vacationType.isRequiresApprovalToCancel())
            .setColor(vacationType.getColor())
            .setVisibleToEveryone(vacationType.isVisibleToEveryone())
            .setLabels(vacationTypeLabelDtos(vacationType))
            .build();
    }

    private static List<AbsenceTypeSettingsItemLabelDto> vacationTypeLabelDtos(VacationType<?> vacationType) {
        // setting label is only supported by CustomVacationType for now
        if (vacationType instanceof CustomVacationType customVacationType) {
            return customVacationType.labels()
                .stream()
                .map(AbsenceTypeSettingsDtoMapper::absenceTypeSettingsItemLabelDto)
                .toList();
        }
        return List.of();
    }

    private static AbsenceTypeSettingsItemLabelDto absenceTypeSettingsItemLabelDto(VacationTypeLabel vacationTypeLabel) {
        return new AbsenceTypeSettingsItemLabelDto(vacationTypeLabel.locale(), vacationTypeLabel.label());
    }
}
