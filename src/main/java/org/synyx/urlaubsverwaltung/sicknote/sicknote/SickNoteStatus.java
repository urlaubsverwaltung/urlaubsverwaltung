package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.util.List;

/**
 * Enum describing which states an {@link SickNote} may have.
 */
public enum SickNoteStatus {

    /**
     * After the submission of a sick note by a user him/herself the saved sick note gets this status
     */
    SUBMITTED,

    /**
     * After creating a sick note the saved sick note gets this status
     */
    ACTIVE,

    /**
     * If a sick note was active and was converted into an application, it gets this status
     */
    CONVERTED_TO_VACATION,

    /**
     * If a sick note has been active and is cancelled afterward, it gets this status.
     */
    CANCELLED;

    /**
     * Returns all active statuses of a sick note.
     * Hint: Sick notes with one of these statuses will be used for calculations and shown as active on the ui.
     *
     * @return a list of all active statuses
     */
    public static List<SickNoteStatus> activeStatuses() {
        return List.of(SUBMITTED, ACTIVE);
    }

    /**
     * Returns all inactive statuses of a sick note.
     * Hint: Sick notes with one of these statuses will not be used for calculations and shown as inactive on the ui.
     *
     * @return a list of all inactive statuses
     */
    public static List<SickNoteStatus> inactiveStatuses() {
        return List.of(CONVERTED_TO_VACATION, CANCELLED);
    }
}
