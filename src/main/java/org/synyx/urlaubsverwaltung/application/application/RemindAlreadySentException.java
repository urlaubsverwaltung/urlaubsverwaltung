package org.synyx.urlaubsverwaltung.application.application;

/**
 * Thrown in case the person of an application for leave tries to remind multiple times the privileged users to decide
 * about the application for leave.
 */
public class RemindAlreadySentException extends Exception {

    RemindAlreadySentException(String message) {
        super(message);
    }
}
