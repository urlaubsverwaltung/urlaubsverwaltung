package org.synyx.urlaubsverwaltung.infobanner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

class InfoBannerControllerAdvice implements DataProviderInterface {

    private final InfoBannerConfigProperties properties;

    InfoBannerControllerAdvice(InfoBannerConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {
            modelAndView.getModelMap().addAttribute("infoBannerText", properties.text().de());
        }
    }
}
