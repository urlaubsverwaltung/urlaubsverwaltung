package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Handles exceptions and redirects to error page.
 */
@ControllerAdvice(basePackages = {"org.synyx.urlaubsverwaltung.sicknote.sicknote"}, annotations = Controller.class)
public class SickNoteExceptionHandlerControllerAdvice {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final String ERROR_PAGE_NAME = "errors";

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(SickNoteAlreadyInactiveException.class)
    public ModelAndView handleException(AbstractNoResultFoundException exception) {
        LOG.error("An exception was thrown", exception);
        return getErrorPage(exception, BAD_REQUEST);
    }

    /**
     * Get the common error page.
     *
     * @param exception has information about cause of error
     * @return the error page as {@link ModelAndView}
     */
    private static ModelAndView getErrorPage(Exception exception, HttpStatus httpStatus) {

        final ModelAndView modelAndView = new ModelAndView(ERROR_PAGE_NAME);
        modelAndView.addObject("exception", exception);
        modelAndView.addObject("statusCode", httpStatus.value());

        return modelAndView;
    }
}
