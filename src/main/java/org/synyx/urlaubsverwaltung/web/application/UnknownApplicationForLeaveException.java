package org.synyx.urlaubsverwaltung.web.application;

import org.synyx.urlaubsverwaltung.web.NoResultForIDFoundException;


/**
 * Thrown in case no application for leave found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownApplicationForLeaveException extends NoResultForIDFoundException {

    public UnknownApplicationForLeaveException(Integer id) {

        super(id, "application for leave");
    }
}
