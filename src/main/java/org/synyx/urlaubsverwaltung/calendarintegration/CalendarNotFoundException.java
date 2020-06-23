package org.synyx.urlaubsverwaltung.calendarintegration;

/**
 * Exception thrown if the calendar with a given name can not be found.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
public class CalendarNotFoundException extends IllegalArgumentException {

    public CalendarNotFoundException(String message, Throwable cause) {

        super(message, cause);
    }
}
