package org.synyx.urlaubsverwaltung.sync;

/**
 * Exception thrown if an error occurs during creation of a calendar.
 */
public class CalendarNotCreatedException extends IllegalArgumentException {

    public CalendarNotCreatedException(String message, Throwable cause) {

        super(message, cause);
    }
}
