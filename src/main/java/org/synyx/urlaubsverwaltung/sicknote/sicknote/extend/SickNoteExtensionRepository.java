package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface SickNoteExtensionRepository extends CrudRepository<SickNoteExtensionEntity, Long> {

    List<SickNoteExtensionEntity> findAllBySickNoteIdOrderByCreatedAtDesc(Long sickNoteId);
}
