package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Customize default Spring Boot error page.
 */
@Controller
public class ErrorController extends BasicErrorController {

    @Autowired
    public ErrorController(ErrorAttributes errorAttributes) {

        super(errorAttributes, new ErrorProperties());
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {

        final String viewName;
        final int status = response.getStatus();

        if (status == 403) {
            viewName = "thymeleaf/error/403";
        } else if (status == 404) {
            viewName = "thymeleaf/error/404";
        } else {
            viewName = "thymeleaf/error";
        }

        final ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("status", status);

        return modelAndView;
    }
}
