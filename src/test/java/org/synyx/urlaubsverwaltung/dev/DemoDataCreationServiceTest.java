package org.synyx.urlaubsverwaltung.dev;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DemoDataCreationServiceTest {

    @Test
    void ensuresPersonnelNumberIsStableForSameEmail() {
        final int first = DemoDataCreationService.personnelNumberOf("max.muster@example.org");
        final int second = DemoDataCreationService.personnelNumberOf("max.muster@example.org");
        assertThat(first).isEqualTo(second);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "max.muster@example.org", "very.long.name.with.negative.hashcode@example.org"})
    void ensuresPersonnelNumberIsInRange(String email) {
        final int actual = DemoDataCreationService.personnelNumberOf(email);
        assertThat(actual).isBetween(11, 1000);
    }
}
