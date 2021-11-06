package org.synyx.urlaubsverwaltung.config;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class LocaleModelInterceptor implements HandlerInterceptor {

    /**
     * Default name of the locale specification parameter: "locale".
     */
    public static final String DEFAULT_PARAM_NAME = "locale";

    private String paramName = DEFAULT_PARAM_NAME;

    /**
     * Set the name of the parameter that contains a locale specification
     * in the model. Default is "locale".
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Return the name of the parameter that contains a locale specification in model.
     */
    public String getParamName() {
        return this.paramName;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            modelAndView.addObject(paramName, LocaleContextHolder.getLocale().toLanguageTag());
        }
    }
}
