package org.synyx.urlaubsverwaltung.core.sicknote.comment;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.List;
import java.util.Optional;


/**
 * Service for handling {@link SickNoteComment}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SickNoteCommentService {

    /**
     * Creates a comment for the given sick note with the given action. The given person defines the author of the
     * comment.
     *
     * @param  sickNote  to create the comment for
     * @param  action  describes the lifecycle action of the sick note that will be saved in the comment
     * @param  text  of the comment (is optional)
     * @param  author  of the comment
     *
     * @return  the created comment
     */
    SickNoteComment create(SickNote sickNote, SickNoteAction action, Optional<String> text, Person author);


    /**
     * Gets all comments for the given sick note.
     *
     * @param  sickNote  to get the comments for
     *
     * @return  all comments for the given sick note.
     */
    List<SickNoteComment> getCommentsBySickNote(SickNote sickNote);
}
