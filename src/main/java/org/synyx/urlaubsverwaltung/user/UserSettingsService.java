package org.synyx.urlaubsverwaltung.user;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface UserSettingsService {

    /**
     * Returns the saved locale of the user
     *
     * @param person to retrieve the locale
     * @return saved locale
     */
    Optional<Locale> getLocale(Person person);

    /**
     * Sets the browser specific locale from the request.
     * <p>
     * Only saves the browser specific locale if the saved 'locale' is null.
     * If the saved 'locale' is null, that means, that the localization is based on the browser,
     * and therefore we save it to use it in e-mail templates e.g.
     *
     * @param person                to save the browser specific locale
     * @param localeBrowserSpecific browser specific locale
     */
    void updateLocaleBrowserSpecific(Person person, Locale localeBrowserSpecific);

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

    /**
     * Returns the user settings for the given person
     *
     * @param person to retrieve user settings for
     * @return user settings of the user
     */
    UserSettings getUserSettingsForPerson(Person person);
}
