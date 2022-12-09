package org.synyx.urlaubsverwaltung.api;

import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Handles exceptions and redirects to error page.
 */
@RestControllerAdvice(annotations = RestControllerAdviceMarker.class)
public class RestControllerAdviceExceptionHandler {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleException(MethodArgumentTypeMismatchException exception) {
        LOG.error("An 'MethodArgumentTypeMismatchException' was thrown", exception);
        throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public void handleException(MissingServletRequestParameterException exception) {
        LOG.error("An 'MissingServletRequestParameterException' was thrown", exception);
        throw new ResponseStatusException(BAD_REQUEST, exception.getMessage(), exception);
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public void handleException(AccessDeniedException exception) {
        LOG.error("An 'AccessDeniedException' was thrown", exception);
        throw new ResponseStatusException(FORBIDDEN, exception.getMessage(), exception);
    }
}
