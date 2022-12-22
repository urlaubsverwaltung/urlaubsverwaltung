package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

/**
 * Thrown in case no application for leave found for a certain ID.
 */
public class UnknownApplicationForLeaveException extends AbstractNoResultFoundException {

    UnknownApplicationForLeaveException(Long id) {
        super(id, "application for leave");
    }
}
