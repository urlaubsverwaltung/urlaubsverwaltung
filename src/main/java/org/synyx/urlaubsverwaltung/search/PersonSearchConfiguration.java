package org.synyx.urlaubsverwaltung.search;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class PersonSearchConfiguration implements WebMvcConfigurer {

    private final PersonSearchInterceptor personSearchInterceptor;

    PersonSearchConfiguration(PersonSearchInterceptor personSearchInterceptor) {
        this.personSearchInterceptor = personSearchInterceptor;
    }

    /**
     * Application-wide default link strategy used by {@link HasPersonSearch} controllers that do not need a
     * feature-specific link. Provide your own {@link PersonSuggestionUrlStrategy} bean to override the default.
     */
    @Bean
    @ConditionalOnMissingBean(value = PersonSuggestionUrlStrategy.class, name = "defaultPersonSuggestionUrlStrategy")
    PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy() {
        return new DefaultPersonSuggestionUrlStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(PersonSearchUiFragmentSupplier.class)
    PersonSearchUiFragmentSupplier defaultPersonSearchTemplateSupplier() {
        return new DefaultPersonSearchUiFragmentSupplier();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(personSearchInterceptor);
    }
}
