package org.synyx.urlaubsverwaltung.core.application.service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
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
     * Saves the given {@link Comment}.
     *
     * @param  comment {@link Comment}
     * @param  person {@link Person}
     * @param  application {@link Application}
     */
    void saveComment(Comment comment, Person person, Application application);


    /**
     * Gets all {@link Comment}s for the given {@link Application}.
     *
     * @param  application {@link Application}
     *
     * @return  all {@link Comment}s for the given {@link Application}
     */
    List<Comment> getCommentsByApplication(Application application);
}
