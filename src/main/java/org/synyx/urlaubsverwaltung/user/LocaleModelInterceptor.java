package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

import java.util.Locale;

@Component
class LocaleModelInterceptor implements DataProviderInterface {

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {
            final Locale currentLocale = LocaleContextHolder.getLocale();
            modelAndView.addObject("locale", currentLocale);
            modelAndView.addObject("language", currentLocale.toLanguageTag());
        }
    }
}
