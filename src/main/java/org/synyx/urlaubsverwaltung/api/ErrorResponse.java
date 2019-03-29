package org.synyx.urlaubsverwaltung.api;

import org.springframework.http.HttpStatus;


/**
 * Represents an API error.
 */
class ErrorResponse {

    private final long timestamp;
    private final int status;
    private final String error;
    private final String exception;
    private final String message;

    ErrorResponse(HttpStatus status, Exception exception) {

        this.timestamp = System.currentTimeMillis();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.exception = exception.getClass().getName();
        this.message = exception.getMessage();
    }

    public long getTimestamp() {

        return timestamp;
    }


    public int getStatus() {

        return status;
    }


    public String getError() {

        return error;
    }


    public String getException() {

        return exception;
    }


    public String getMessage() {

        return message;
    }
}
