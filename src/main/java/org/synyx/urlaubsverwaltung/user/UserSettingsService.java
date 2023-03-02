package org.synyx.urlaubsverwaltung.user;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface UserSettingsService {

    /**
     * Returns the effective locale of a list of persons.
     * <p>
     * The effective locale is based (in order) of:
     * - saved locale
     * - saved browser specific locale (if saved locale is null)
     *
     * @return map of persons and their effective locale
     */
    Map<Person, Locale> getEffectiveLocale(List<Person> persons);
}
