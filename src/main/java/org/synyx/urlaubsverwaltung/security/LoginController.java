package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final String applicationVersion;

    @Autowired
    public LoginController(@Value("${info.app.version}") String applicationVersion) {

        this.applicationVersion = applicationVersion;
    }

    @GetMapping("")
    public String login(Model model) {

        model.addAttribute("version", applicationVersion);

        return "login/login";
    }
}
