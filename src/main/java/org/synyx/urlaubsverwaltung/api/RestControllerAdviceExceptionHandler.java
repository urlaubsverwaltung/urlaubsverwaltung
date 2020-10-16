package org.synyx.urlaubsverwaltung.api;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;


/**
 * Handles exceptions and redirects to error page.
 */
@RestControllerAdvice(annotations = RestControllerAdviceMarker.class)
public class RestControllerAdviceExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleException(MethodArgumentTypeMismatchException exception) {
        throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public void handleException(MissingServletRequestParameterException exception) {
        throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public void handleException(AccessDeniedException exception) {
        throw new ResponseStatusException(FORBIDDEN, exception.getMessage(), exception);
    }
}
