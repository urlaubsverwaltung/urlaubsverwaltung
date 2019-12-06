package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
class SickNoteTypeServiceImpl implements SickNoteTypeService {

    private final SickNoteTypeDAO sickNoteTypeDAO;

    @Autowired
    public SickNoteTypeServiceImpl(SickNoteTypeDAO sickNoteTypeDAO) {

        this.sickNoteTypeDAO = sickNoteTypeDAO;
    }

    @Override
    public List<SickNoteType> getSickNoteTypes() {

        return this.sickNoteTypeDAO.findAll();
    }
}
