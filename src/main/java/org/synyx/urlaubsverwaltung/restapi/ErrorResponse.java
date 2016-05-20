package org.synyx.urlaubsverwaltung.restapi;

import org.springframework.http.HttpStatus;


/**
 * Represents an API error.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
class ErrorResponse {

    private long timestamp;
    private int status;
    private String error;
    private String exception;
    private String message;

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
