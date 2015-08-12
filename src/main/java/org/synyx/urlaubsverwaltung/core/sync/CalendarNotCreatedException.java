package org.synyx.urlaubsverwaltung.core.sync;

/**
 * Exception thrown if an error occurs during creation of a calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalendarNotCreatedException extends IllegalArgumentException {

    public CalendarNotCreatedException(String message, Throwable cause) {

        super(message, cause);
    }
}
