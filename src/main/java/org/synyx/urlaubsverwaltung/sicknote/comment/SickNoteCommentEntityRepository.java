package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

/**
 * Repository for {@link SickNoteCommentEntity} entities.
 */
interface SickNoteCommentEntityRepository extends CrudRepository<SickNoteCommentEntity, Long> {

    List<SickNoteCommentEntity> findBySickNoteId(Long sickNoteId);

    List<SickNoteCommentEntity> findByPerson(Person author);

    @Modifying
    @Query("DELETE FROM sick_note_comment c WHERE c.sickNoteId IN (SELECT s.id FROM SickNoteEntity s WHERE s.person = :person)")
    void deleteBySickNotePerson(@Param("person") Person person);
}
