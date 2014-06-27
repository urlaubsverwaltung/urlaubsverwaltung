package org.synyx.urlaubsverwaltung.web;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
public class BaseController {

    private static final String ROOT_URL = "/";

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public String index() {

        return "redirect:/web/overview";
    }
}
