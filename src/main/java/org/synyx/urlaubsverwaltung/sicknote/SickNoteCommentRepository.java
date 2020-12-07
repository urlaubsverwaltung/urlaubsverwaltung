package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


/**
 * Repository for {@link SickNoteComment} entities.
 */
interface SickNoteCommentRepository extends CrudRepository<SickNoteComment, Integer> {

    List<SickNoteComment> findBySickNote(SickNote sickNote);
}
