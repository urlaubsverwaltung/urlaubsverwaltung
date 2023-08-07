package org.synyx.urlaubsverwaltung.calendar;

import jakarta.persistence.Converter;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodConverterTest {

    @Test
    void ensureConvertToDatabaseColumn() {

        final PeriodConverter sut = new PeriodConverter();
        final String actual = sut.convertToDatabaseColumn(Period.parse("P42Y"));

        assertThat(actual).isEqualTo("P42Y");
    }

    @Test
    void ensureConvertToEntityAttribute() {

        final PeriodConverter sut = new PeriodConverter();
        final Period actual = sut.convertToEntityAttribute("P42Y");

        assertThat(actual).isEqualTo(Period.parse("P42Y"));
    }

    @Test
    void ensureAutoApplyConverterAnnotation() {

        final Converter annotation = PeriodConverter.class.getAnnotation(Converter.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.autoApply()).isFalse();
    }
}
