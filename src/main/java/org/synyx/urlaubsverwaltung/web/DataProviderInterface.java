package org.synyx.urlaubsverwaltung.web;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public interface DataProviderInterface extends HandlerInterceptor {

    default boolean addDataIf(ModelAndView modelAndView) {

        if (modelAndView == null) {
            return false;
        }

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("forward:")
            && !viewName.startsWith("redirect:");
    }
}
