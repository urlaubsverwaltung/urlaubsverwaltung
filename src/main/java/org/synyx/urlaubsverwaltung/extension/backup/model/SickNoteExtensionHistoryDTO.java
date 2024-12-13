package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionEntity;

import java.time.Instant;
import java.time.LocalDate;

public record SickNoteExtensionHistoryDTO(Instant createdAt, LocalDate endDate, boolean aub,
                                          SickNoteExtensionStatusDTO sickNoteExtensionStatus) {
    public SickNoteExtensionEntity toSickNoteExtensionEntity(Long importedSickNoteId) {
        final SickNoteExtensionEntity entity = new SickNoteExtensionEntity();
        entity.setCreatedAt(this.createdAt);
        entity.setSickNoteId(importedSickNoteId);
        entity.setNewEndDate(this.endDate);
        entity.setAub(this.aub);
        entity.setStatus(this.sickNoteExtensionStatus.toSickNoteExtensionStatus());
        return entity;
    }
}
