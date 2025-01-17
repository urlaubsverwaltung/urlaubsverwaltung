package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import org.springframework.stereotype.Service;

@Service
public class SickNoteTypeImportService {

    private final SickNoteTypeRepository sickNoteTypeRepository;

    SickNoteTypeImportService(SickNoteTypeRepository sickNoteTypeRepository) {
        this.sickNoteTypeRepository = sickNoteTypeRepository;
    }

    public void deleteAll() {
        sickNoteTypeRepository.deleteAll();
    }

    public SickNoteType importSickNoteType(SickNoteType sickNoteType) {
        return sickNoteTypeRepository.save(sickNoteType);
    }
}
