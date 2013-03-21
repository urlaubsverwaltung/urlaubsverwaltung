package org.synyx.urlaubsverwaltung.application.service;

import java.util.List;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.person.Person;


/**
 * @author  Aljona Murygina
 */
public interface CommentService {

    void saveComment(Comment comment, Person person, Application application);
    
    Comment getCommentByApplicationAndStatus(Application a, ApplicationStatus status);
    
    List<Comment> getCommentsByApplication(Application a);
}
