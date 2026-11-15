package org.synyx.urlaubsverwaltung;

import jakarta.persistence.Converter;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;

class LocaleStringMapJpaConverterTest {

    private final LocaleStringMapJpaConverter sut = new LocaleStringMapJpaConverter();

    @Test
    void ensureConvertToDatabaseColumn() {

        final String actual = sut.convertToDatabaseColumn(Map.of(GERMAN, "Betriebsruhe"));

        assertThat(actual).isEqualTo("{\"de\":\"Betriebsruhe\"}");
    }

    @Test
    void ensureConvertToDatabaseColumnWithNull() {

        final String actual = sut.convertToDatabaseColumn(null);

        assertThat(actual).isNull();
    }

    @Test
    void ensureConvertToEntityAttribute() {

        final Map<Locale, String> actual = sut.convertToEntityAttribute("{\"de\":\"Betriebsruhe\",\"en\":\"Company closure\"}");

        assertThat(actual).containsExactlyInAnyOrderEntriesOf(Map.of(GERMAN, "Betriebsruhe", ENGLISH, "Company closure"));
    }

    @Test
    void ensureConvertToEntityAttributeWithNull() {

        final Map<Locale, String> actual = sut.convertToEntityAttribute(null);

        assertThat(actual).isNull();
    }

    @Test
    void ensureConvertToEntityAttributeWithInvalidJsonReturnsEmptyMap() {

        final Map<Locale, String> actual = sut.convertToEntityAttribute("not json");

        assertThat(actual).isEmpty();
    }

    @Test
    void ensureAutoApplyConverterAnnotation() {

        final Converter annotation = LocaleStringMapJpaConverter.class.getAnnotation(Converter.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.autoApply()).isFalse();
    }
}
