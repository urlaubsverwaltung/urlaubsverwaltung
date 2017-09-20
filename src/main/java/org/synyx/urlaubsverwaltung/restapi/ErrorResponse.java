package org.synyx.urlaubsverwaltung.restapi;

import lombok.Value;
import org.springframework.http.HttpStatus;


/**
 * Represents an API error.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Value
class ErrorResponse {

    long timestamp;
    int status;
    String error;
    String exception;
    String message;

    ErrorResponse(HttpStatus status, Exception exception) {

        this.timestamp = System.currentTimeMillis();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.exception = exception.getClass().getName();
        this.message = exception.getMessage();
    }

}
