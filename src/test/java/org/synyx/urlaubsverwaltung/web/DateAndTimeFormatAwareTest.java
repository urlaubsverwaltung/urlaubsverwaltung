package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

class DateAndTimeFormatAwareTest {

    final DateFormatAware parser = new DateFormatAware();

    @Test
    void ensureParseForIsoDateString() {
        assertThat(parser.parse("2020-10-30", GERMAN)).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseForGermanDateString() {
        assertThat(parser.parse("30.10.2020", GERMAN)).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseReturnsEmptyOptionalForUnknownDateFormat() {
        assertThat(parser.parse("30/10/2020", GERMAN)).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void ensureParseReturnsEmptyOptionalForEmptyString(String givenDateString) {
        assertThat(parser.parse(givenDateString, GERMAN)).isEmpty();
    }
}
