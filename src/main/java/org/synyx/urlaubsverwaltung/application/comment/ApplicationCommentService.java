package org.synyx.urlaubsverwaltung.application.comment;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;

/**
 * This service provides access to the {@link ApplicationComment} entities.
 */
public interface ApplicationCommentService {

    /**
     * Creates a comment for the given application for leave with the given action. The given person defines the author
     * of the comment.
     *
     * @param application to create the comment for
     * @param action      describes the lifecycle action of the application for leave
     * @param text        of the comment (is optional)
     * @param author      of the comment
     * @return the created comment
     */
    ApplicationComment create(Application application, ApplicationCommentAction action, Optional<String> text, Person author);

    /**
     * Gets all {@link ApplicationComment}s for the given {@link Application}.
     *
     * @param application {@link Application}
     * @return all {@link ApplicationComment}s for the given {@link Application}
     */
    List<ApplicationComment> getCommentsByApplication(Application application);

    /**
     * Deletes all {@link ApplicationComment} in the database for given person.
     * This does not delete comments of this person on other persons applications.
     *
     * @param applicationPerson is the person whose applications should be deleted
     */
    void deleteByApplicationPerson(Person applicationPerson);

    /**
     * Removes the author of a {@link ApplicationComment}. This is used to delete a user.
     * The author remains empty (null). Which must be displayed as 'unknown author'.
     *
     * @param author is the person who is deleted from {@link ApplicationComment}
     */
    void deleteCommentAuthor(Person author);
}
