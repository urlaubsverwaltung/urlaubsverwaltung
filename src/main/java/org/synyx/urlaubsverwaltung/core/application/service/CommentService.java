package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;


/**
 * This service provides access to the {@link Comment} entities.
 *
 * @author  Aljona Murygina
 */
public interface CommentService {

    /**
     * Creates a comment for the given application for leave with the given status. The given person defines the author
     * of the comment.
     *
     * @param  application  to create the comment for
     * @param  status  describes the lifecycle status of the application for leave
     * @param  text  of the comment (is optional)
     * @param  author  of the comment
     *
     * @return  the created comment
     */
    Comment create(Application application, ApplicationStatus status, Optional<String> text, Person author);


    /**
     * Gets all {@link Comment}s for the given {@link Application}.
     *
     * @param  application {@link Application}
     *
     * @return  all {@link Comment}s for the given {@link Application}
     */
    List<Comment> getCommentsByApplication(Application application);
}
