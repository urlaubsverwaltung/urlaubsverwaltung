package org.synyx.urlaubsverwaltung.web.department;

import org.synyx.urlaubsverwaltung.web.NoResultForIDFoundException;


/**
 * Thrown in case no department found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownDepartmentException extends NoResultForIDFoundException {

    public UnknownDepartmentException(Integer id) {

        super(id, "department");
    }
}
