package org.synyx.urlaubsverwaltung.infobanner;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
