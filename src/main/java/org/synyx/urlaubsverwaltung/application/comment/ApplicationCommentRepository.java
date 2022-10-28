package org.synyx.urlaubsverwaltung.application.comment;

import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.application.application.Application;

import java.util.List;

/**
 * Repository for {@link ApplicationComment} entities.
 */
interface ApplicationCommentRepository extends CrudRepository<ApplicationComment, Integer> {

    List<ApplicationComment> findByApplication(Application application);
}
