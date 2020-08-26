package org.synyx.urlaubsverwaltung.workingtime;

/**
 * Exception that is thrown when no valid {@link WorkingTime} can be found for a period.
 */
public class NoValidWorkingTimeException extends IllegalStateException {

    NoValidWorkingTimeException(String message) {
        super(message);
    }
}
