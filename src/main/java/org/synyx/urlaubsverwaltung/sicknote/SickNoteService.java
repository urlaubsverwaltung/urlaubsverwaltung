package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;


/**
 * Service for handling {@link SickNote}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteService {

    private SickNoteDAO sickNoteDAO;

    @Autowired
    public SickNoteService(SickNoteDAO sickNoteDAO) {

        this.sickNoteDAO = sickNoteDAO;
    }
}
