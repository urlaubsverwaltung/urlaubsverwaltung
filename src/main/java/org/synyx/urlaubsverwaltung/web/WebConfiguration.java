package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebConfiguration implements WebMvcConfigurer {

    private final MenuDataProvider menuDataProvider;
    private final PreloadAssetProvider preloadAssetProvider;

    @Autowired
    WebConfiguration(MenuDataProvider menuDataProvider, PreloadAssetProvider preloadAssetProvider) {
        this.menuDataProvider = menuDataProvider;
        this.preloadAssetProvider = preloadAssetProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(menuDataProvider);
        registry.addInterceptor(preloadAssetProvider);
    }
}
