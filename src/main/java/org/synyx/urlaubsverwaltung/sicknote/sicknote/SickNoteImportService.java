package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.stereotype.Service;

@Service
public class SickNoteImportService {

    private final SickNoteRepository sickNoteRepository;

    SickNoteImportService(SickNoteRepository sickNoteRepository) {
        this.sickNoteRepository = sickNoteRepository;
    }

    public void deleteAll() {
        sickNoteRepository.deleteAll();
    }

    public SickNoteEntity importSickNote(SickNoteEntity sickNoteEntity) {
        return sickNoteRepository.save(sickNoteEntity);
    }
}
