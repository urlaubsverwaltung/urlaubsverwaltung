package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
class LocaleModelInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (attributeRequired(modelAndView)) {
            modelAndView.addObject("language", LocaleContextHolder.getLocale().toLanguageTag());
        }
    }

    private boolean attributeRequired(ModelAndView modelAndView) {

        if (modelAndView == null) {
            return false;
        }

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("forward:") &&
            !viewName.startsWith("redirect:");
    }
}
