package org.synyx.urlaubsverwaltung.person;

import org.synyx.urlaubsverwaltung.search.StringComparators;

import java.util.Comparator;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

/**
 * Locale-aware {@link Comparator}s to sort {@link Person}s by name, so that names with umlauts, accents or
 * apostrophes are sorted next to their base letters (e.g. "Joél" is sorted between "Joe" and "Jof").
 */
public final class PersonComparators {

    private PersonComparators() {
        // ok
    }

    /**
     * Locale-aware and case-insensitive comparator sorting persons by first name, then by last name.
     *
     * @return comparator sorting persons by first name, then by last name
     */
    public static Comparator<Person> comparingFirstNameLastName() {
        final Comparator<String> nameComparator = nullsLast(StringComparators.localeAwareComparator());
        return comparing(Person::getFirstName, nameComparator).thenComparing(Person::getLastName, nameComparator);
    }

    /**
     * Locale-aware and case-insensitive comparator sorting persons by {@linkplain Person#getNiceName() nice name}.
     *
     * @return comparator sorting persons by nice name
     */
    public static Comparator<Person> comparingNiceName() {
        return comparing(Person::getNiceName, StringComparators.localeAwareComparator());
    }
}
