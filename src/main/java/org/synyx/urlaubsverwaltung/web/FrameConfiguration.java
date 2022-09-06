package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class FrameConfiguration implements WebMvcConfigurer {

    private final FrameDataProvider frameDataProvider;

    @Autowired
    FrameConfiguration(FrameDataProvider frameDataProvider) {
        this.frameDataProvider = frameDataProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(frameDataProvider);
    }
}
