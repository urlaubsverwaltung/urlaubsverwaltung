package org.synyx.urlaubsverwaltung.settings;


import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;

@Component
class SettingsAbsenceTypesDtoValidator implements Validator {

    private final MessageSource messageSource;

    SettingsAbsenceTypesDtoValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

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

        validateUniqueLabel(absenceTypeSettings, errors);
        validateAtLeastOneLabelTranslation(absenceTypeSettings, errors);

        errors.popNestedPath();
    }

    private void validateUniqueLabel(AbsenceTypeSettingsDto absenceTypeSettings, Errors errors) {

        // map the itemDtos to a projection view.
        // this is obsolete as soon as there are no ProvidedVacationTypes anymore (and the user has to maintain all labels)
        final List<ItemProjection> itemProjections = absenceTypeSettings.getItems()
            .stream()
            .map(this::toProjection)
            .toList();

        final Map<Locale, List<String>> allLabelsByLocale = itemProjections.stream()
            .map(ItemProjection::getLabels)
            .flatMap(Collection::stream)
            .collect(toMap(
                LabelProjection::getLocale,
                dto -> List.of(dto.getLabel()),
                (strings, strings2) -> Stream.concat(strings.stream(), strings2.stream()).toList()
            ));

        final Predicate<LabelProjection> isNotUniqueLabel = dto ->
            // `dto.custom` is obsolete as soon as there are no ProvidedVacationTypes anymore.
            dto.custom && hasText(dto.getLabel()) && allLabelsByLocale.get(dto.getLocale())
                .stream()
                .filter(label -> label.strip().equalsIgnoreCase(dto.getLabel().strip()))
                .toList()
                .size() > 1;

        for (int index = 0; index < itemProjections.size(); index++) {
            final ItemProjection itemDto = itemProjections.get(index);

            final List<NotUniqueLabelDto> notUniqueLabels = notUniqueLabelTranslations(itemDto, isNotUniqueLabel);
            for (NotUniqueLabelDto notUniqueLabelDto : notUniqueLabels) {
                final String label = itemDto.getLabels().stream()
                    .filter(labelDto -> labelDto.getLocale().equals(notUniqueLabelDto.locale))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("expected a label text for locale=%s".formatted(notUniqueLabelDto.locale)))
                    .getLabel();
                errors.rejectValue(
                    "items[%d].labels[%d].label".formatted(index, notUniqueLabelDto.index),
                    "vacationtype.validation.constraints.labels.NotUnique.message",
                    new Object[]{label},
                    null
                );
                // used to detect label translation error at the labels element
                // no code and no defaultMessage to skip error messages in ui in this case
                errors.rejectValue("items[%d].labels".formatted(index), "", "");
            }
        }
    }

    private record NotUniqueLabelDto(int index, Locale locale) {}

    // obsolete as soon as there are no ProvidedVacationTypes anymore
    private ItemProjection toProjection(AbsenceTypeSettingsItemDto dto) {

        final List<LabelProjection> labels;
        final boolean customType = isCustomVacationType(dto);

        if (customType) {
            labels = dto.getLabels().stream()
                .map(labelDto -> new LabelProjection(labelDto.getLocale(), labelDto.getLabel(), true))
                .toList();
        } else {
            labels = Arrays.stream(SupportedLanguages.values())
                .map(supportedLanguage -> {
                    final Locale supportedLanguageLocale = supportedLanguage.getLocale();
                    final String label = msg(dto.getMessageKey(), supportedLanguageLocale);
                    return new LabelProjection(supportedLanguageLocale, label, false);
                })
                .toList();
        }

        return new ItemProjection(labels, customType);
    }

    private static boolean isCustomVacationType(AbsenceTypeSettingsItemDto item) {
        return item.getLabels() != null && !item.getLabels().isEmpty();
    }

    private String msg(String code, Locale locale) {
        return messageSource.getMessage(code, new Object[]{}, locale);
    }

    private record ItemProjection(List<LabelProjection> labels, boolean custom) {
        List<LabelProjection> getLabels() { return labels; }
    }

    private record LabelProjection(Locale locale, String label, boolean custom) {
        Locale getLocale() { return locale; }
        String getLabel() { return label; }
    }

    private List<NotUniqueLabelDto> notUniqueLabelTranslations(ItemProjection itemProjection, Predicate<LabelProjection> checkNotUnique) {
        final List<NotUniqueLabelDto> notUniqueIndexes = new ArrayList<>();

        for (int index = 0; index < itemProjection.labels().size(); index++) {
            final LabelProjection labelDto = itemProjection.labels().get(index);
            if (checkNotUnique.test(labelDto)) {
                notUniqueIndexes.add(new NotUniqueLabelDto(index, labelDto.getLocale()));
            }
        }

        return notUniqueIndexes;
    }

    private void validateAtLeastOneLabelTranslation(AbsenceTypeSettingsDto absenceTypeSettings, Errors errors) {
        final List<AbsenceTypeSettingsItemDto> items = absenceTypeSettings.getItems();
        for (int i = 0; i < items.size(); i++) {
            final AbsenceTypeSettingsItemDto item = items.get(i);
            errors.pushNestedPath("items[%d]".formatted(i));
            validateLabelsHaveText(item, errors);
            errors.popNestedPath();
        }
    }

    private void validateLabelsHaveText(AbsenceTypeSettingsItemDto item, Errors errors) {
        final List<AbsenceTypeSettingsItemLabelDto> labels = item.getLabels();
        if (labels == null || labels.isEmpty()) {
            // label translation list is null for ProvidedVacationType
            return;
        }

        final boolean validLabels = labels.stream().anyMatch(labelDto -> hasText(labelDto.getLabel()));
        if (!validLabels) {
            errors.rejectValue("labels", "vacationtype.validation.constraints.labels.NotEmpty.message");
        }
    }
}
