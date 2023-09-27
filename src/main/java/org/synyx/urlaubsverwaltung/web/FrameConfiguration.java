package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class FrameConfiguration implements WebMvcConfigurer {

    private final FrameDataProvider frameDataProvider;
    private final PreloadAssetProvider preloadAssetProvider;

    @Autowired
    FrameConfiguration(FrameDataProvider frameDataProvider, PreloadAssetProvider preloadAssetProvider) {
        this.frameDataProvider = frameDataProvider;
        this.preloadAssetProvider = preloadAssetProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(frameDataProvider);
        registry.addInterceptor(preloadAssetProvider);
    }
}
