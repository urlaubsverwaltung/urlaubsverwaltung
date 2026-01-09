package org.synyx.urlaubsverwaltung.web.headerscript;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

class HeaderScriptControllerAdvice implements DataProviderInterface {

    private final HeaderScriptConfigProperties properties;

    HeaderScriptControllerAdvice(HeaderScriptConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {
            modelAndView.getModelMap().addAttribute("headerScriptContent", properties.content());
        }
    }
}
