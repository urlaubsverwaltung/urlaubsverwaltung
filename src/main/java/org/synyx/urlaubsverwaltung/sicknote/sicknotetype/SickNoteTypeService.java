package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import java.util.List;

public interface SickNoteTypeService {

    /**
     * Returns a list of all sicknote types that are available
     *
     * @return list of all types
     */
    List<SickNoteType> getSickNoteTypes();

    void insertDefaultSickNoteTypes();
}
