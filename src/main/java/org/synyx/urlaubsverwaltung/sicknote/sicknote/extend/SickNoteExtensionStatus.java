package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

public enum SickNoteExtensionStatus {

    /**
     * Meanwhile, a newer {@link SickNoteExtension} exists or the referenced
     * {@link org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote} has been converted.
     */
    SUPERSEDED,

    /**
     * The {@link SickNoteExtension} has been submitted by the user and has to be accepted by a privileged person.
     */
    SUBMITTED,

    /**
     * The {@link SickNoteExtension} has been accepted.
     */
    ACCEPTED
}
