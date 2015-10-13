package org.synyx.urlaubsverwaltung.core.overtime;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Optional;


/**
 * Provides access to {@link OvertimeComment}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeCommentService {

    /**
     * Creates a comment for the given overtime record with the given action. The given person defines the author of the
     * comment.
     *
     * @param  overtime  to create the comment for
     * @param  action  describes the lifecycle action of the overtime record
     * @param  text  of the comment (is optional)
     * @param  author  of the comment
     *
     * @return  the created comment
     */
    OvertimeComment create(Overtime overtime, OvertimeAction action, Optional<String> text, Person author);
}
