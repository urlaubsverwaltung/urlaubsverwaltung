package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link SickNote} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SickNoteDAO extends JpaRepository<SickNote, Integer> {
}
