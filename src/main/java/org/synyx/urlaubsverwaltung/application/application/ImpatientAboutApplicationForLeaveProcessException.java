package org.synyx.urlaubsverwaltung.application.application;

/**
 * Thrown in case the person of an application for leave is too impatient and tries too early to remind the privileged
 * user(s) that they should decide about the application for leave.
 */
public class ImpatientAboutApplicationForLeaveProcessException extends Exception {

    ImpatientAboutApplicationForLeaveProcessException(String message) {
        super(message);
    }
}
