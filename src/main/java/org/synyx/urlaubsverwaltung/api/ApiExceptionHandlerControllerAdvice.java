package org.synyx.urlaubsverwaltung.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.synyx.urlaubsverwaltung.workingtime.NoValidWorkingTimeException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;


/**
 * Handles exceptions and redirects to error page.
 */
@RestControllerAdvice(annotations = RestControllerAdviceMarker.class)
public class ApiExceptionHandlerControllerAdvice {

    @ResponseStatus(NO_CONTENT)
    @ExceptionHandler({NoValidWorkingTimeException.class})
    public ErrorResponse handleException(IllegalStateException exception) {

        return new ErrorResponse(NO_CONTENT, exception);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({NumberFormatException.class, IllegalArgumentException.class})
    public ErrorResponse handleException(IllegalArgumentException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleException(MethodArgumentTypeMismatchException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handleException(MissingServletRequestParameterException exception) {

        return new ErrorResponse(BAD_REQUEST, exception);
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleException(AccessDeniedException exception) {

        return new ErrorResponse(HttpStatus.FORBIDDEN, exception);
    }
}
