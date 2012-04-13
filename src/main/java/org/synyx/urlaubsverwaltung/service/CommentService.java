package org.synyx.urlaubsverwaltung.service;

import java.util.List;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  Aljona Murygina
 */
public interface CommentService {

    void saveComment(Comment comment, Person person, Application application);
    
    Comment getCommentByApplicationAndStatus(Application a, ApplicationStatus status);
    
    List<Comment> getCommentsByApplication(Application a);
}
