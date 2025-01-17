package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SickNoteExtensionImportService {

    private final SickNoteExtensionRepository sickNoteExtensionRepository;

    SickNoteExtensionImportService(SickNoteExtensionRepository sickNoteExtensionRepository) {
        this.sickNoteExtensionRepository = sickNoteExtensionRepository;
    }

    public void deleteAll() {
        sickNoteExtensionRepository.deleteAll();
    }

    public void importSickNoteExtension(List<SickNoteExtensionEntity> entities) {
        sickNoteExtensionRepository.saveAll(entities);

    }
}
