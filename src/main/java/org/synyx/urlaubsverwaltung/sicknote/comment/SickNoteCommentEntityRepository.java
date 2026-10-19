package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository for {@link SickNoteCommentEntity} entities.
 */
interface SickNoteCommentEntityRepository extends CrudRepository<SickNoteCommentEntity, Long> {

    List<SickNoteCommentEntity> findBySickNoteIdOrderByIdDesc(Long sickNoteId);
}
