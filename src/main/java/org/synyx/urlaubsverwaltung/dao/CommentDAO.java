
package org.synyx.urlaubsverwaltung.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
public interface CommentDAO extends JpaRepository<Comment, Integer> {
    
    @Query("select x from Comment x where x.application = ?1 and x.status = ?2")
    Comment getCommentByApplicationAndStatus(Application a, ApplicationStatus status);
    
    @Query("select x from Comment x where x.application = ?1")
    List<Comment> getCommentsByApplication(Application a);
}
