package org.synyx.urlaubsverwaltung.search;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class StringComparatorsTest {

    @Test
    void ensureUmlautsAndAccentsAreSortedNextToTheirBaseLetters() {

        final Comparator<String> sut = StringComparators.localeAwareComparator(Locale.GERMAN);

        final List<String> sorted = List.of("Jof", "Özdemir", "Joe", "Otto", "Joél", "Müller", "Muster", "Mzz")
            .stream().sorted(sut).toList();

        assertThat(sorted).containsExactly("Joe", "Joél", "Jof", "Müller", "Muster", "Mzz", "Otto", "Özdemir");
    }

    @Test
    void ensureApostrophesAreHandled() {

        final Comparator<String> sut = StringComparators.localeAwareComparator(Locale.GERMAN);

        final List<String> sorted = List.of("Obrecht", "O'Brian", "Otto")
            .stream().sorted(sut).toList();

        assertThat(sorted).containsExactly("O'Brian", "Obrecht", "Otto");
    }

    @Test
    void ensureCaseIsIgnored() {

        final Comparator<String> sut = StringComparators.localeAwareComparator(Locale.GERMAN);

        assertThat(sut.compare("anna", "ANNA")).isZero();
        assertThat(sut.compare("anna", "Bernd")).isNegative();
        assertThat(sut.compare("BERND", "anna")).isPositive();
    }
}
