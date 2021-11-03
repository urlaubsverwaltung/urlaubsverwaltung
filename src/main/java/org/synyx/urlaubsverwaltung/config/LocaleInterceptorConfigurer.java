package org.synyx.urlaubsverwaltung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

class LocaleInterceptorConfigurer implements WebMvcConfigurer {

    private final List<HandlerInterceptor> handlerInterceptors;

    @Autowired
    LocaleInterceptorConfigurer(List<HandlerInterceptor> handlerInterceptors) {
        this.handlerInterceptors = handlerInterceptors;
    }

    public List<HandlerInterceptor> getHandlerInterceptors() {
        return handlerInterceptors;
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        handlerInterceptors.forEach(interceptorRegistry::addInterceptor);
    }
}
