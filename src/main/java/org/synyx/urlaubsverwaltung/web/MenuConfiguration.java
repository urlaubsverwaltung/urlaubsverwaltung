package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class MenuConfiguration implements WebMvcConfigurer {

    private final MenuDataProvider menuDataProvider;

    @Autowired
    MenuConfiguration( MenuDataProvider menuDataProvider) {
        this.menuDataProvider = menuDataProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(menuDataProvider);
    }
}
