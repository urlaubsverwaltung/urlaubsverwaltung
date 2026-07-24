package org.synyx.urlaubsverwaltung.person;

/**
 * Thrown when {@link PersonActivePeriodServiceImpl} is asked to open or close an active period in a way that
 * is inconsistent with the currently persisted state (e.g. opening a period for a person that already has one
 * open, or closing a period for a person that has none open). This should never happen during normal operation -
 * surfacing it loudly (instead of silently ignoring it) is deliberate, since a swallowed occurrence would leave
 * the active period history silently wrong with no way to detect it later.
 */
public class PersonActivePeriodInconsistentStateException extends RuntimeException {
    PersonActivePeriodInconsistentStateException(String message) {
        super(message);
    }
}
