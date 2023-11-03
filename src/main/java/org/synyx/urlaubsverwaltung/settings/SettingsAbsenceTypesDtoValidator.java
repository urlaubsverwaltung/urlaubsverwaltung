package org.synyx.urlaubsverwaltung.settings;


import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
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

        validateUniqueLabels(absenceTypeSettings, errors);

        final List<AbsenceTypeSettingsItemDto> items = absenceTypeSettings.getItems();
        for (int i = 0; i < items.size(); i++) {
            final AbsenceTypeSettingsItemDto item = items.get(i);
            errors.pushNestedPath("items[%d]".formatted(i));
            validateLabels(item, errors);
            errors.popNestedPath();
        }

        errors.popNestedPath();
    }

    private void validateUniqueLabels(AbsenceTypeSettingsDto absenceTypeSettings, Errors errors) {

        // vacationType label must be unique for a locale

        final Map<Locale, List<String>> allLabelsByLocale = absenceTypeSettings.getItems()
            .stream()
            .map(AbsenceTypeSettingsItemDto::getLabels)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(
                AbsenceTypeSettingsItemLabelDto::getLocale,
                dto -> List.of(dto.getLabel()),
                (o, o2) -> Stream.concat(o.stream(), o2.stream()).collect(toList())
            ));

        final boolean containsDuplicates = allLabelsByLocale.values()
            .stream()
            .anyMatch(strings -> strings.size() != new HashSet<>(strings).size());

        if (containsDuplicates) {
            errors.reject("vacationtype.validation.constraints.labels.NotUnique.message");
        }
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
