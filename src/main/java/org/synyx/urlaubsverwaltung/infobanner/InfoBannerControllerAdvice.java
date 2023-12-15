package org.synyx.urlaubsverwaltung.infobanner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

class InfoBannerControllerAdvice implements HandlerInterceptor {

    private final InfoBannerConfigProperties properties;

    InfoBannerControllerAdvice(InfoBannerConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && modelAndView.hasView()) {
            modelAndView.getModelMap().addAttribute("infoBannerText", properties.getText().getDe());
        }
    }
}
