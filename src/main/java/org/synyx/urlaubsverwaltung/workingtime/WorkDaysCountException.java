package org.synyx.urlaubsverwaltung.workingtime;

/**
 * Exception that is thrown when no valid {@link WorkingTime} can be found for a period.
 */
public class WorkDaysCountException extends IllegalStateException {

    WorkDaysCountException(String message) {
        super(message);
    }
}
