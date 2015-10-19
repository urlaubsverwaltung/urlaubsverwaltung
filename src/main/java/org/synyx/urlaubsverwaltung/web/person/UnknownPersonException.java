package org.synyx.urlaubsverwaltung.web.person;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;


/**
 * Thrown in case no person found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownPersonException extends AbstractNoResultFoundException {

    public UnknownPersonException(Integer id) {

        super(id, "person");
    }


    public UnknownPersonException(String loginName) {

        super(loginName, "person");
    }
}
