package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SickNoteExtensionHistoryServiceImpl implements SickNoteExtensionHistoryService {

    private final SickNoteExtensionRepository sickNoteExtensionRepository;

    SickNoteExtensionHistoryServiceImpl(SickNoteExtensionRepository sickNoteExtensionRepository) {
        this.sickNoteExtensionRepository = sickNoteExtensionRepository;
    }

    @Override
    public List<SickNoteExtensionHistory> getSickNoteExtensionHistory(Long sickNoteId) {
        return sickNoteExtensionRepository.findAllBySickNoteId(sickNoteId).stream().map(sickNoteExtension -> new SickNoteExtensionHistory(sickNoteExtension.getCreatedAt(), sickNoteExtension.getNewEndDate(), sickNoteExtension.isAub(), sickNoteExtension.getStatus())).toList();
    }

}
