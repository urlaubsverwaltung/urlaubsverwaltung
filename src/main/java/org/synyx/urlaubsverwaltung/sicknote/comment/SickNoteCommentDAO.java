package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link SickNoteComment} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SickNoteCommentDAO extends JpaRepository<SickNoteComment, Integer> {
}
