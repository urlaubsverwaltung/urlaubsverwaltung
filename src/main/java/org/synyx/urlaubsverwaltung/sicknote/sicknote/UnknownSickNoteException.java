package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

/**
 * Thrown in case no sick note found for a certain ID.
 */
public class UnknownSickNoteException extends AbstractNoResultFoundException {

    UnknownSickNoteException(Integer id) {
        super(id, "sick note");
    }
}
