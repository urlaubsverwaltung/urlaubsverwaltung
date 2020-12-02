package org.synyx.urlaubsverwaltung.sicknote.web;

/**
 * Thrown in case trying to execute action on an already inactive sick note.
 */
public class SickNoteAlreadyInactiveException extends Exception {

    SickNoteAlreadyInactiveException(Integer id) {
        super("Sick note with ID = " + id + " is already inactive.");
    }
}
