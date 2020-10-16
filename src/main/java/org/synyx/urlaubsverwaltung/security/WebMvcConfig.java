package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;
    private final LocaleInterceptor localeInterceptor;

    @Autowired
    public WebMvcConfig(UserInterceptor userInterceptor, LocaleInterceptor localeInterceptor) {
        this.userInterceptor = userInterceptor;
        this.localeInterceptor = localeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // add signedInUser to modelAndViews
        registry.addInterceptor(userInterceptor).addPathPatterns("/web/**");
        registry.addInterceptor(localeInterceptor);
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        // should be dynamically in the future
        // depends on the available messages files
        localeResolver.setSupportedLocales(List.of(Locale.GERMAN, Locale.ENGLISH));
        localeResolver.setDefaultLocale(Locale.GERMAN);
        return localeResolver;
    }
}
