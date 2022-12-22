package org.synyx.urlaubsverwaltung.overtime.web;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

/**
 * Thrown in case no overtime record found for a certain ID.
 */
public class UnknownOvertimeException extends AbstractNoResultFoundException {
    UnknownOvertimeException(Long id) {
        super(id, "overtime");
    }
}
