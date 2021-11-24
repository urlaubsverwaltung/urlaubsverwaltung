package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

/**
 * Repository for {@link SickNoteComment} entities.
 */
interface SickNoteCommentRepository extends CrudRepository<SickNoteComment, Integer> {

    List<SickNoteComment> findBySickNote(SickNote sickNote);
}
