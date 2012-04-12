package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Comment;


/**
 * @author  Aljona Murygina
 */
public interface CommentService {

    void saveComment(Comment comment);
    
    Comment getCommentByApplication(Application a);
}
