package org.synyx.urlaubsverwaltung.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class ThemeConfiguration implements WebMvcConfigurer {

    private final UserThemeDataProvider userThemeDataProvider;

    @Autowired
    ThemeConfiguration(UserThemeDataProvider userThemeDataProvider) {
        this.userThemeDataProvider = userThemeDataProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userThemeDataProvider);
    }
}
