package org.synyx.urlaubsverwaltung.application.service;

public class DeclineCancellationRequestedApplicationForLeaveNotAllowedException extends RuntimeException {
    DeclineCancellationRequestedApplicationForLeaveNotAllowedException(String message) {
        super(message);
    }
}
