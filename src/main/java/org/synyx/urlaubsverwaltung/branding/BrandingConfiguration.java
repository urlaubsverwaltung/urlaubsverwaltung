package org.synyx.urlaubsverwaltung.branding;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(BrandingProperties.class)
class BrandingConfiguration implements WebMvcConfigurer {

    private final BrandingDataProvider brandingDataProvider;

    BrandingConfiguration(BrandingDataProvider brandingDataProvider) {
        this.brandingDataProvider = brandingDataProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(brandingDataProvider);
    }
}
