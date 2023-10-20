package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

interface ApplicationCommentRepository extends CrudRepository<ApplicationCommentEntity, Long> {

    List<ApplicationCommentEntity> findByApplicationId(Long applicationId);

    List<ApplicationCommentEntity> findByPerson(Person person);

    @Modifying
    void deleteByPerson(Person person);

    @Modifying
    @Query("DELETE FROM application_comment c WHERE c.applicationId IN (SELECT a.id FROM application a WHERE a.person = :applicationPerson)")
    void deleteByApplicationPerson(Person applicationPerson);
}
