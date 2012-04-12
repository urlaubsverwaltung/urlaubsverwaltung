
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
public interface CommentDAO extends JpaRepository<Comment, Integer> {
    
    @Query("select x from Comment x where x.application = ?1")
    Comment getCommentByApplication(Application a);
    
}
