package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


/**
 * Repository for {@link SickNoteComment} entities.
 */
interface SickNoteCommentRepository extends CrudRepository<SickNoteComment, Integer> {

    @Query("select x from SickNoteComment x where x.sickNote = ?1")
    List<SickNoteComment> getCommentsBySickNote(SickNote sickNote);
}
