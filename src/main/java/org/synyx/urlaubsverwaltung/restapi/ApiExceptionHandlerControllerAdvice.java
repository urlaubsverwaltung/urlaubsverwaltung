package org.synyx.urlaubsverwaltung.restapi;

import org.apache.log4j.Logger;

import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;


/**
 * Handles exceptions and redirects to error page.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@ControllerAdvice(basePackages = "org.synyx.urlaubsverwaltung.restapi")
public class ApiExceptionHandlerControllerAdvice {

    private static final Logger LOG = Logger.getLogger(ApiExceptionHandlerControllerAdvice.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ NumberFormatException.class, IllegalArgumentException.class })
    @ResponseBody
    public ErrorResponse handleException(HttpServletResponse response,
                                         IllegalArgumentException exception) throws IOException {

        LOG.debug(exception.toString());

        return new ErrorResponse(HttpStatus.BAD_REQUEST, exception);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ErrorResponse handleException(HttpServletResponse response, MethodArgumentTypeMismatchException exception)
        throws IOException {

        LOG.debug(exception.toString());

        return new ErrorResponse(HttpStatus.BAD_REQUEST, exception);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ErrorResponse handleException(HttpServletResponse response,
                                         MissingServletRequestParameterException exception)
        throws IOException {

        LOG.debug(exception.toString());

        return new ErrorResponse(HttpStatus.BAD_REQUEST, exception);
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ErrorResponse handleException(AccessDeniedException exception) {

        LOG.debug(exception.toString());

        return new ErrorResponse(HttpStatus.FORBIDDEN, exception);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleException(HttpServletResponse response, Exception exception) throws IOException {

        LOG.debug(exception.toString());

        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }
}
