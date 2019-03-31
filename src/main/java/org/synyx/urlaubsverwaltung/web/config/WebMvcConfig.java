package org.synyx.urlaubsverwaltung.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.synyx.urlaubsverwaltung.web.UserInterceptor;

import java.util.concurrent.TimeUnit;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    @Autowired
    public WebMvcConfig(UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // add signedInUser to modelAndViews
        registry.addInterceptor(userInterceptor).addPathPatterns("/web/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/assets/**")
            .addResourceLocations("classpath:static/assets/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .resourceChain(false)
            .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }
}
