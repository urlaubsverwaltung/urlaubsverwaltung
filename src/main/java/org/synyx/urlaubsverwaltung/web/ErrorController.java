package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;

import org.springframework.stereotype.Controller;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Customize default Spring Boot error page.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class ErrorController extends BasicErrorController {

    @Autowired
    public ErrorController(ErrorAttributes errorAttributes) {

        super(errorAttributes, new ErrorProperties());
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {

        ModelAndView modelAndView = new ModelAndView("errors");
        modelAndView.addObject("statusCode", response.getStatus());

        return modelAndView;
    }
}
