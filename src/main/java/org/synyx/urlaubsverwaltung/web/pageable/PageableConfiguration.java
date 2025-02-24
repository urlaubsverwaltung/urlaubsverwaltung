package org.synyx.urlaubsverwaltung.web.pageable;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpringDataWebProperties.class)
class PageableConfiguration extends SpringDataWebConfiguration {

    private final SpringDataWebProperties properties;

    public PageableConfiguration(
        ApplicationContext context,
        @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService,
        SpringDataWebProperties properties
    ) {
        super(context, conversionService);
        this.properties = properties;
    }

    @Bean
    PageableUserAwareArgumentResolver pageableResolver(
        SortHandlerMethodArgumentResolver sortResolver,
        UserPaginationSettingsSupplier userPaginationSettingsSupplier,
        PersonService personService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        final PageableUserAwareArgumentResolver pageableResolver =
            new PageableUserAwareArgumentResolver(sortResolver, userPaginationSettingsSupplier, personService, applicationEventPublisher);

        customizePageableResolver(pageableResolver);

        return pageableResolver;
    }

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            final Pageable pageable = this.properties.getPageable();
            resolver.setPageParameterName(pageable.getPageParameter());
            resolver.setSizeParameterName(pageable.getSizeParameter());
            resolver.setOneIndexedParameters(pageable.isOneIndexedParameters());
            resolver.setPrefix(pageable.getPrefix());
            resolver.setQualifierDelimiter(pageable.getQualifierDelimiter());
            resolver.setFallbackPageable(PageRequest.of(0, pageable.getDefaultPageSize()));
            resolver.setMaxPageSize(pageable.getMaxPageSize());
        };
    }

    @Bean
    SortHandlerMethodArgumentResolverCustomizer sortCustomizer() {
        return resolver -> resolver.setSortParameter(this.properties.getSort().getSortParameter());
    }
}
