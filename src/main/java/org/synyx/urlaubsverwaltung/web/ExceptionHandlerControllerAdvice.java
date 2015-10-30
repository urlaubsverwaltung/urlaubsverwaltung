package org.synyx.urlaubsverwaltung.web;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import org.synyx.urlaubsverwaltung.web.sicknote.SickNoteAlreadyInactiveException;


/**
 * Handles exceptions and redirects to error page.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

    private static final String ERROR_PAGE_NAME = "errors";

    /**
     * Get the common error page.
     *
     * @param  exception  has information about cause of error
     *
     * @return  the error page as {@link ModelAndView}
     */
    public static ModelAndView getErrorPage(Exception exception, HttpStatus httpStatus) {

        ModelAndView modelAndView = new ModelAndView(ERROR_PAGE_NAME);
        modelAndView.addObject("exception", exception);
        modelAndView.addObject("statusCode", httpStatus.value());

        return modelAndView;
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        { AbstractNoResultFoundException.class, SickNoteAlreadyInactiveException.class, NumberFormatException.class }
    )
    public ModelAndView handleException(AbstractNoResultFoundException exception) {

        return ExceptionHandlerControllerAdvice.getErrorPage(exception, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleException(AccessDeniedException exception) {

        return ExceptionHandlerControllerAdvice.getErrorPage(exception, HttpStatus.FORBIDDEN);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {

        return ExceptionHandlerControllerAdvice.getErrorPage(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
