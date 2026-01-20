package org.synyx.urlaubsverwaltung.web.headerscript;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(HeaderScriptConfigProperties.class)
@ConditionalOnProperty(prefix = "uv.header-script", name = "enabled", havingValue = "true")
class HeaderScriptConfiguration implements WebMvcConfigurer {

    @Bean
    HeaderScriptControllerAdvice headerScriptControllerAdvice(HeaderScriptConfigProperties properties) {
        return new HeaderScriptControllerAdvice(properties);
    }

    @Bean
    WebMvcConfigurer headerScriptWebMvcConfigurer(HeaderScriptControllerAdvice headerScriptControllerAdvice) {
        return new HeaderScriptWebMvcConfigurer(headerScriptControllerAdvice);
    }

    static class HeaderScriptWebMvcConfigurer implements WebMvcConfigurer {

        private final HeaderScriptControllerAdvice headerScriptControllerAdvice;

        HeaderScriptWebMvcConfigurer(HeaderScriptControllerAdvice headerScriptControllerAdvice) {
            this.headerScriptControllerAdvice = headerScriptControllerAdvice;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(headerScriptControllerAdvice);
        }
    }
}
