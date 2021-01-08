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
    void convertToDatabaseColumn() {

        final Double converted = sut.convertToDatabaseColumn(Duration.parse("PT1H15M"));
        assertThat(converted).isEqualTo(1.25);
    }

    @Test
    void convertToEntityAttribute() {

        assertThat(sut.convertToEntityAttribute(1.25)).isEqualTo(Duration.parse("PT1H15M"));
        assertThat(sut.convertToEntityAttribute(1.26)).isEqualTo(Duration.parse("PT1H15M"));
        assertThat(sut.convertToEntityAttribute(1.24)).isEqualTo(Duration.parse("PT1H14M"));
        assertThat(sut.convertToEntityAttribute(1.23)).isEqualTo(Duration.parse("PT1H13M"));
    }
}
