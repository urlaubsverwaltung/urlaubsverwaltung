package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

/**
 * Repository for {@link SickNoteCommentEntity} entities.
 */
interface SickNoteCommentRepository extends CrudRepository<SickNoteCommentEntity, Integer> {

    List<SickNoteCommentEntity> findBySickNote(SickNote sickNote);
}
