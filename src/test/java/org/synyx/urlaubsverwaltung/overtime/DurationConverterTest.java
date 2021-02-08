package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DurationConverterTest {

    DurationConverter sut;

    @BeforeEach
    void setUp() {
        sut = new DurationConverter();
    }

    @Test
    void ensureConversionOfMinutesCorrectly() {
        for (int i = 0; i <= 60; i++) {
            Duration expected = Duration.ofMinutes(i);
            final Duration actual = sut.convertToEntityAttribute(sut.convertToDatabaseColumn(expected));
            assertThat(actual).isEqualTo(expected);
        }
    }
}
