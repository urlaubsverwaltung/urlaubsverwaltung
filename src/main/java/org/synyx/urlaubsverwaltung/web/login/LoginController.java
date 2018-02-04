package org.synyx.urlaubsverwaltung.web.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author  David Schilling - schilling@synyx.de
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private final String applicationVersion;
    private final String authenticationMethod;

    @Autowired
    public LoginController(@Value("${info.app.version}") String applicationVersion,
			@Value("${auth}") String authenticationMethod) {

        this.applicationVersion = applicationVersion;
        this.authenticationMethod = authenticationMethod;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String login(Model model) {
        if (authenticationMethod.equals("azure")) {
			UUID state = UUID.randomUUID();
			UUID nonce = UUID.randomUUID();

			// Save the state and nonce in the session so we can
			// verify after the auth process redirects back
			HttpSession session = request.getSession();

			session.setAttribute("expected_state", state);
			session.setAttribute("expected_nonce", nonce);

			String loginUrl = AuthHelper.getLoginUrl(state, nonce);
			model.addAttribute("loginUrl", loginUrl);
		}
		model.addAttribute("auth", authenticationMethod);
        model.addAttribute("version", applicationVersion);

        return "login/login";
    }
}
