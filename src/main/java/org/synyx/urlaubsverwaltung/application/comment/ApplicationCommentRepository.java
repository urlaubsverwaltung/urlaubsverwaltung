package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

/**
 * Repository for {@link ApplicationComment} entities.
 */
interface ApplicationCommentRepository extends CrudRepository<ApplicationComment, Long> {

    List<ApplicationComment> findByApplication(Application application);

    List<ApplicationComment> findByPerson(Person person);

    @Modifying
    void deleteByApplicationPerson(Person person);
}
