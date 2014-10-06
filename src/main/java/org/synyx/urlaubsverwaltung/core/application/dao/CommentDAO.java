
package org.synyx.urlaubsverwaltung.core.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;

import java.util.List;


/**
 * Repository for {@link Comment} entities.
 *
 * @author  Aljona Murygina
 */
public interface CommentDAO extends JpaRepository<Comment, Integer> {

    @Query("select x from Comment x where x.application = ?1")
    List<Comment> getCommentsByApplication(Application a);
}
