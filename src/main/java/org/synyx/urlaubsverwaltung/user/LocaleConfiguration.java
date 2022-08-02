package org.synyx.urlaubsverwaltung.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class LocaleConfiguration implements WebMvcConfigurer {

    private final LocaleModelInterceptor localeModelInterceptor;

    LocaleConfiguration(LocaleModelInterceptor localeModelInterceptor) {
        this.localeModelInterceptor = localeModelInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeModelInterceptor);
    }

    @Bean
    LocaleResolver localeResolver(UserSettingsService userSettingsService) {
        return new UserSettingsAwareSessionLocaleResolver(userSettingsService);
    }
}
