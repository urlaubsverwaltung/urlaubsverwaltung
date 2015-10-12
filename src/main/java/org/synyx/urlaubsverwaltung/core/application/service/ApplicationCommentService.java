package org.synyx.urlaubsverwaltung.core.application.service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationCommentStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * This service provides access to the {@link ApplicationComment} entities.
 *
 * @author  Aljona Murygina
 */
public interface ApplicationCommentService {

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
    ApplicationComment create(Application application, ApplicationCommentStatus status, Optional<String> text,
        Person author);


    /**
     * Gets all {@link ApplicationComment}s for the given {@link Application}.
     *
     * @param  application {@link Application}
     *
     * @return  all {@link ApplicationComment}s for the given {@link Application}
     */
    List<ApplicationComment> getCommentsByApplication(Application application);
}
