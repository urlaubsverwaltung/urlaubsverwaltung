package org.synyx.urlaubsverwaltung.person;

import java.util.Optional;
import java.util.function.Function;

/**
 * Defines sortable attributes of a {@link Person}.
 */
public enum PersonSortProperty {

    FIRST_NAME(PersonSortProperty.FIRST_NAME_KEY, Person::getFirstName),
    LAST_NAME(PersonSortProperty.LAST_NAME_KEY, Person::getLastName);

    /**
     * Value used by APIs, in URLQueryParams for instance.
     *
     * <p>
     * This is not bound to {@link Person} attribute names, {@link PersonSortProperty#propertyExtractor()} is!
     */
    private final String key;

    /**
     * a {@link Person} function (e.g. <code>Person::getFirstName</code>)
     */
    private final Function<Person, ?> propertyExtractor;

    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";

    PersonSortProperty(String key, Function<Person, ?> propertyExtractor) {
        this.key = key;
        this.propertyExtractor = propertyExtractor;
    }

    /**
     * Creates the {@link PersonSortProperty} for the given key.
     *
     * @param key key to map
     * @return the matched {@link PersonSortProperty}, {@link Optional#empty()} if key is unknown.
     */
    public static Optional<PersonSortProperty> byKey(String key) {
        final PersonSortProperty sort = switch (key) {
            case FIRST_NAME_KEY -> FIRST_NAME;
            case LAST_NAME_KEY -> LAST_NAME;
            default -> null;
        };
        return Optional.ofNullable(sort);
    }

    /**
     * Can be used to expose this property as string.
     *
     * @return a string representation of this {@link PersonSortProperty}
     */
    public String key() {
        return key;
    }

    /**
     * The {@link Person} property extractor of this {@link PersonSortProperty}.
     *
     * <p>
     * Usage:
     *
     * <pre><code>
     *     Sort.TypedSort<Person> typeSort = Sort.sort(Person.class);
     *     typeSort.by(FIRST_NAME::propertyExtractor);
     * </code></pre>
     *
     * @return the {@link Person} property extractor
     */
    public Function<Person, ?> propertyExtractor() {
        return propertyExtractor;
    }
}
