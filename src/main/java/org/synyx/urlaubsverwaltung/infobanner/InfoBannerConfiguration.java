package org.synyx.urlaubsverwaltung.infobanner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(InfoBannerConfigProperties.class)
@ConditionalOnProperty(prefix = "uv.info-banner", name = "enabled", havingValue = "true")
class InfoBannerConfiguration implements WebMvcConfigurer {

    @Bean
    InfoBannerControllerAdvice infoBannerControllerAdvice(InfoBannerConfigProperties properties) {
        return new InfoBannerControllerAdvice(properties);
    }

    @Bean
    WebMvcConfigurer infoBannerWebMvcConfigurer(InfoBannerControllerAdvice infoBannerControllerAdvice) {
        return new InfoBannerWebMvcConfigurer(infoBannerControllerAdvice);
    }

    static class InfoBannerWebMvcConfigurer implements WebMvcConfigurer {

        private final InfoBannerControllerAdvice infoBannerControllerAdvice;

        InfoBannerWebMvcConfigurer(InfoBannerControllerAdvice infoBannerControllerAdvice) {
            this.infoBannerControllerAdvice = infoBannerControllerAdvice;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(infoBannerControllerAdvice);
        }
    }
}
