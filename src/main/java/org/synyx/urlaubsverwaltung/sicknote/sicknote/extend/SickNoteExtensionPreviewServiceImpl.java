package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus.SUBMITTED;

@Service
class SickNoteExtensionPreviewServiceImpl implements SickNoteExtensionPreviewService {

    private final SickNoteExtensionRepository repository;
    private final SickNoteService sickNoteService;

    SickNoteExtensionPreviewServiceImpl(SickNoteExtensionRepository repository, SickNoteService sickNoteService) {
        this.repository = repository;
        this.sickNoteService = sickNoteService;
    }

    @Override
    public Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId) {

        final SickNote sickNote = getSickNote(sickNoteId);
        final List<SickNoteExtensionEntity> extensions = repository.findAllBySickNoteIdOrderByCreatedAtDesc(sickNoteId);

        return extensions.stream()
            .findFirst()
            .filter(extension -> SUBMITTED.equals(extension.getStatus()))
            .map(extensionEntity -> new SickNoteExtensionPreview(
                extensionEntity.getId(),
                sickNote.getStartDate(),
                extensionEntity.getNewEndDate(),
                extensionEntity.isAub(),
                // TODO working days
                BigDecimal.valueOf(42L)
            ));
    }


    private SickNote getSickNote(Long id) {
        return sickNoteService.getById(id)
            .orElseThrow(() -> new IllegalStateException("could not find referenced sickNote with id=" + id));
    }
}
