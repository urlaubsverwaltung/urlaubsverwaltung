package org.synyx.urlaubsverwaltung.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@RequestMapping("/api")
public class SwaggerUiController {

    @GetMapping({"", "/"})
    public RedirectView discover() {
        return new RedirectView("/swagger-ui/index.html", true);
    }
}
