package org.synyx.urlaubsverwaltung.application.service;

public class EditApplicationForLeaveNotAllowedException extends RuntimeException {
    EditApplicationForLeaveNotAllowedException(String message) {
        super(message);
    }
}
