package org.synyx.urlaubsverwaltung.sicknote.web;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.AbstractNoResultFoundException;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Handles exceptions and redirects to error page.
 */
@ControllerAdvice(basePackages = {"org.synyx.urlaubsverwaltung.sicknote.web"}, annotations = Controller.class)
public class SicknoteExceptionHandlerControllerAdvice {

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


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SickNoteAlreadyInactiveException.class)
    public ModelAndView handleException(AbstractNoResultFoundException exception) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("An exception was thrown: {}", exception.getClass().getName());
            LOG.debug("An error occurred: {}", exception.getMessage());
        }
        return getErrorPage(exception, HttpStatus.BAD_REQUEST);
    }
}
