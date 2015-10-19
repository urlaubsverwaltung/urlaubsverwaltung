package org.synyx.urlaubsverwaltung.web.sicknote;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;


/**
 * Thrown in case no sick note found for a certain ID.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UnknownSickNoteException extends AbstractNoResultFoundException {

    public UnknownSickNoteException(Integer id) {

        super(id, "sick note");
    }
}
