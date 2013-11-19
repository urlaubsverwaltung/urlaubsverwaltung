package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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


    public SickNoteService() {
    }

    public void save(SickNote sickNote) {

        sickNoteDAO.save(sickNote);
    }


    public List<SickNote> getAll() {

        return sickNoteDAO.findAll();
    }
}
