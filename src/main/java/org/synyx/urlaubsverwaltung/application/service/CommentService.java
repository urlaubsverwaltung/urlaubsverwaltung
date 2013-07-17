package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.Comment;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * This service provides access to the {@link Comment} entities.
 *
 * @author  Aljona Murygina
 */
public interface CommentService {

    /**
     * Saves the given {@link Comment}.
     *
     * @param  comment {@link Comment}
     * @param  person {@link Person}
     * @param  application {@link Application}
     */
    void saveComment(Comment comment, Person person, Application application);


    /**
     * Gets the {@link Comment} for the given {@link Application} and {@link ApplicationStatus}.
     *
     * @param  application {@link Application}
     * @param  status {@link ApplicationStatus}
     *
     * @return  {@link Comment} for the given {@link Application} and {@link ApplicationStatus}
     */
    Comment getCommentByApplicationAndStatus(Application application, ApplicationStatus status);


    /**
     * Gets all {@link Comment}s for the given {@link Application}.
     *
     * @param  application {@link Application}
     *
     * @return  all {@link Comment}s for the given {@link Application}
     */
    List<Comment> getCommentsByApplication(Application application);
}
