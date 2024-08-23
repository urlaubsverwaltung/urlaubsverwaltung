package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

enum SickNoteExtensionStatus {

    /**
     * Meanwhile, a newer {@link SickNoteExtension} exists.
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
