package org.synyx.urlaubsverwaltung.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;

@Configuration
class LocaleConfiguration {

    @Bean
    LocaleResolver localeResolver(UserSettingsService userSettingsService) {
        return new UserSettingsAwareSessionLocaleResolver(userSettingsService);
    }

    @Bean
    LocaleInterceptorConfigurer localeInterceptorConfigurer() {
        return new LocaleInterceptorConfigurer(List.of(localeModelInterceptor()));
    }

    LocaleModelInterceptor localeModelInterceptor() {
        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        localeModelInterceptor.setParamName("language");
        return localeModelInterceptor;
    }
}
