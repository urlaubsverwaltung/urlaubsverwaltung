package org.synyx.urlaubsverwaltung.web.pageable;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.user.pagination.UserPaginationSettingsSupplier;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DataWebProperties.class)
class PageableConfiguration extends DataWebProperties {

    private final DataWebProperties properties;

    public PageableConfiguration(DataWebProperties dataWebProperties) {
        this.properties = dataWebProperties;
    }

    @Bean
    PageableUserAwareArgumentResolver pageableResolver(
        SortHandlerMethodArgumentResolver sortResolver,
        UserPaginationSettingsSupplier userPaginationSettingsSupplier,
        PersonService personService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        return new PageableUserAwareArgumentResolver(sortResolver, userPaginationSettingsSupplier, personService, applicationEventPublisher);
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
