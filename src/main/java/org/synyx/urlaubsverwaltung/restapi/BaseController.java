package org.synyx.urlaubsverwaltung.restapi;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller("restApiBaseController")
@RequestMapping("/api")
public class BaseController {

    private static final String ROOT_URL = "/";
    private static final boolean RELATIVE_CONTEXT = true;

    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    public RedirectView discover() {

        return new RedirectView("index.html", RELATIVE_CONTEXT);
    }
}
