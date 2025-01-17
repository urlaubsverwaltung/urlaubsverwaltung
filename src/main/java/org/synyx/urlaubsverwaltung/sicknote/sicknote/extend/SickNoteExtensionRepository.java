package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

interface SickNoteExtensionRepository extends CrudRepository<SickNoteExtensionEntity, Long> {

    List<SickNoteExtensionEntity> findAllBySickNoteIdOrderByCreatedAtDesc(Long sickNoteId);

    @Query("""
        select
          e as sickNoteExtension,
          s as sickNote
        from sick_note_extension e
          inner join SickNoteEntity s on s.id = e.sickNoteId
        where e.status = :status and s.person.id in :personIds
        """)
    List<SickNoteExtensionProjection> findAllByStatusAndPersonIsIn(@Param("status") SickNoteExtensionStatus status, @Param("personIds") Collection<Long> personIds);

    List<SickNoteExtensionEntity> findAllBySickNoteId(Long sickNoteId);
}
