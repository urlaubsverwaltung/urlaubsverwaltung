package org.synyx.urlaubsverwaltung.department.web;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

/**
 * Thrown in case no department found for a certain ID.
 */
public class UnknownDepartmentException extends AbstractNoResultFoundException {
    public UnknownDepartmentException(Long id) {
        super(id, "department");
    }
}
