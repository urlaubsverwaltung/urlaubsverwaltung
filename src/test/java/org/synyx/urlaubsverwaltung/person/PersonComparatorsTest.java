package org.synyx.urlaubsverwaltung.person;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class PersonComparatorsTest {

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void ensureComparingFirstNameLastNameSortsUmlautsNextToBaseLetters() {
        LocaleContextHolder.setLocale(Locale.GERMAN);

        final Person joe = new Person("joe", "Doe", "Joe", "joe@example.org");
        final Person joel = new Person("joel", "Doe", "Joél", "joel@example.org");
        final Person jof = new Person("jof", "Doe", "Jof", "jof@example.org");
        final Person juergen = new Person("juergen", "Doe", "Jürgen", "juergen@example.org");
        final Person justus = new Person("justus", "Doe", "Justus", "justus@example.org");

        final List<Person> sorted = List.of(justus, jof, juergen, joel, joe)
            .stream().sorted(PersonComparators.comparingFirstNameLastName()).toList();

        assertThat(sorted).containsExactly(joe, joel, jof, juergen, justus);
    }

    @Test
    void ensureComparingFirstNameLastNameSortsByLastNameWhenFirstNamesAreEqual() {
        LocaleContextHolder.setLocale(Locale.GERMAN);

        final Person mueller = new Person("mueller", "Müller", "Marlene", "mueller@example.org");
        final Person muster = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person mzz = new Person("mzz", "Mzz", "Marlene", "mzz@example.org");

        final List<Person> sorted = List.of(mzz, muster, mueller)
            .stream().sorted(PersonComparators.comparingFirstNameLastName()).toList();

        assertThat(sorted).containsExactly(mueller, muster, mzz);
    }

    @Test
    void ensureComparingFirstNameLastNameSortsNullNamesLast() {

        final Person withoutName = new Person("username", null, null, "username@example.org");
        final Person zora = new Person("zora", "Doe", "Zora", "zora@example.org");

        final List<Person> sorted = List.of(withoutName, zora)
            .stream().sorted(PersonComparators.comparingFirstNameLastName()).toList();

        assertThat(sorted).containsExactly(zora, withoutName);
    }

    @Test
    void ensureComparingNiceNameSortsUmlautsNextToBaseLetters() {
        LocaleContextHolder.setLocale(Locale.GERMAN);

        final Person otto = new Person("otto", "Otto", "Anna", "otto@example.org");
        final Person oezdemir = new Person("oezdemir", "Özdemir", "Anna", "oezdemir@example.org");
        final Person obrian = new Person("obrian", "O'Brian", "Anna", "obrian@example.org");

        final List<Person> sorted = List.of(otto, oezdemir, obrian)
            .stream().sorted(PersonComparators.comparingNiceName()).toList();

        assertThat(sorted).containsExactly(obrian, otto, oezdemir);
    }
}
