package org.synyx.urlaubsverwaltung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.List;

@Configuration
class LocaleConfiguration {

    @Bean
    LocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

    @Bean
    LocaleInterceptorConfigurer localeInterceptorConfigurer() {
        return new LocaleInterceptorConfigurer(List.of(localeChangeInterceptor(), localeModelInterceptor()));
    }

    LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language");
        return localeChangeInterceptor;
    }

    LocaleModelInterceptor localeModelInterceptor() {
        final LocaleModelInterceptor localeModelInterceptor = new LocaleModelInterceptor();
        localeModelInterceptor.setParamName("language");
        return localeModelInterceptor;
    }
}
