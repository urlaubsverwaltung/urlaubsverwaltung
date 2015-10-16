package org.synyx.urlaubsverwaltung.web.overtime;

import org.synyx.urlaubsverwaltung.web.NoResultForIDFoundException;


/**
 * Thrown in case no overtime record found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownOvertimeException extends NoResultForIDFoundException {

    public UnknownOvertimeException(Integer id) {

        super(id, "overtime");
    }
}
