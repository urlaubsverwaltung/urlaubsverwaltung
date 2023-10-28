package org.synyx.urlaubsverwaltung.settings;


import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Component
class SettingsAbsenceTypesDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return SettingsAbsenceTypesDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final SettingsAbsenceTypesDto dto = (SettingsAbsenceTypesDto) target;
        validateAbsenceTypeSettings(dto, errors);
    }

    private void validateAbsenceTypeSettings(SettingsAbsenceTypesDto dto, Errors errors) {

        final AbsenceTypeSettingsDto absenceTypeSettings = dto.getAbsenceTypeSettings();
        errors.pushNestedPath("absenceTypeSettings");

        final List<AbsenceTypeSettingsItemDto> items = absenceTypeSettings.getItems();
        for (int i = 0; i < items.size(); i++) {
            final AbsenceTypeSettingsItemDto item = items.get(i);
            errors.pushNestedPath("items[%d]".formatted(i));
            validateLabels(item, errors);
            errors.popNestedPath();
        }

        errors.popNestedPath();
    }

    private void validateLabels(AbsenceTypeSettingsItemDto item, Errors errors) {
        final List<AbsenceTypeSettingsItemLabelDto> labels = item.getLabels();
        if (labels == null) {
            // label translation list is null for ProvidedVacationType
            return;
        }

        final boolean validLabels = labels.stream().anyMatch(labelDto -> hasText(labelDto.getLabel()));
        if (!validLabels) {
            errors.rejectValue("labels", "vacationtype.validation.constraints.labels.NotEmpty.message");
        }
    }
}
