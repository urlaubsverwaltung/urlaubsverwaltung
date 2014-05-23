package org.synyx.urlaubsverwaltung.restapi;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class BaseController {

    private static final String ROOT_URL = "/";

    @Value(value = "${application.version}")
    private String version;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ModelAttribute("response")
    public BaseResponse discover() {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        return new BaseResponse(version, userName);
    }
}
