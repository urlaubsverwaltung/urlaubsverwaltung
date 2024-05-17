package org.synyx.urlaubsverwaltung.application.vacationtype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.StaticMessageSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CustomVacationTypeTest {

    static Stream<Arguments> localeTuples() {
        return Stream.of(
            Arguments.of("de", "de-de"),
            Arguments.of("en", "en-au")
        );
    }

    @ParameterizedTest
    @MethodSource("localeTuples")
    void ensureLabelFallbackToBaseLocale(String language, String languageAndCountry) {

        final CustomVacationType vacationType = CustomVacationType.builder(new StaticMessageSource())
            .labels(List.of(new VacationTypeLabel(Locale.forLanguageTag(language), "label")))
            .build();

        assertThat(vacationType.getLabel(Locale.forLanguageTag(languageAndCountry))).isEqualTo("label");
    }

    @Test
    void ensureLabelFallbackToText() {

        final StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("vacationtype.label.fallback", Locale.GERMAN, "fallback");

        final CustomVacationType vacationType = CustomVacationType.builder(messageSource)
            .labels(List.of(new VacationTypeLabel(Locale.GERMAN, "")))
            .build();

        assertThat(vacationType.getLabel(Locale.GERMAN)).isEqualTo("fallback");
    }
}
