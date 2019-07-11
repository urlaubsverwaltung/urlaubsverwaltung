package org.synyx.urlaubsverwaltung.workingtime;

/**
 * Exception that is thrown when no valid {@link WorkingTime} can be found for a period.
 */
public class NoValidWorkingTimeException extends IllegalStateException {

    private final String message;

    NoValidWorkingTimeException(String message) {

        super(message);

        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }
}
