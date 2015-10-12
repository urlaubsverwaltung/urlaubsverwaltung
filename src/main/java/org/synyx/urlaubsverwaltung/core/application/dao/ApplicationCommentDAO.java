
package org.synyx.urlaubsverwaltung.core.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;

import java.util.List;


/**
 * Repository for {@link ApplicationComment} entities.
 *
 * @author  Aljona Murygina
 */
public interface ApplicationCommentDAO extends JpaRepository<ApplicationComment, Integer> {

    @Query("select x from ApplicationComment x where x.application = ?1")
    List<ApplicationComment> getCommentsByApplication(Application a);
}
