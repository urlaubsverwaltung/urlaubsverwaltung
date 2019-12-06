package org.synyx.urlaubsverwaltung.web;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


/**
 * Handles exceptions and redirects to error page.
 */
@ControllerAdvice(annotations = Controller.class)
public class ViewExceptionHandlerControllerAdvice {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String ERROR_PAGE_NAME = "errors";

    /**
     * Get the common error page.
     *
     * @param exception has information about cause of error
     * @return the error page as {@link ModelAndView}
     */
    private static ModelAndView getErrorPage(Exception exception, HttpStatus httpStatus) {

        ModelAndView modelAndView = new ModelAndView(ERROR_PAGE_NAME);
        modelAndView.addObject("exception", exception);
        modelAndView.addObject("statusCode", httpStatus.value());

        return modelAndView;
    }


    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({AbstractNoResultFoundException.class, NumberFormatException.class}
    )
    public ModelAndView handleException(AbstractNoResultFoundException exception) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("An exception was thrown", exception);
        }
        return ViewExceptionHandlerControllerAdvice.getErrorPage(exception, BAD_REQUEST);
    }


    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleException(AccessDeniedException exception) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("An exception was thrown", exception);
        }
        return ViewExceptionHandlerControllerAdvice.getErrorPage(exception, FORBIDDEN);
    }


    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {

        LOG.warn("An exception was thrown", exception);

        return ViewExceptionHandlerControllerAdvice.getErrorPage(exception, INTERNAL_SERVER_ERROR);
    }
}
