package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsAbsenceTypesDtoValidatorTest {

    private SettingsAbsenceTypesDtoValidator sut;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        sut = new SettingsAbsenceTypesDtoValidator(messageSource);
    }

    @Test
    void ensureSupportsIsTrueForDto() {
        assertThat(sut.supports(SettingsAbsenceTypesDto.class)).isTrue();
    }

    @Test
    void ensureSupportsIsFalse() {
        assertThat(sut.supports(Object.class)).isFalse();
    }

    @Test
    void ensureValidationFailsWhenNoLabelIsSet() {

        final AbsenceTypeSettingsItemDto validAbsenceTypeDto = new AbsenceTypeSettingsItemDto();
        validAbsenceTypeDto.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "some text")
        ));

        final AbsenceTypeSettingsItemDto invalidAbsenceTypeDto = new AbsenceTypeSettingsItemDto();
        invalidAbsenceTypeDto.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "")
        ));

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(List.of(validAbsenceTypeDto, invalidAbsenceTypeDto));

        final SettingsAbsenceTypesDto dto = new SettingsAbsenceTypesDto();
        dto.setAbsenceTypeSettings(absenceTypeSettingsDto);

        final Errors errors = new BeanPropertyBindingResult(dto, "settingsAbsenceTypesDto");
        sut.validate(dto, errors);

        assertThat(errors.getFieldError("absenceTypeSettings.items[0].labels")).isNull();
        assertThat(errors.getFieldError("absenceTypeSettings.items[1].labels")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotEmpty.message");
            assertThat(error.getArguments()).isNull();
        });
    }

    @Test
    void ensureValidationFailsWhenLabelIsNotUnique() {

        final AbsenceTypeSettingsItemDto absenceTypeDto1 = new AbsenceTypeSettingsItemDto();
        absenceTypeDto1.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, "irgendein text"),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "")
        ));

        final AbsenceTypeSettingsItemDto absenceTypeDto2 = new AbsenceTypeSettingsItemDto();
        absenceTypeDto2.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, "irgendein text"),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "some text")
        ));

        final AbsenceTypeSettingsItemDto absenceTypeDto3 = new AbsenceTypeSettingsItemDto();
        absenceTypeDto3.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "some text")
        ));

        final AbsenceTypeSettingsItemDto absenceTypeDto4 = new AbsenceTypeSettingsItemDto();
        absenceTypeDto4.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, "das hier ist ok"),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "")
        ));

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(List.of(absenceTypeDto1, absenceTypeDto2, absenceTypeDto3, absenceTypeDto4));

        final SettingsAbsenceTypesDto dto = new SettingsAbsenceTypesDto();
        dto.setAbsenceTypeSettings(absenceTypeSettingsDto);

        final Errors errors = new BeanPropertyBindingResult(dto, "settingsAbsenceTypesDto");
        sut.validate(dto, errors);

        assertThat(errors.getFieldError("absenceTypeSettings.items[0].labels[0].label")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotUnique.message");
            assertThat(error.getArguments()).isEqualTo(new Object[]{"irgendein text"});
        });
        assertThat(errors.getFieldError("absenceTypeSettings.items[0].labels[1].label")).isNull();

        assertThat(errors.getFieldError("absenceTypeSettings.items[1].labels[0].label")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotUnique.message");
            assertThat(error.getArguments()).isEqualTo(new Object[]{"irgendein text"});
        });
        assertThat(errors.getFieldError("absenceTypeSettings.items[1].labels[1].label")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotUnique.message");
            assertThat(error.getArguments()).isEqualTo(new Object[]{"some text"});
        });

        assertThat(errors.getFieldError("absenceTypeSettings.items[2].labels[0].label")).isNull();
        assertThat(errors.getFieldError("absenceTypeSettings.items[2].labels[1].label")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotUnique.message");
            assertThat(error.getArguments()).isEqualTo(new Object[]{"some text"});
        });

        assertThat(errors.getFieldError("absenceTypeSettings.items[3].labels[0].label")).isNull();
        assertThat(errors.getFieldError("absenceTypeSettings.items[3].labels[1].label")).isNull();
    }

    // delete me as soon as there are no ProvidedVacationTypes anymore (and the user has to maintain all labels)
    @Test
    void ensureValidationFailsWhenLabelClashesWithProvidedVacationTypeLabelForCurrentContextLocale() {

        for (SupportedLanguages supportedLanguage : SupportedLanguages.values()) {
            final String message = switch (supportedLanguage) {
                case GERMAN -> "Erholungsurlaub";
                case GERMAN_AUSTRIA -> "Erholungsurlaub";
                case ENGLISH -> "Holiday";
                case GREEK -> "Διακοπές";
            };
            when(messageSource.getMessage("provided.messagekey", new Object[]{}, supportedLanguage.getLocale())).thenReturn(message);
        }

        final AbsenceTypeSettingsItemDto providedAbsenceTypeDto = new AbsenceTypeSettingsItemDto();
        providedAbsenceTypeDto.setLabel("Holiday");
        providedAbsenceTypeDto.setMessageKey("provided.messagekey");
        providedAbsenceTypeDto.setLabels(List.of());

        final AbsenceTypeSettingsItemDto customAbsenceTypeDto = new AbsenceTypeSettingsItemDto();
        customAbsenceTypeDto.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, ""),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "Holiday")
        ));

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(List.of(providedAbsenceTypeDto, customAbsenceTypeDto));

        final SettingsAbsenceTypesDto dto = new SettingsAbsenceTypesDto();
        dto.setAbsenceTypeSettings(absenceTypeSettingsDto);

        final Errors errors = new BeanPropertyBindingResult(dto, "settingsAbsenceTypesDto");
        sut.validate(dto, errors);

        assertThat(errors.getFieldError("absenceTypeSettings.items[0]")).isNull();
        assertThat(errors.getFieldError("absenceTypeSettings.items[1].labels[0].label")).isNull();
        assertThat(errors.getFieldError("absenceTypeSettings.items[1].labels[1].label")).satisfies(error -> {
            assertThat(error.getCode()).isEqualTo("vacationtype.validation.constraints.labels.NotUnique.message");
            assertThat(error.getArguments()).isEqualTo(new Object[]{"Holiday"});
        });
    }

    @Test
    void ensureValidationSucceeds() {

        for (SupportedLanguages supportedLanguage : SupportedLanguages.values()) {
            final String message = switch (supportedLanguage) {
                case GERMAN -> "Erholungsurlaub";
                case GERMAN_AUSTRIA -> "Erholungsurlaub";
                case ENGLISH -> "Holiday";
                case GREEK -> "Διακοπές";
            };
            when(messageSource.getMessage("provided.messagekey", new Object[]{}, supportedLanguage.getLocale())).thenReturn(message);
        }

        final AbsenceTypeSettingsItemDto providedAbsenceTypeDto = new AbsenceTypeSettingsItemDto();
        providedAbsenceTypeDto.setLabel("Erholungsurlaub");
        providedAbsenceTypeDto.setMessageKey("provided.messagekey");
        providedAbsenceTypeDto.setLabels(List.of());

        final AbsenceTypeSettingsItemDto customAbsenceTypeDto1 = new AbsenceTypeSettingsItemDto();
        customAbsenceTypeDto1.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, "Jokertag"),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "")
        ));

        final AbsenceTypeSettingsItemDto customAbsenceTypeDto2 = new AbsenceTypeSettingsItemDto();
        customAbsenceTypeDto2.setLabels(List.of(
            new AbsenceTypeSettingsItemLabelDto(Locale.GERMAN, "Familientag"),
            new AbsenceTypeSettingsItemLabelDto(Locale.ENGLISH, "")
        ));

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(List.of(providedAbsenceTypeDto, customAbsenceTypeDto1, customAbsenceTypeDto2));

        final SettingsAbsenceTypesDto dto = new SettingsAbsenceTypesDto();
        dto.setAbsenceTypeSettings(absenceTypeSettingsDto);

        final Errors errors = new BeanPropertyBindingResult(dto, "settingsAbsenceTypesDto");
        sut.validate(dto, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
