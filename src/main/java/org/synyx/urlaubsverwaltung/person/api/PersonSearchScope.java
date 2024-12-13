package org.synyx.urlaubsverwaltung.person.api;

/**
 * Defines the scope of a person search used in {@link PersonApiController#persons(PersonSearchScope)}.
 */
public enum PersonSearchScope {
    /**
     * Only return persons that are active. This is the default behabiour
     */
    ACTIVE_PERSONS,
    /**
     * Return all persons, including inactive ones.
     */
    ALL_PERSONS
}
