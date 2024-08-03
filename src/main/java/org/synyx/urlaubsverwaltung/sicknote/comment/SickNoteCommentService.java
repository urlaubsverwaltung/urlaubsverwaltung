package org.synyx.urlaubsverwaltung.sicknote.comment;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

/**
 * Service for handling {@link SickNoteCommentEntity}s.
 */
public interface SickNoteCommentService {

    /**
     * Creates a comment for the given sick note with the given action. The given person defines the author of the
     * comment.
     *
     * @param sickNote to create the comment for
     * @param action   describes the lifecycle action of the sick note that will be saved in the comment
     * @param author   of the comment
     * @return the created comment
     */
    SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author);

    /**
     * Creates a comment for the given sick note with the given action. The given person defines the author of the
     * comment.
     *
     * @param sickNote to create the comment for
     * @param action   describes the lifecycle action of the sick note that will be saved in the comment
     * @param author   of the comment
     * @param text     optional text of the comment, can be {@code null}
     * @return the created comment
     */
    SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author, @Nullable String text);

    /**
     * Gets all comments for the given sick note.
     *
     * @param sickNote to get the comments for
     * @return all comments for the given sick note.
     */
    List<SickNoteCommentEntity> getCommentsBySickNote(SickNote sickNote);

    /**
     * Deletes all {@link SickNoteCommentEntity} in the database for given person.
     * This does not delete comments of this person on other persons sicknotes
     *
     * @param sickNotePerson is the person whose sicknotes should be deleted
     */
    void deleteAllBySickNotePerson(Person sickNotePerson);

    /**
     * Removes the author of a {@link SickNoteCommentEntity}. This is used to delete a user.
     * The author remains empty (null). Which must be displayed as 'unknown author'.
     *
     * @param author is the person who is deleted from {@link SickNoteCommentEntity}
     */
    void deleteCommentAuthor(Person author);
}
