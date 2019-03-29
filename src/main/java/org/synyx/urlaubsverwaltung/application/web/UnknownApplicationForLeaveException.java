package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;


/**
 * Thrown in case no application for leave found for a certain ID.
 */
public class UnknownApplicationForLeaveException extends AbstractNoResultFoundException {

    public UnknownApplicationForLeaveException(Integer id) {

        super(id, "application for leave");
    }
}
