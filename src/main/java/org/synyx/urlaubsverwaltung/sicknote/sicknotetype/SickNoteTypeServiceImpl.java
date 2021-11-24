package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SickNoteTypeServiceImpl implements SickNoteTypeService {

    private final SickNoteTypeRepository sickNoteTypeRepository;

    @Autowired
    public SickNoteTypeServiceImpl(SickNoteTypeRepository sickNoteTypeRepository) {
        this.sickNoteTypeRepository = sickNoteTypeRepository;
    }

    @Override
    public List<SickNoteType> getSickNoteTypes() {
        return this.sickNoteTypeRepository.findAll();
    }
}
