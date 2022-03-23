package org.synyx.urlaubsverwaltung.application.application;

/**
 * This exception will be thrown if the person who wants to approve an absence has no rights to do it.
 */
public class NotPrivilegedToApproveException extends Exception {

    NotPrivilegedToApproveException(String message) {
        super(message);
    }
}
