package org.synyx.urlaubsverwaltung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.synyx.urlaubsverwaltung.web.UserInterceptor;


/**
 * Configuration for WebMvc. Configuration is done by overriding base methods of {@link WebMvcConfigurerAdapter}.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    private final UserInterceptor userInterceptor;

    @Autowired
    public WebMvcConfig(UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(userInterceptor).addPathPatterns("/web/**");
    }
}
