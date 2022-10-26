package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

/**
 * Repository for {@link SickNoteCommentEntity} entities.
 */
interface SickNoteCommentEntityRepository extends CrudRepository<SickNoteCommentEntity, Integer> {

    List<SickNoteCommentEntity> findBySickNote(SickNote sickNote);

    List<SickNoteCommentEntity> findByPerson(Person author);

    void deleteBySickNotePerson(Person person);
}
