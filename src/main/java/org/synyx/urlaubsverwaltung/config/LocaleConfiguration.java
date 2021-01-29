package org.synyx.urlaubsverwaltung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;

@Configuration
class LocaleConfiguration implements WebMvcConfigurer {

    private final LocaleInterceptor localeInterceptor;

    @Autowired
    LocaleConfiguration(LocaleInterceptor localeInterceptor) {
        this.localeInterceptor = localeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor);
    }

    @Bean
    MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(10);
        return messageSource;
    }

    @Bean
    LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        // should be dynamically in the future
        // depends on the available messages files
        localeResolver.setSupportedLocales(List.of(GERMAN, ENGLISH));
        localeResolver.setDefaultLocale(GERMAN);
        return localeResolver;
    }
}
