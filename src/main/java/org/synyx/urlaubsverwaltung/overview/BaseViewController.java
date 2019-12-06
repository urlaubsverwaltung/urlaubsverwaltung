package org.synyx.urlaubsverwaltung.overview;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class BaseViewController {

    private static final String ROOT_URL = "/";

    @GetMapping(ROOT_URL)
    public String index() {

        return "redirect:/web/overview";
    }
}
