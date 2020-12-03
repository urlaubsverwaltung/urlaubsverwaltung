package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Interceptor to add menu specific information to all requests
 */
@Component
public class MenuDataProvider implements HandlerInterceptor {

    private final PersonService personService;

    @Autowired
    public MenuDataProvider(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        if (menuIsShown(modelAndView)) {

            final Person signedInUserInModel = (Person) modelAndView.getModelMap().get("signedInUser");
            final String gravatarUrl = Objects.requireNonNullElseGet(signedInUserInModel, personService::getSignedInUser).getGravatarURL();

            modelAndView.getModelMap().addAttribute("menuGravatarUrl", gravatarUrl);
        }
    }

    private boolean menuIsShown(ModelAndView modelAndView) {

        if (modelAndView == null || modelAndView.getViewName() == null) {
            return false;
        }

        final String viewName = modelAndView.getViewName();
        return !viewName.startsWith("redirect:")
            && !viewName.startsWith("login");
    }
}
