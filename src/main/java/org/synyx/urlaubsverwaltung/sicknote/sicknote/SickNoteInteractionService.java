package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * Provides interactions with sick notes, i.e. create, edit etc.
 */
public interface SickNoteInteractionService {

    /**
     * Creates a new sick note in SUBMITTED state if a user submits the sick note for him/herself
     *
     * @param sickNote
     * @param submitter
     * @param comment
     * @return the saved sick note in SUBMITTED state
     */
    SickNote submit(SickNote sickNote, Person submitter, String comment);

    /**
     * A maintainer (OFFICE or SICK_NOTE_VIEW_ADD_EDIT) accepts a sick note which was submitted by user
     *
     * @param sickNote
     * @param maintainer with role OFFICE or SICK_NOTE_VIEW_ADD_EDIT
     * @param comment optional comment when accepting a sick note
     * @return the saved sick note in ACTIVE state
     */
    SickNote accept(SickNote sickNote, Person maintainer, String comment);

    /**
     * Creates a new sick note.
     *
     * @param sickNote to be saved
     * @param creator  the person who creates the sick note
     * @return the saved sick note
     */
    SickNote create(SickNote sickNote, Person creator);

    /**
     * Creates a new sick note.
     *
     * @param sickNote to be saved
     * @param creator  the person who creates the sick note
     * @param comment  giving further information
     * @return the saved sick note
     */
    SickNote create(SickNote sickNote, Person creator, String comment);

    /**
     * Update an existent sick note with comment.
     *
     * @param sickNote to be updated
     * @param editor   the person who updates the sick note
     * @param comment  optional comment giving further information, can be {@code null}
     * @return the updated sick note
     */
    SickNote update(SickNote sickNote, Person editor, @Nullable String comment);

    /**
     * Convert a sick note to an allowed application for leave.
     *
     * @param sickNote    to be converted to vacation
     * @param application represents the application for leave that is created
     * @param converter   the person who converts the sick note
     * @return the converted sick note
     */
    SickNote convert(SickNote sickNote, Application application, Person converter);

    /**
     * Cancel an existent sick note.
     *
     * @param sickNote  to be cancelled
     * @param canceller the person who cancels the sick note
     * @param comment reason for cancellation
     * @return the cancelled sick note
     */
    SickNote cancel(SickNote sickNote, Person canceller, String comment);
}
