package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ui.ModelMap;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interceptor to add current user specific attributes like the Gravatar URL to every response.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class UserInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    @Autowired
    public UserInterceptor(SessionService sessionService) {

        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) {

        if (modelAndView != null && !modelAndView.getViewName().startsWith("redirect:")) {
            Person signedInUser = sessionService.getSignedInUser();
            String gravatar = GravatarUtil.createImgURL(signedInUser.getEmail());

            ModelMap modelMap = modelAndView.getModelMap();

            modelMap.addAttribute("signedInUser", signedInUser);
            modelMap.addAttribute("gravatar", gravatar);
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
        Object o, Exception e) {

        // OK
    }
}
