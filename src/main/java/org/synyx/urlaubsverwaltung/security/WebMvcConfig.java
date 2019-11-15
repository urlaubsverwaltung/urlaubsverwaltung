package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/assets/**", "/css/**", "/images/**")
            .addResourceLocations("classpath:static/assets/", "classpath:static/css/", "classpath:static/images/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .resourceChain(false)
            // do not add contentVersionStrategy for "/assets/**" since these files are already handled by the webpack build
            .addResolver(new VersionResourceResolver().addContentVersionStrategy("/css/**", "/images/**"))
            .addTransformer(new CssLinkResourceTransformer());
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
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
