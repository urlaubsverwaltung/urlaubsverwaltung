package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateFormatAwareTest {

    final DateFormatAware parser = new DateFormatAware();

    @Test
    void ensureParseForIsoDateString() {
        assertThat(parser.parse("2020-10-30")).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseForGermanDateString() {
        assertThat(parser.parse("30.10.2020")).hasValue(LocalDate.of(2020, 10, 30));
    }

    @Test
    void ensureParseReturnsEmptyOptionalForUnknownDateFormat() {
        assertThat(parser.parse("30/10/2020")).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "  "})
    void ensureParseReturnsEmptyOptionalForEmptyString(String givenDateString) {
        assertThat(parser.parse(givenDateString)).isEmpty();
    }
}
